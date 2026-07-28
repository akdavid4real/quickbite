const configuredApiHost = import.meta.env.VITE_API_BASE_URL?.trim()
const API_BASE_URL = configuredApiHost
  ? configuredApiHost.startsWith('http')
    ? `${configuredApiHost.replace(/\/$/, '')}${configuredApiHost.endsWith('/api') ? '' : '/api'}`
    : `https://${configuredApiHost.replace(/\/$/, '')}/api`
  : '/api'
export const TOKEN_KEY = 'quickbite_token'

async function request(path, options = {}) {
  const token = localStorage.getItem(TOKEN_KEY)
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({}))
    const validationMessage = error.errors
      ? Object.values(error.errors).filter(Boolean).join('. ')
      : ''
    throw new Error(validationMessage || error.message || `Request failed (${response.status})`)
  }

  if (response.status === 204) return null
  const body = await response.json()
  return body.data ?? body
}

export const api = {
  login: (payload) => request('/auth/login', { method: 'POST', body: JSON.stringify(payload) }),
  register: (payload) => request('/auth/register', { method: 'POST', body: JSON.stringify(payload) }),
  restaurants: () => request('/restaurants'),
  searchRestaurants: (name) => request(`/restaurants/search?name=${encodeURIComponent(name)}`),
  restaurantsByCuisine: (cuisineType) => request(`/restaurants/cuisine/${cuisineType}`),
  restaurant: (id) => request(`/restaurants/${id}`),
  createRestaurant: (payload) => request('/restaurants', { method: 'POST', body: JSON.stringify(payload) }),
  updateRestaurant: (id, payload) => request(`/restaurants/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  toggleRestaurant: (id) => request(`/restaurants/${id}/openOrClose`, { method: 'PATCH' }),
  menu: (id) => request(`/menu/${id}/all`),
  addMenuItem: (restaurantId, payload) => request(`/menu/${restaurantId}`, { method: 'POST', body: JSON.stringify(payload) }),
  updateMenuItem: (menuItemId, payload) => request(`/menu/${menuItemId}`, { method: 'PUT', body: JSON.stringify(payload) }),
  toggleMenuItem: (menuItemId) => request(`/menu/${menuItemId}/hideOrShow`, { method: 'PATCH' }),
  deleteMenuItem: (menuItemId) => request(`/menu/${menuItemId}`, { method: 'DELETE' }),
  cart: () => request('/cart'),
  addToCart: (payload) => request('/cart/add', { method: 'POST', body: JSON.stringify(payload) }),
  updateCartItem: (cartItemId, quantity) => request(`/cart/item/${cartItemId}`, { method: 'PUT', body: JSON.stringify({ quantity }) }),
  clearCart: () => request('/cart', { method: 'DELETE' }),
  placeOrder: (payload) => request('/orders', { method: 'POST', body: JSON.stringify(payload) }),
  myOrders: () => request('/orders/my-orders'),
  initializePayment: (orderId) => request('/payments/initialize_payment', { method: 'POST', body: JSON.stringify({ orderId }) }),
  ownerRestaurants: () => request('/restaurants/my-restaurants'),
  restaurantOrders: (id) => request(`/orders/restaurant/${id}`),
  updateOrderStatus: (id, orderStatus) => request(`/orders/${id}/status?orderStatus=${orderStatus}`, { method: 'PATCH' }),
  adminDashboard: () => request('/admin/dashboard'),
  adminUsers: (role = '') => request(`/admin/users${role ? `?role=${role}` : ''}`),
  adminRestaurants: () => request('/admin/restaurants'),
  adminOrders: (status = '') => request(`/admin/orders${status ? `?orderStatus=${status}` : ''}`),
  adminReviews: () => request('/admin/reviews'),
  riderAvailable: () => request('/orders/available-deliveries'),
  riderDeliveries: () => request('/orders/my-deliveries'),
  acceptDelivery: (orderId) => request(`/orders/${orderId}/assign-rider`, { method: 'PATCH' }),
}
