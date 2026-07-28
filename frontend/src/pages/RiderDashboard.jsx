import { Bike, CheckCircle2, MapPin, PackageCheck } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../lib/api'
import { useApp } from '../store/AppContext'

export default function RiderDashboard() {
  const { setAuthOpen, setToast, signOut, user } = useApp()
  const [available, setAvailable] = useState([])
  const [deliveries, setDeliveries] = useState([])
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [availableResult, deliveryResult] = await Promise.all([api.riderAvailable(), api.riderDeliveries()])
      setAvailable(Array.isArray(availableResult) ? availableResult : [])
      setDeliveries(Array.isArray(deliveryResult) ? deliveryResult : [])
    } catch (error) {
      setToast(error.message)
    } finally {
      setLoading(false)
    }
  }, [setToast])

  useEffect(() => {
    if (user?.role === 'RIDER') load()
    else setLoading(false)
  }, [load, user?.role])

  async function accept(orderId) {
    try {
      await api.acceptDelivery(orderId)
      setToast(`Delivery #${orderId} accepted.`)
      await load()
    } catch (error) {
      setToast(error.message)
    }
  }

  async function deliver(orderId) {
    try {
      await api.updateOrderStatus(orderId, 'DELIVERED')
      setToast(`Delivery #${orderId} completed.`)
      await load()
    } catch (error) {
      setToast(error.message)
    }
  }

  if (!user || user.role !== 'RIDER') {
    return <main className="role-gate"><Bike size={40} /><h1>Rider workspace</h1><p>Sign in with a rider account to find and manage deliveries.</p>{user ? <button className="primary-button" onClick={signOut}>Sign out</button> : <button className="primary-button" onClick={() => setAuthOpen(true)}>Rider sign in</button>}<Link to="/">Back home</Link></main>
  }
  if (loading) return <main className="owner-loading">Loading deliveries…</main>
  return (
    <main className="operations-page rider-page">
      <header><div><Link to="/" className="wordmark">QuickBite</Link><span>Rider workspace</span></div><button className="secondary-button" onClick={signOut}>Sign out</button></header>
      <section><h1>Deliveries.</h1><p className="operations-intro">Pick up ready orders and keep customers updated through delivery.</p>
        <div className="operations-grid">
          <article className="operations-card"><h2>Ready for pickup <b>{available.length}</b></h2>{available.length === 0 ? <p className="operations-empty">No unassigned orders are ready.</p> : available.map((order) => <div className="delivery-card" key={order.id}><PackageCheck /><div><strong>#{order.id} · {order.restaurantName}</strong><span><MapPin />{order.deliveryAddress}</span><small>₦{Number(order.totalAmount).toLocaleString()}</small></div><button className="primary-button" onClick={() => accept(order.id)}>Accept</button></div>)}</article>
          <article className="operations-card"><h2>My deliveries <b>{deliveries.length}</b></h2>{deliveries.length === 0 ? <p className="operations-empty">Accepted deliveries appear here.</p> : deliveries.map((order) => <div className="delivery-card" key={order.id}><Bike /><div><strong>#{order.id} · {order.restaurantName}</strong><span><MapPin />{order.deliveryAddress}</span><small>{order.orderStatus.replaceAll('_', ' ')}</small></div>{order.orderStatus === 'OUT_FOR_DELIVERY' ? <button className="primary-button" onClick={() => deliver(order.id)}><CheckCircle2 />Delivered</button> : null}</div>)}</article>
        </div>
      </section>
    </main>
  )
}
