import { Bell, ChevronDown, ChevronRight, ClipboardList, Grid2X2, Headphones, LogOut, MenuSquare, Pencil, Plus, RefreshCw, Star, Store, Trash2, X } from 'lucide-react'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../lib/api'
import { useApp } from '../store/AppContext'
import PaginationControls from '../components/PaginationControls'

const cuisineTypes = ['NIGERIAN', 'CHINESE', 'CONTINENTAL', 'FAST_FOOD', 'PIZZA', 'SEAFOOD', 'VEGETARIAN', 'OTHERS']
const statusCopy = {
  PENDING: 'Pending',
  CONFIRMED: 'Confirmed',
  PREPARING: 'Preparing',
  READY_FOR_PICKUP: 'Ready for pickup',
}
const nextStatus = {
  PENDING: 'CONFIRMED',
  CONFIRMED: 'PREPARING',
  PREPARING: 'READY_FOR_PICKUP',
}

function money(value) {
  return `₦${Number(value || 0).toLocaleString()}`
}

function time(value) {
  if (!value) return 'Recently'
  return new Intl.DateTimeFormat('en-NG', { hour: 'numeric', minute: '2-digit' }).format(new Date(value))
}

function OwnerAccessState({ role, onOpenAuth, onSignOut }) {
  return (
    <main className="owner-access">
      <Link to="/" className="wordmark">QuickBite</Link>
      <div>
        <Store size={38} />
        <h1>{role ? 'Restaurant-owner account required' : 'Run your restaurant on QuickBite'}</h1>
        <p>{role ? 'Sign out and use a restaurant-owner account to access this workspace.' : 'Sign in, or create an account and select Restaurant owner as the account type.'}</p>
        {role ? (
          <button className="primary-button" type="button" onClick={onSignOut}>Sign out</button>
        ) : (
          <button className="primary-button" type="button" onClick={onOpenAuth}>Owner sign in</button>
        )}
        <Link className="text-button" to="/">Back to storefront</Link>
      </div>
    </main>
  )
}

function RestaurantSetup({ loading, onCreate }) {
  return (
    <main className="owner-setup">
      <section>
        <Link to="/" className="wordmark">QuickBite</Link>
        <p className="eyebrow">Restaurant setup</p>
        <h1>Open your kitchen.</h1>
        <p>Create your restaurant profile first. You can add menu items and start accepting orders immediately afterward.</p>
        <form className="owner-form" onSubmit={onCreate}>
          <label>Restaurant name<input name="name" required placeholder="e.g. Debbie’s Kitchen" /></label>
          <label>Cuisine type<select name="cuisineType" defaultValue="NIGERIAN">{cuisineTypes.map((type) => <option key={type} value={type}>{type.replaceAll('_', ' ')}</option>)}</select></label>
          <label className="wide">Description<textarea name="description" placeholder="Tell customers what makes your food special." /></label>
          <label className="wide">Restaurant address<input name="address" required placeholder="Street, area, city" /></label>
          <label>Phone number<input name="phoneNumber" required placeholder="0800 000 0000" /></label>
          <label>Logo or image URL<input name="logoURl" type="url" placeholder="https://…" /></label>
          <button className="primary-button wide" disabled={loading} type="submit">{loading ? 'Creating restaurant…' : 'Create restaurant'}</button>
        </form>
      </section>
    </main>
  )
}

export default function OwnerDashboard() {
  const { setAuthOpen, setToast, signOut, user } = useApp()
  const [restaurants, setRestaurants] = useState([])
  const [restaurantId, setRestaurantId] = useState(null)
  const [orders, setOrders] = useState([])
  const [menu, setMenu] = useState([])
  const [selectedId, setSelectedId] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [showMenuForm, setShowMenuForm] = useState(false)
  const [reviews, setReviews] = useState([])
  const [orderFilter, setOrderFilter] = useState('ALL')
  const [editingRestaurant, setEditingRestaurant] = useState(false)
  const [editingMenuItem, setEditingMenuItem] = useState(null)
  const [orderPage, setOrderPage] = useState(0)
  const [orderPageInfo, setOrderPageInfo] = useState({ totalPages: 1 })
  const currentRestaurant = restaurants.find((restaurant) => restaurant.id === restaurantId) || restaurants[0]
  const selected = orders.find((order) => order.id === selectedId) || orders[0]

  const loadRestaurants = useCallback(async () => {
    setLoading(true)
    try {
      const result = await api.ownerRestaurants()
      const next = Array.isArray(result) ? result : []
      setRestaurants(next)
      setRestaurantId((current) => next.some((restaurant) => restaurant.id === current) ? current : next[0]?.id || null)
    } catch (error) {
      setToast(error.message)
    } finally {
      setLoading(false)
    }
  }, [setToast])

  const loadOperations = useCallback(async () => {
    if (!restaurantId) return
    setLoading(true)
    try {
      const [menuResult, orderResult, reviewResult] = await Promise.all([
        api.menu(restaurantId),
        api.restaurantOrdersPage(restaurantId, orderPage),
        api.restaurantReviews(restaurantId),
      ])
      const nextOrders = (orderResult?.content || []).toSorted((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      setMenu(Array.isArray(menuResult) ? menuResult : [])
      setOrders(nextOrders)
      setOrderPageInfo(orderResult || { totalPages: 1 })
      setReviews(Array.isArray(reviewResult) ? reviewResult : [])
      setSelectedId((current) => nextOrders.some((order) => order.id === current) ? current : nextOrders[0]?.id || null)
    } catch (error) {
      setToast(error.message)
    } finally {
      setLoading(false)
    }
  }, [orderPage, restaurantId, setToast])

  useEffect(() => {
    if (user?.role === 'RESTAURANT_OWNER') loadRestaurants()
    else setLoading(false)
  }, [loadRestaurants, user?.role])

  useEffect(() => {
    loadOperations()
  }, [loadOperations])

  const counts = useMemo(() => Object.fromEntries(
    Object.keys(statusCopy).map((status) => [status, orders.filter((order) => order.orderStatus === status).length]),
  ), [orders])
  const visibleOrders = orderFilter === 'ALL' ? orders : orders.filter((order) => order.orderStatus === orderFilter)

  async function createRestaurant(event) {
    event.preventDefault()
    setSaving(true)
    try {
      const created = await api.createRestaurant(Object.fromEntries(new FormData(event.currentTarget)))
      setRestaurants([created])
      setRestaurantId(created.id)
      setToast(`${created.name} is ready for its first menu items.`)
    } catch (error) {
      setToast(error.message)
    } finally {
      setSaving(false)
    }
  }

  async function addMenuItem(event) {
    event.preventDefault()
    const form = event.currentTarget
    const payload = Object.fromEntries(new FormData(form))
    payload.price = Number(payload.price)
    setSaving(true)
    try {
      const created = await api.addMenuItem(restaurantId, payload)
      setMenu((current) => [...current, created])
      setShowMenuForm(false)
      form.reset()
      setToast(`${created.name} was added to the menu.`)
    } catch (error) {
      setToast(error.message)
    } finally {
      setSaving(false)
    }
  }

  async function toggleMenuItem(item) {
    try {
      const updated = await api.toggleMenuItem(item.id)
      setMenu((current) => current.map((entry) => entry.id === item.id ? updated : entry))
    } catch (error) {
      setToast(error.message)
    }
  }

  async function saveMenuItem(event) {
    event.preventDefault()
    const payload = Object.fromEntries(new FormData(event.currentTarget))
    payload.price = Number(payload.price)
    try {
      const updated = await api.updateMenuItem(editingMenuItem.id, payload)
      setMenu((current) => current.map((item) => item.id === updated.id ? updated : item))
      setEditingMenuItem(null)
      setToast('Menu item updated.')
    } catch (error) { setToast(error.message) }
  }

  async function removeMenuItem(item) {
    if (!window.confirm(`Delete ${item.name}?`)) return
    try { await api.deleteMenuItem(item.id); setMenu((current) => current.filter((entry) => entry.id !== item.id)); setToast('Menu item deleted.') } catch (error) { setToast(error.message) }
  }

  async function saveRestaurant(event) {
    event.preventDefault()
    try {
      const updated = await api.updateRestaurant(restaurantId, Object.fromEntries(new FormData(event.currentTarget)))
      setRestaurants((current) => current.map((restaurant) => restaurant.id === updated.id ? updated : restaurant))
      setEditingRestaurant(false)
      setToast('Restaurant details updated.')
    } catch (error) { setToast(error.message) }
  }

  async function toggleRestaurant() {
    try {
      const updated = await api.toggleRestaurant(restaurantId)
      setRestaurants((current) => current.map((restaurant) => restaurant.id === updated.id ? updated : restaurant))
      setToast(`${updated.name} is now ${updated.isOpen ? 'open' : 'closed'}.`)
    } catch (error) {
      setToast(error.message)
    }
  }

  async function advanceOrder() {
    const next = nextStatus[selected?.orderStatus]
    if (!next) return
    try {
      const updated = await api.updateOrderStatus(selected.id, next)
      setOrders((current) => current.map((order) => order.id === updated.id ? updated : order))
      setToast(`Order #${updated.id} is now ${statusCopy[updated.orderStatus]}.`)
    } catch (error) {
      setToast(error.message)
    }
  }

  if (!user || user.role !== 'RESTAURANT_OWNER') {
    return <OwnerAccessState role={user?.role} onOpenAuth={() => setAuthOpen(true)} onSignOut={signOut} />
  }
  if (user.accountStatus !== 'ACTIVE' || user.verificationStatus !== 'VERIFIED') {
    return <main className="role-gate"><Store size={40} /><h1>Finish owner onboarding</h1><p>Complete the mock identity check and wait for administrator approval before operating a restaurant.</p><Link className="primary-button" to="/account">Open onboarding</Link><Link to="/">Back home</Link></main>
  }
  if (loading && restaurants.length === 0) return <main className="owner-loading">Loading owner workspace…</main>
  if (restaurants.length === 0) return <RestaurantSetup loading={saving} onCreate={createRestaurant} />

  return (
    <div className="owner-shell">
      <aside className="owner-sidebar">
        <Link to="/" className="wordmark">QuickBite</Link>
        <nav>
          <a className="active" href="#overview"><Grid2X2 />Overview</a>
          <a href="#orders"><ClipboardList />Orders <b>{orders.length}</b></a>
          <a href="#menu"><MenuSquare />Menu</a>
          <a href="#restaurant"><Store />Restaurant</a>
          <a href="#reviews"><Star />Reviews</a>
        </nav>
        <div className="owner-side-bottom">
          <a href="mailto:support@quickbite.local?subject=QuickBite owner support"><Headphones />Help & support</a>
          <button type="button" onClick={signOut}><LogOut />Log out</button>
        </div>
      </aside>

      <div className="owner-main">
        <header className="owner-topbar">
          <select value={restaurantId || ''} onChange={(event) => setRestaurantId(Number(event.target.value))}>
            {restaurants.map((restaurant) => <option value={restaurant.id} key={restaurant.id}>{restaurant.name}</option>)}
          </select>
          <div><button aria-label="View new-order notifications" type="button" onClick={() => document.getElementById('orders')?.scrollIntoView({ behavior: 'smooth' })}><Bell size={20} /><b>{counts.PENDING}</b></button><span>{user.name?.split(' ').map((part) => part[0]).join('').slice(0, 2)}</span><p><strong>{user.name}</strong><small>Owner</small></p><ChevronDown size={17} /></div>
        </header>

        <div className={selected ? 'owner-content' : 'owner-content no-detail'}>
          <section className="owner-workspace" id="overview">
            <div className="owner-title">
              <div><h1>Today at {currentRestaurant.name}</h1><p>{new Intl.DateTimeFormat('en-NG', { dateStyle: 'full' }).format(new Date())}</p></div>
              <button type="button" onClick={loadOperations}><RefreshCw size={17} />Refresh</button>
            </div>
            <div className="restaurant-live-bar" id="restaurant">
              <div><span className={currentRestaurant.isOpen ? 'live-dot' : 'live-dot closed'} /><strong>{currentRestaurant.verificationStatus === 'PENDING' ? 'Awaiting restaurant approval' : currentRestaurant.verificationStatus === 'REJECTED' ? 'Restaurant verification rejected' : currentRestaurant.isOpen ? 'Open for orders' : 'Closed'}</strong><small>{currentRestaurant.address}</small></div>
              <div className="restaurant-actions"><button className="secondary-button" type="button" onClick={() => setEditingRestaurant((value) => !value)}><Pencil />Edit</button><button className="secondary-button" disabled={currentRestaurant.verificationStatus !== 'VERIFIED'} type="button" onClick={toggleRestaurant}>{currentRestaurant.isOpen ? 'Close restaurant' : 'Open restaurant'}</button></div>
            </div>
            {editingRestaurant ? <form className="owner-form compact restaurant-edit-form" onSubmit={saveRestaurant}><label>Name<input name="name" defaultValue={currentRestaurant.name} required /></label><label>Cuisine<select name="cuisineType" defaultValue={currentRestaurant.cuisineType}>{cuisineTypes.map((type) => <option key={type} value={type}>{type.replaceAll('_', ' ')}</option>)}</select></label><label className="wide">Description<textarea name="description" defaultValue={currentRestaurant.description || ''} /></label><label className="wide">Address<input name="address" defaultValue={currentRestaurant.address} required /></label><label>Phone<input name="phoneNumber" defaultValue={currentRestaurant.phoneNumber} required /></label><label>Image URL<input name="logoURl" type="url" defaultValue={currentRestaurant.logoURL || ''} /></label><button className="primary-button" type="submit">Save restaurant</button></form> : null}
            <div className="status-summary">
              {Object.entries(statusCopy).map(([status, label]) => <div key={status}><span className={`dot ${status.toLowerCase()}`} />{label}<strong>{counts[status]}</strong><small>{status === 'PENDING' ? 'New orders' : status === 'CONFIRMED' ? 'Accepted' : status === 'PREPARING' ? 'In the kitchen' : 'Awaiting pickup'}</small></div>)}
            </div>

            <section className="order-queue" id="orders">
              <div className="queue-head"><h2>Order queue <b>{visibleOrders.length}</b></h2><label className="order-filter">Status<select value={orderFilter} onChange={(event) => setOrderFilter(event.target.value)}><option value="ALL">All orders</option>{Object.entries(statusCopy).map(([value, label]) => <option value={value} key={value}>{label}</option>)}</select><ChevronDown size={15} /></label></div>
              <div className="order-table">
                <div className="order-table-head"><span>Order</span><span>Customer</span><span>Items</span><span>Total</span><span>Status</span><span>Time</span><span>Action</span></div>
                {visibleOrders.length === 0 ? <p className="owner-empty-row">No orders match this view.</p> : visibleOrders.map((order) => (
                  <button className={selected?.id === order.id ? 'order-table-row selected' : 'order-table-row'} type="button" key={order.id} onClick={() => setSelectedId(order.id)}>
                    <span>#QB-{order.id}</span><span>{order.customerName}</span><span>{order.orderItems?.length || 0} items</span><span>{money(order.totalAmount)}</span><span><i className={`status-chip ${order.orderStatus.toLowerCase()}`}>{statusCopy[order.orderStatus] || order.orderStatus.replaceAll('_', ' ')}</i></span><span>{time(order.createdAt)}</span><span><ChevronRight size={17} /></span>
                  </button>
                ))}
              </div>
              <PaginationControls page={orderPage} totalPages={orderPageInfo.totalPages} onPageChange={setOrderPage} label="restaurant orders" />
            </section>

            <section className="owner-menu-panel" id="menu">
              <div className="owner-panel-heading"><div><h2>Menu</h2><p>Manage what customers can order today.</p></div><button className="primary-button" type="button" onClick={() => setShowMenuForm((show) => !show)}><Plus size={17} />Add item</button></div>
              {showMenuForm ? (
                <form className="owner-form compact" onSubmit={addMenuItem}>
                  <label>Item name<input name="name" required /></label><label>Category<input name="category" required /></label>
                  <label>Price<input name="price" type="number" min="1" step="0.01" required /></label><label>Image URL<input name="imageURL" type="url" /></label>
                  <label className="wide">Description<textarea name="description" /></label>
                  <button className="primary-button" disabled={saving} type="submit">{saving ? 'Saving…' : 'Save menu item'}</button>
                </form>
              ) : null}
              {editingMenuItem ? <form className="owner-form compact" onSubmit={saveMenuItem}><label>Item name<input name="name" defaultValue={editingMenuItem.name} required /></label><label>Category<input name="category" defaultValue={editingMenuItem.category} required /></label><label>Price<input name="price" type="number" min="1" step="0.01" defaultValue={editingMenuItem.price} required /></label><label>Image URL<input name="imageURL" type="url" defaultValue={editingMenuItem.imageURL || ''} /></label><label className="wide">Description<textarea name="description" defaultValue={editingMenuItem.description || ''} /></label><div className="form-actions"><button className="primary-button" type="submit">Update item</button><button className="secondary-button" type="button" onClick={() => setEditingMenuItem(null)}>Cancel</button></div></form> : null}
              <div className="owner-menu-list">
                {menu.length === 0 ? <p className="owner-empty-row">Your menu is empty. Add the first item to make this restaurant orderable.</p> : menu.map((item) => (
                  <article key={item.id}><img src={item.imageURL || currentRestaurant.logoURL || '/assets/hero-jollof.png'} alt="" /><div><strong>{item.name}</strong><span>{item.category} · {money(item.price)}</span></div><button className={item.isAvailable ? 'menu-toggle available' : 'menu-toggle'} type="button" onClick={() => toggleMenuItem(item)}>{item.isAvailable ? 'Available' : 'Hidden'}</button></article>
                ))}
              </div>
              {menu.length > 0 ? <div className="owner-menu-management"><strong>Edit or delete menu items</strong>{menu.map((item) => <div key={item.id}><span>{item.name}</span><button type="button" onClick={() => setEditingMenuItem(item)}><Pencil />Edit</button><button className="danger-text" type="button" onClick={() => removeMenuItem(item)}><Trash2 />Delete</button></div>)}</div> : null}
            </section>
            <section className="owner-menu-panel owner-reviews" id="reviews"><div className="owner-panel-heading"><div><h2>Customer reviews</h2><p>Feedback from completed deliveries.</p></div><span>{reviews.length} total</span></div>{reviews.length === 0 ? <p className="owner-empty-row">No reviews yet.</p> : reviews.map((review) => <article key={review.id}><div><strong>{review.customerName}</strong><span>{review.rating}/5 stars</span></div><p>{review.comment || 'No written comment.'}</p></article>)}</section>
          </section>

          {selected ? (
            <aside className="owner-order-detail">
              <div className="owner-detail-head"><div><h2>Order #QB-{selected.id}</h2><i className={`status-chip ${selected.orderStatus.toLowerCase()}`}>{statusCopy[selected.orderStatus] || selected.orderStatus}</i><p>Placed at {time(selected.createdAt)}</p></div><button type="button" onClick={() => setSelectedId(null)}><X size={19} /></button></div>
              <div className="customer-block"><span>{selected.customerName?.split(' ').map((part) => part[0]).join('').slice(0, 2)}</span><div><strong>{selected.customerName}</strong><small>QuickBite customer</small></div></div>
              <div className="detail-section"><h3>Delivery address</h3><p>{selected.deliveryAddress}</p></div>
              <div className="detail-section order-items"><h3>Items ({selected.orderItems?.length || 0})</h3>{selected.orderItems?.map((item) => <div key={item.id}><img src={item.imageURL || currentRestaurant?.logoURL || '/assets/hero-jollof.png'} alt="" /><p><strong>{item.itemName}</strong><span>x {item.quantity}</span></p><b>{money(item.subTotal)}</b></div>)}</div>
              <div className="detail-totals"><p><span>Subtotal</span><b>{money(selected.subTotal)}</b></p><p><span>Delivery fee</span><b>{money(selected.deliveryFee)}</b></p><p><strong>Total</strong><strong>{money(selected.totalAmount)}</strong></p></div>
              <div className="detail-section"><h3>Payment method</h3><p>{selected.paymentMethod === 'PAYSTACK' ? 'Paystack' : 'Cash on delivery'}</p></div>
              <button className="primary-button full-button" type="button" disabled={!nextStatus[selected.orderStatus]} onClick={advanceOrder}>
                {selected.orderStatus === 'PENDING' ? 'Confirm order' : selected.orderStatus === 'CONFIRMED' ? 'Start preparing' : selected.orderStatus === 'PREPARING' ? 'Mark ready for pickup' : 'Waiting for rider'}
              </button>
            </aside>
          ) : null}
        </div>
      </div>
    </div>
  )
}
