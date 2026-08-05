import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react'
import { menuItems, restaurants } from '../data'
import { api, TOKEN_KEY } from '../lib/api'

const AppContext = createContext(null)
const SESSION_KEY = 'quickbite_session_v1'
const CART_KEY = 'quickbite_cart_v1'

function normalizeCartItem(item) {
  if (!item || typeof item !== 'object') return null

  const id = Number(item.id ?? item.menuItemId)
  const price = Number(item.price)
  const quantity = Math.max(1, Number(item.quantity) || 1)
  if (!Number.isFinite(id) || !Number.isFinite(price) || !item.name) return null

  return {
    ...item,
    id,
    restaurantId: Number(item.restaurantId) || item.restaurantId,
    price,
    quantity,
    imageURL: item.imageURL || '/assets/hero-jollof.png',
    isAvailable: item.isAvailable !== false,
    isLocalPreview: item.isLocalPreview === true,
  }
}

function loadCart() {
  try {
    const saved = JSON.parse(localStorage.getItem(CART_KEY))
    return Array.isArray(saved) ? saved.map(normalizeCartItem).filter(Boolean) : []
  } catch {
    return []
  }
}

function loadUser() {
  try {
    const saved = JSON.parse(localStorage.getItem(SESSION_KEY))
    return saved && typeof saved === 'object' ? saved : null
  } catch {
    return null
  }
}

function mapServerCart(response) {
  if (!Array.isArray(response?.cartItems)) return []

  return response.cartItems.map((item) => normalizeCartItem({
    id: item.menuItemId,
    cartItemId: item.cartItemId,
    restaurantId: item.restaurantId,
    restaurantName: item.restaurantName,
    name: item.itemName,
    price: item.price,
    quantity: item.quantity,
    imageURL: item.imageURL || '/assets/hero-jollof.png',
    isAvailable: item.isAvailable,
  })).filter(Boolean)
}

export function AppProvider({ children }) {
  const [cart, setCart] = useState(loadCart)
  const [user, setUser] = useState(loadUser)
  const [isCartOpen, setCartOpen] = useState(false)
  const [isAuthOpen, setAuthOpen] = useState(false)
  const [toast, setToast] = useState('')
  const hydratedUserId = useRef(null)

  const saveCart = useCallback((next) => {
    setCart(next)
    localStorage.setItem(CART_KEY, JSON.stringify(next))
  }, [])

  const replaceWithServerCart = useCallback((response) => {
    saveCart(mapServerCart(response))
  }, [saveCart])

  useEffect(() => {
    if (user?.role !== 'CUSTOMER' || hydratedUserId.current === user.id) return
    hydratedUserId.current = user.id
    api.cart()
      .then(replaceWithServerCart)
      .catch(() => {
        hydratedUserId.current = null
        setToast('Your saved cart could not be loaded. Local preview mode is still available.')
      })
  }, [replaceWithServerCart, user])

  const addItem = useCallback(async (item) => {
    const nextItem = normalizeCartItem(item)
    if (!nextItem) {
      setToast('Open the restaurant to choose an available menu item.')
      return
    }
    const existingRestaurant = cart[0]?.restaurantId
    if (existingRestaurant && String(existingRestaurant) !== String(nextItem.restaurantId)) {
      setToast('Your cart can only contain items from one restaurant.')
      return
    }
    if (user && user.role !== 'CUSTOMER') {
      setToast('Please use a customer account to place an order.')
      return
    }
    const current = cart.find((cartItem) => cartItem.id === nextItem.id)
    const next = current
      ? cart.map((cartItem) => cartItem.id === nextItem.id
        ? { ...cartItem, ...nextItem, quantity: cartItem.quantity + 1 }
        : cartItem)
      : [...cart, nextItem]
    saveCart(next)
    setCartOpen(true)
    if (!user || nextItem.isLocalPreview) return

    try {
      const response = await api.addToCart({ menuItemId: nextItem.id, quantity: 1 })
      replaceWithServerCart(response)
    } catch (error) {
      saveCart(cart)
      setToast(error.message)
    }
  }, [cart, replaceWithServerCart, saveCart, user])

  const updateQuantity = useCallback(async (id, quantity) => {
    const currentItem = cart.find((item) => item.id === id)
    const previous = cart
    saveCart(quantity === 0
      ? cart.filter((item) => item.id !== id)
      : cart.map((item) => item.id === id ? { ...item, quantity } : item))
    if (!user || !currentItem?.cartItemId) return

    try {
      const response = await api.updateCartItem(currentItem.cartItemId, quantity)
      replaceWithServerCart(response)
    } catch (error) {
      saveCart(previous)
      setToast(error.message)
    }
  }, [cart, replaceWithServerCart, saveCart, user])

  const resetCart = useCallback(() => {
    saveCart([])
  }, [saveCart])

  const clearCart = useCallback(async () => {
    const previous = cart
    resetCart()
    if (user?.role !== 'CUSTOMER') return

    try {
      await api.clearCart()
    } catch (error) {
      saveCart(previous)
      setToast(error.message)
    }
  }, [cart, resetCart, saveCart, user])

  const syncCart = useCallback(async () => {
    if (user?.role !== 'CUSTOMER') return cart

    let serverCart = await api.cart()
    const serverItemIds = new Set((serverCart.cartItems || []).map((item) => Number(item.menuItemId)))
    for (const item of cart) {
      if (!item.cartItemId && !serverItemIds.has(Number(item.id))) {
        serverCart = await api.addToCart({ menuItemId: item.id, quantity: item.quantity })
        serverItemIds.add(Number(item.id))
      }
    }

    const syncedCart = mapServerCart(serverCart)
    saveCart(syncedCart)
    return syncedCart
  }, [cart, saveCart, user])

  const authenticate = useCallback(async (session) => {
    const nextUser = {
      id: session.id,
      name: session.name,
      email: session.email,
      role: session.role,
      accountStatus: session.accountStatus,
      verificationStatus: session.verificationStatus,
    }
    localStorage.setItem(TOKEN_KEY, session.token)
    localStorage.setItem(SESSION_KEY, JSON.stringify(nextUser))
    hydratedUserId.current = nextUser.id
    setUser(nextUser)

    if (nextUser.role !== 'CUSTOMER') {
      resetCart()
      return
    }

    try {
      let serverCart = await api.cart()
      const previewItems = cart.filter((item) => item.isLocalPreview)
      const syncableItems = cart.filter((item) => !item.isLocalPreview)
      if (serverCart.cartItems?.length === 0 && syncableItems.length > 0) {
        for (const item of syncableItems) {
          serverCart = await api.addToCart({ menuItemId: item.id, quantity: item.quantity })
        }
      }
      saveCart([...mapServerCart(serverCart), ...previewItems])
    } catch {
      setToast('Signed in. Your cart will sync when the API is available.')
    }
  }, [cart, resetCart, saveCart])

  const signOut = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(SESSION_KEY)
    hydratedUserId.current = null
    setUser(null)
    resetCart()
    setToast('You have been signed out.')
  }, [resetCart])

  const updateSessionUser = useCallback((profile) => {
    setUser((current) => {
      if (!current) return current
      const next = { ...current, ...profile }
      localStorage.setItem(SESSION_KEY, JSON.stringify(next))
      return next
    })
  }, [])

  const cartRestaurant = useMemo(() => (
    restaurants.find((restaurant) => restaurant.id === cart[0]?.restaurantId)
      || (cart[0] ? {
        id: cart[0].restaurantId,
        name: cart[0].restaurantName || 'Your restaurant',
        deliveryFee: null,
      } : null)
  ), [cart])
  const cartTotal = cart.reduce((sum, item) => sum + item.price * item.quantity, 0)
  const cartCount = cart.reduce((sum, item) => sum + item.quantity, 0)
  const value = useMemo(() => ({
    cart,
    cartCount,
    cartTotal,
    cartRestaurant,
    user,
    isCartOpen,
    isAuthOpen,
    toast,
    addItem,
    updateQuantity,
    clearCart,
    syncCart,
    resetCart,
    authenticate,
    signOut,
    updateSessionUser,
    setCartOpen,
    setAuthOpen,
    setToast,
    menuItems,
  }), [cart, cartCount, cartTotal, cartRestaurant, user, isCartOpen, isAuthOpen, toast, addItem, updateQuantity, clearCart, syncCart, resetCart, authenticate, signOut, updateSessionUser])

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>
}

export function useApp() {
  return useContext(AppContext)
}
