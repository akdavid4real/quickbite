import { ClipboardList, ShieldCheck, Store, Users } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../lib/api'
import { useApp } from '../store/AppContext'

export default function AdminDashboard() {
  const { setAuthOpen, setToast, signOut, user } = useApp()
  const [data, setData] = useState({ dashboard: null, users: [], restaurants: [], orders: [], reviews: [] })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (user?.role !== 'ADMIN') {
      setLoading(false)
      return
    }
    let active = true
    Promise.all([api.adminDashboard(), api.adminUsers(), api.adminRestaurants(), api.adminOrders(), api.adminReviews()])
      .then(([dashboard, users, restaurants, orders, reviews]) => {
        if (active) setData({ dashboard, users, restaurants, orders, reviews })
      })
      .catch((error) => setToast(error.message))
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [setToast, user?.role])

  if (!user || user.role !== 'ADMIN') {
    return <main className="role-gate"><ShieldCheck size={40} /><h1>Admin access</h1><p>Sign in with an administrator account to manage QuickBite.</p>{user ? <button className="primary-button" onClick={signOut}>Sign out</button> : <button className="primary-button" onClick={() => setAuthOpen(true)}>Admin sign in</button>}<Link to="/">Back home</Link></main>
  }
  if (loading) return <main className="owner-loading">Loading platform dashboard…</main>
  const stats = [
    ['Users', data.dashboard?.totalUsers, <Users />],
    ['Restaurants', data.dashboard?.totalRestaurants, <Store />],
    ['Orders', data.dashboard?.totalOrders, <ClipboardList />],
    ['Delivered', data.dashboard?.totalDeliveredOrders, <ShieldCheck />],
  ]
  return (
    <main className="operations-page">
      <header><div><Link to="/" className="wordmark">QuickBite</Link><span>Platform administration</span></div><button className="secondary-button" onClick={signOut}>Sign out</button></header>
      <section>
        <h1>Platform overview.</h1>
        <div className="operations-stats">{stats.map(([label, value, icon]) => <article key={label}>{icon}<span>{label}</span><strong>{value || 0}</strong></article>)}</div>
        <div className="operations-grid">
          <article className="operations-card"><h2>Recent users</h2>{data.users.slice(0, 8).map((entry) => <div className="operations-row" key={entry.id}><span><strong>{entry.name}</strong><small>{entry.email}</small></span><i>{entry.role.replaceAll('_', ' ')}</i></div>)}</article>
          <article className="operations-card"><h2>Restaurants</h2>{data.restaurants.slice(0, 8).map((entry) => <div className="operations-row" key={entry.id}><span><strong>{entry.name}</strong><small>{entry.ownerName}</small></span><i>{entry.isOpen ? 'Open' : 'Closed'}</i></div>)}</article>
          <article className="operations-card wide"><h2>Latest orders</h2>{data.orders.slice(0, 10).map((entry) => <div className="operations-row" key={entry.id}><span><strong>#QB-{entry.id} · {entry.restaurantName}</strong><small>{entry.customerName}</small></span><i>{entry.orderStatus.replaceAll('_', ' ')}</i><b>₦{Number(entry.totalAmount || 0).toLocaleString()}</b></div>)}</article>
        </div>
      </section>
    </main>
  )
}
