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

const pageItems = (promise) => promise.then((result) => Array.isArray(result) ? result : result?.content || [])
const pageResult = (promise) => promise.then((result) => Array.isArray(result)
  ? { content: result, page: 0, size: result.length, totalElements: result.length, totalPages: 1 }
  : result)
const pageQuery = (page = 0, size = 10, extra = {}) => new URLSearchParams({ page, size, ...extra }).toString()

export const api = {
  login: (payload) => request('/auth/login', { method: 'POST', body: JSON.stringify(payload) }),
  register: (payload) => request('/auth/register', { method: 'POST', body: JSON.stringify(payload) }),
  profile: () => request('/profile'),
  updateProfile: (payload) => request('/profile', { method: 'PUT', body: JSON.stringify(payload) }),
  savedAddresses: () => request('/profile/addresses'),
  createAddress: (payload) => request('/profile/addresses', { method: 'POST', body: JSON.stringify(payload) }),
  updateAddress: (id, payload) => request(`/profile/addresses/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteAddress: (id) => request(`/profile/addresses/${id}`, { method: 'DELETE' }),
  providerVerification: () => request('/provider-verification'),
  submitProviderVerification: (payload) => request('/provider-verification', { method: 'POST', body: JSON.stringify(payload) }),
  restaurants: () => pageItems(request('/restaurants')),
  restaurantsPage: (page = 0, size = 10) => pageResult(request(`/restaurants?${pageQuery(page, size)}`)),
  searchRestaurants: (name) => pageItems(request(`/restaurants/search?name=${encodeURIComponent(name)}`)),
  restaurantsByCuisine: (cuisineType) => pageItems(request(`/restaurants/cuisine/${cuisineType}`)),
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
  myOrders: () => pageItems(request('/orders/my-orders')),
  myOrdersPage: (page = 0, size = 10) => pageResult(request(`/orders/my-orders?${pageQuery(page, size)}`)),
  cancelOrder: (orderId) => request(`/orders/${orderId}/cancel`, { method: 'PATCH' }),
  initializePayment: (orderId) => request('/payments/initialize_payment', { method: 'POST', body: JSON.stringify({ orderId }) }),
  restaurantReviews: (restaurantId) => pageItems(request(`/reviews/restaurant/${restaurantId}`)),
  myReviews: () => pageItems(request('/reviews')),
  createReview: (payload) => request('/reviews', { method: 'POST', body: JSON.stringify(payload) }),
  deleteReview: (reviewId) => request(`/reviews/${reviewId}`, { method: 'DELETE' }),
  ownerRestaurants: () => pageItems(request('/restaurants/my-restaurants')),
  restaurantOrders: (id) => pageItems(request(`/orders/restaurant/${id}`)),
  restaurantOrdersPage: (id, page = 0, size = 10) => pageResult(request(`/orders/restaurant/${id}?${pageQuery(page, size)}`)),
  updateOrderStatus: (id, orderStatus) => request(`/orders/${id}/status?orderStatus=${orderStatus}`, { method: 'PATCH' }),
  adminDashboard: () => request('/admin/dashboard'),
  adminUsers: (role = '') => pageItems(request(`/admin/users${role ? `?role=${role}` : ''}`)),
  adminUsersPage: (page = 0, size = 10, role = '') => pageResult(request(`/admin/users?${pageQuery(page, size, role ? { role } : {})}`)),
  adminRestaurants: () => pageItems(request('/admin/restaurants')),
  adminRestaurantsPage: (page = 0, size = 10) => pageResult(request(`/admin/restaurants?${pageQuery(page, size)}`)),
  adminOrders: (status = '') => pageItems(request(`/admin/orders${status ? `?orderStatus=${status}` : ''}`)),
  adminOrdersPage: (page = 0, size = 10, status = '') => pageResult(request(`/admin/orders?${pageQuery(page, size, status ? { orderStatus: status } : {})}`)),
  adminReviews: () => pageItems(request('/admin/reviews')),
  adminReviewsPage: (page = 0, size = 10) => pageResult(request(`/admin/reviews?${pageQuery(page, size)}`)),
  pendingProvidersPage: (page = 0, size = 10) => pageResult(request(`/admin/providers/pending-approval?${pageQuery(page, size)}`)),
  pendingRestaurantsPage: (page = 0, size = 10) => pageResult(request(`/admin/restaurants/pending-approval?${pageQuery(page, size)}`)),
  approveProvider: (id) => request(`/admin/users/${id}/approve-provider`, { method: 'PATCH' }),
  approveRestaurant: (id) => request(`/admin/restaurants/${id}/approve`, { method: 'PATCH' }),
  rejectRestaurant: (id) => request(`/admin/restaurants/${id}/reject`, { method: 'PATCH' }),
  suspendUser: (id) => request(`/admin/users/${id}/suspend`, { method: 'PATCH' }),
  reactivateUser: (id) => request(`/admin/users/${id}/reactivate`, { method: 'PATCH' }),
  adminDeleteRestaurant: (id) => request(`/admin/restaurants/${id}`, { method: 'DELETE' }),
  adminDeleteReview: (id) => request(`/admin/reviews/${id}`, { method: 'DELETE' }),
  resolveOrder: (id, orderStatus) => request(`/admin/orders/${id}/resolve?orderStatus=${orderStatus}`, { method: 'PATCH' }),
  riderAvailable: () => pageItems(request('/orders/available-deliveries')),
  riderAvailablePage: (page = 0, size = 10) => pageResult(request(`/orders/available-deliveries?${pageQuery(page, size)}`)),
  riderDeliveries: () => pageItems(request('/orders/my-deliveries')),
  riderDeliveriesPage: (page = 0, size = 10) => pageResult(request(`/orders/my-deliveries?${pageQuery(page, size)}`)),
  acceptDelivery: (orderId) => request(`/orders/${orderId}/assign-rider`, { method: 'PATCH' }),
  riderSummary: () => request('/orders/rider/summary'),
  setRiderAvailability: (available) => request(`/orders/rider/availability?available=${available}`, { method: 'PATCH' }),
  submitDeliveryProof: (orderId, payload) => request(`/orders/${orderId}/delivery-proof`, { method: 'POST', body: JSON.stringify(payload) }),
}
