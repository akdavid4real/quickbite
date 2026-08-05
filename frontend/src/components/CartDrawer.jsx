import { ArrowRight, Minus, Plus, ShoppingBag, X } from 'lucide-react'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../lib/api'
import { useApp } from '../store/AppContext'

export default function CartDrawer() {
  const {
    cart,
    cartCount,
    cartTotal,
    cartRestaurant,
    isCartOpen,
    resetCart,
    setAuthOpen,
    setCartOpen,
    setToast,
    syncCart,
    updateQuantity,
    user,
  } = useApp()
  const [checkout, setCheckout] = useState(false)
  const [placingOrder, setPlacingOrder] = useState(false)
  const [savedAddresses, setSavedAddresses] = useState([])
  const navigate = useNavigate()
  const deliveryFee = cartRestaurant?.deliveryFee

  async function beginCheckout() {
    if (!user) {
      setCartOpen(false)
      setAuthOpen(true)
      setToast('Sign in or create an account to continue to checkout.')
      return
    }
    if (user.role !== 'CUSTOMER') {
      setToast('Please sign in with a customer account to place an order.')
      return
    }
    setCheckout(true)
    try {
      const result = await api.savedAddresses()
      setSavedAddresses(Array.isArray(result) ? result : [])
    } catch {
      setSavedAddresses([])
    }
  }

  async function placeOrder(event) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const paymentMethod = form.get('paymentMethod')
    setPlacingOrder(true)

    try {
      await syncCart()
      const order = await api.placeOrder({
        deliveryAddress: form.get('deliveryAddress'),
        paymentMethod,
      })
      resetCart()
      setCheckout(false)
      setCartOpen(false)

      if (paymentMethod === 'PAYSTACK') {
        try {
          const payment = await api.initializePayment(order.id)
          if (payment.paymentURL) {
            window.location.assign(payment.paymentURL)
            return
          }
        } catch (error) {
          setToast(`Order #${order.id} was created, but payment could not start: ${error.message}`)
          navigate('/orders')
          return
        }
      }

      setToast(`Order #${order.id} placed successfully.`)
      navigate('/orders')
    } catch (error) {
      setToast(error.message)
    } finally {
      setPlacingOrder(false)
    }
  }

  return (
    <div className={isCartOpen ? 'drawer-layer is-open' : 'drawer-layer'} aria-hidden={!isCartOpen}>
      <button className="drawer-scrim" type="button" aria-label="Close cart" onClick={() => setCartOpen(false)} />
      <aside className="cart-drawer" aria-label="Shopping cart">
        <div className="drawer-header">
          <div>
            <p>Your cart</p>
            <span>{cartCount} {cartCount === 1 ? 'item' : 'items'} · {cartRestaurant?.name || 'QuickBite'}</span>
          </div>
          <button className="square-button" type="button" onClick={() => setCartOpen(false)}><X size={20} /></button>
        </div>

        {cart.length === 0 ? (
          <div className="empty-cart">
            <ShoppingBag size={32} />
            <h3>Your cart is waiting</h3>
            <p>Add something delicious and it will show up here.</p>
            <button className="primary-button" type="button" onClick={() => setCartOpen(false)}>Browse restaurants</button>
          </div>
        ) : (
          <>
            <div className="cart-list">
              {cart.map((item) => (
                <article className="cart-item" key={item.id}>
                  <img src={item.imageURL} alt="" />
                  <div>
                    <h3>{item.name}</h3>
                    <p>₦{item.price.toLocaleString()}</p>
                    <div className="quantity-control">
                      <button type="button" aria-label={`Decrease ${item.name}`} onClick={() => updateQuantity(item.id, item.quantity - 1)}><Minus size={15} /></button>
                      <span>{item.quantity}</span>
                      <button type="button" aria-label={`Increase ${item.name}`} onClick={() => updateQuantity(item.id, item.quantity + 1)}><Plus size={15} /></button>
                    </div>
                  </div>
                </article>
              ))}
            </div>
            {checkout ? (
              <form className="checkout-form" onSubmit={placeOrder}>
                {savedAddresses.length > 0 ? (
                  <label>Delivery address<select name="deliveryAddress" defaultValue={savedAddresses.find((address) => address.isDefault)?.address || savedAddresses[0].address}>{savedAddresses.map((address) => <option value={address.address} key={address.id}>{address.label} · {address.address}</option>)}</select></label>
                ) : <label>Delivery address<input name="deliveryAddress" defaultValue={user?.address || ''} required minLength={8} placeholder="Street, area, Lagos" /></label>}
                <label>Payment method<select name="paymentMethod" defaultValue="CASH_ON_DELIVERY"><option value="CASH_ON_DELIVERY">Cash on delivery</option><option value="PAYSTACK">Pay with Paystack</option></select></label>
                <p className="delivery-note">The exact delivery fee is calculated from this address when your order is placed.</p>
                <button className="primary-button full-button" disabled={placingOrder} type="submit">{placingOrder ? 'Placing order…' : 'Place order'}</button>
              </form>
            ) : (
              <div className="cart-summary">
                <div><span>Subtotal</span><strong>₦{cartTotal.toLocaleString()}</strong></div>
                <div><span>Delivery</span><strong>{typeof deliveryFee === 'number' ? `₦${deliveryFee.toLocaleString()}` : 'Calculated at checkout'}</strong></div>
                <div className="cart-total"><span>{typeof deliveryFee === 'number' ? 'Estimated total' : 'Subtotal before delivery'}</span><strong>₦{(cartTotal + (deliveryFee || 0)).toLocaleString()}</strong></div>
                <button className="primary-button full-button" type="button" onClick={beginCheckout}>Continue to checkout <ArrowRight size={18} /></button>
              </div>
            )}
          </>
        )}
      </aside>
    </div>
  )
}
