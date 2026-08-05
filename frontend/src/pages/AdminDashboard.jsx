import { Ban, CheckCircle2, ClipboardList, RotateCcw, ShieldCheck, Store, Trash2, Users } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../lib/api'
import { useApp } from '../store/AppContext'
import PaginationControls from '../components/PaginationControls'

const emptyData = { dashboard: null, users: [], restaurants: [], orders: [], reviews: [], pending: [], pendingRestaurants: [], pages: {} }

export default function AdminDashboard() {
  const { setAuthOpen, setToast, signOut, user } = useApp()
  const [data, setData] = useState(emptyData)
  const [loading, setLoading] = useState(true)
  const [working, setWorking] = useState('')
  const [pages, setPages] = useState({ providers: 0, restaurantApprovals: 0, users: 0, restaurants: 0, orders: 0, reviews: 0 })
  const isAdmin = user?.role === 'ADMIN'

  const load = useCallback(async () => {
    if (!isAdmin) return
    setLoading(true)
    try {
      const [dashboard, users, restaurants, orders, reviews, pending, pendingRestaurants] = await Promise.all([
        api.adminDashboard(), api.adminUsersPage(pages.users), api.adminRestaurantsPage(pages.restaurants), api.adminOrdersPage(pages.orders), api.adminReviewsPage(pages.reviews), api.pendingProvidersPage(pages.providers), api.pendingRestaurantsPage(pages.restaurantApprovals),
      ])
      setData({ dashboard, users: users.content, restaurants: restaurants.content, orders: orders.content, reviews: reviews.content, pending: pending.content, pendingRestaurants: pendingRestaurants.content, pages: { users, restaurants, orders, reviews, providers: pending, restaurantApprovals: pendingRestaurants } })
    } catch (error) {
      setToast(error.message)
    } finally {
      setLoading(false)
    }
  }, [isAdmin, pages, setToast])

  const changePage = useCallback((key, page) => setPages((current) => ({ ...current, [key]: page })), [])

  useEffect(() => { load() }, [load])

  async function act(key, action, message) {
    setWorking(key)
    try {
      await action()
      await load()
      setToast(message)
    } catch (error) {
      setToast(error.message)
    } finally {
      setWorking('')
    }
  }

  if (!isAdmin) return <main className="role-gate"><ShieldCheck /><h1>Admin access</h1><p>Sign in with an administrator account to manage QuickBite.</p>{user ? <button className="primary-button" onClick={signOut}>Sign out</button> : <button className="primary-button" onClick={() => setAuthOpen(true)}>Admin sign in</button>}<Link to="/">Back home</Link></main>
  if (loading && !data.dashboard) return <main className="owner-loading">Loading platform dashboard…</main>

  const stats = [
    ['Users', data.dashboard?.totalUsers, <Users />], ['Restaurants', data.dashboard?.totalRestaurants, <Store />],
    ['Orders', data.dashboard?.totalOrders, <ClipboardList />], ['Delivered', data.dashboard?.totalDeliveredOrders, <ShieldCheck />],
  ]
  return (
    <main className="operations-page admin-page">
      <header><div><Link to="/" className="wordmark">QuickBite</Link><span>Platform administration</span></div><button className="secondary-button" onClick={signOut}>Sign out</button></header>
      <section>
        <div className="admin-title"><div><h1>Platform control.</h1><p>Approve providers, moderate content and resolve active orders.</p></div><button className="secondary-button" onClick={load}><RotateCcw />Refresh</button></div>
        <div className="operations-stats">{stats.map(([label, value, icon]) => <article key={label}>{icon}<span>{label}</span><strong>{value || 0}</strong></article>)}</div>
        <div className="operations-grid">
          <article className="operations-card wide"><h2>Provider approvals <b>{data.pages.providers?.totalElements || 0}</b></h2>{data.pending.length === 0 ? <p className="operations-empty">No verified providers are waiting for approval.</p> : data.pending.map((entry) => <div className="operations-row admin-row" key={entry.id}><span><strong>{entry.name}</strong><small>{entry.email} · {entry.role.replaceAll('_', ' ')}</small></span><i>{entry.verificationStatus}</i><button disabled={working === `approve-${entry.id}`} onClick={() => act(`approve-${entry.id}`, () => api.approveProvider(entry.id), 'Provider approved.')}><CheckCircle2 />Approve</button></div>)}<PaginationControls page={pages.providers} totalPages={data.pages.providers?.totalPages} onPageChange={(page) => changePage('providers', page)} label="provider approvals" /></article>
          <article className="operations-card wide"><h2>Restaurant approvals <b>{data.pages.restaurantApprovals?.totalElements || 0}</b></h2>{data.pendingRestaurants.length === 0 ? <p className="operations-empty">No restaurants are waiting for approval.</p> : data.pendingRestaurants.map((entry) => <div className="operations-row admin-row" key={entry.id}><span><strong>{entry.name}</strong><small>{entry.ownerName} · {entry.address}</small></span><button onClick={() => act(`approve-restaurant-${entry.id}`, () => api.approveRestaurant(entry.id), 'Restaurant approved.')}><CheckCircle2 />Approve</button><button className="danger-text" onClick={() => act(`reject-restaurant-${entry.id}`, () => api.rejectRestaurant(entry.id), 'Restaurant rejected.')}><Ban />Reject</button></div>)}<PaginationControls page={pages.restaurantApprovals} totalPages={data.pages.restaurantApprovals?.totalPages} onPageChange={(page) => changePage('restaurantApprovals', page)} label="restaurant approvals" /></article>
          <article className="operations-card"><h2>User moderation</h2>{data.users.map((entry) => <div className="operations-row admin-row" key={entry.id}><span><strong>{entry.name}</strong><small>{entry.email}</small></span><i>{entry.accountStatus || 'ACTIVE'}</i>{entry.role !== 'ADMIN' ? entry.accountStatus === 'SUSPENDED' ? <button onClick={() => act(`user-${entry.id}`, () => api.reactivateUser(entry.id), 'User reactivated.')}><RotateCcw />Reactivate</button> : <button className="danger-text" onClick={() => act(`user-${entry.id}`, () => api.suspendUser(entry.id), 'User suspended.')}><Ban />Suspend</button> : null}</div>)}<PaginationControls page={pages.users} totalPages={data.pages.users?.totalPages} onPageChange={(page) => changePage('users', page)} label="users" /></article>
          <article className="operations-card"><h2>Restaurant moderation</h2>{data.restaurants.map((entry) => <div className="operations-row admin-row" key={entry.id}><span><strong>{entry.name}</strong><small>{entry.ownerName}</small></span><i>{entry.isOpen ? 'Open' : 'Closed'}</i><button className="danger-text" onClick={() => window.confirm(`Remove ${entry.name}?`) && act(`restaurant-${entry.id}`, () => api.adminDeleteRestaurant(entry.id), 'Restaurant removed.')}><Trash2 />Remove</button></div>)}<PaginationControls page={pages.restaurants} totalPages={data.pages.restaurants?.totalPages} onPageChange={(page) => changePage('restaurants', page)} label="restaurants" /></article>
          <article className="operations-card wide"><h2>Order resolution</h2>{data.orders.map((entry) => <div className="operations-row admin-row" key={entry.id}><span><strong>#QB-{entry.id} · {entry.restaurantName}</strong><small>{entry.customerName} · ₦{Number(entry.totalAmount || 0).toLocaleString()}</small></span><i>{entry.orderStatus.replaceAll('_', ' ')}</i>{entry.orderStatus === 'OUT_FOR_DELIVERY' ? <button onClick={() => act(`order-${entry.id}`, () => api.resolveOrder(entry.id, 'DELIVERED'), 'Order resolved as delivered.')}><CheckCircle2 />Mark delivered</button> : !['DELIVERED', 'CANCELLED'].includes(entry.orderStatus) ? <button className="danger-text" onClick={() => act(`order-${entry.id}`, () => api.resolveOrder(entry.id, 'CANCELLED'), 'Order cancelled by administration.')}><Ban />Cancel</button> : null}</div>)}<PaginationControls page={pages.orders} totalPages={data.pages.orders?.totalPages} onPageChange={(page) => changePage('orders', page)} label="orders" /></article>
          <article className="operations-card wide"><h2>Review moderation</h2>{data.reviews.length === 0 ? <p className="operations-empty">No reviews to moderate.</p> : data.reviews.map((entry) => <div className="operations-row admin-row" key={entry.id}><span><strong>{entry.customerName} · {entry.rating}/5</strong><small>{entry.restaurantName} · {entry.comment || 'No comment'}</small></span><button className="danger-text" onClick={() => window.confirm('Remove this review?') && act(`review-${entry.id}`, () => api.adminDeleteReview(entry.id), 'Review removed.')}><Trash2 />Remove</button></div>)}<PaginationControls page={pages.reviews} totalPages={data.pages.reviews?.totalPages} onPageChange={(page) => changePage('reviews', page)} label="reviews" /></article>
        </div>
      </section>
    </main>
  )
}
