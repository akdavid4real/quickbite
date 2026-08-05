import { Bike, Camera, CheckCircle2, ExternalLink, MapPin, PackageCheck, Phone } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../lib/api'
import { useApp } from '../store/AppContext'
import PaginationControls from '../components/PaginationControls'

function DeliveryProofForm({ orderId, onComplete, onToast }) {
  const [open, setOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  async function submit(event) {
    event.preventDefault()
    setSaving(true)
    try {
      const updated = await api.submitDeliveryProof(orderId, Object.fromEntries(new FormData(event.currentTarget)))
      onComplete(updated)
      onToast('Delivery completed with evidence.')
    } catch (error) { onToast(error.message) } finally { setSaving(false) }
  }
  if (!open) return <button className="primary-button" onClick={() => setOpen(true)}><Camera />Complete</button>
  return <form className="delivery-proof-form" onSubmit={submit}><input name="evidenceUrl" type="url" placeholder="Evidence photo URL" required /><input name="notes" placeholder="Delivery notes" /><button disabled={saving} type="submit">{saving ? 'Saving…' : 'Submit proof'}</button></form>
}

export default function RiderDashboard() {
  const { setAuthOpen, setToast, signOut, user } = useApp()
  const [available, setAvailable] = useState([])
  const [deliveries, setDeliveries] = useState([])
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [availablePage, setAvailablePage] = useState(0)
  const [deliveryPage, setDeliveryPage] = useState(0)
  const [availablePageInfo, setAvailablePageInfo] = useState({ totalPages: 1 })
  const [deliveryPageInfo, setDeliveryPageInfo] = useState({ totalPages: 1 })
  const canOperate = user?.role === 'RIDER' && user.accountStatus === 'ACTIVE' && user.verificationStatus === 'VERIFIED'

  const load = useCallback(async () => {
    if (!canOperate) return
    setLoading(true)
    try {
      const [availableResult, deliveryResult, summaryResult] = await Promise.all([api.riderAvailablePage(availablePage), api.riderDeliveriesPage(deliveryPage), api.riderSummary()])
      setAvailable(availableResult?.content || []); setDeliveries(deliveryResult?.content || []); setSummary(summaryResult)
      setAvailablePageInfo(availableResult || { totalPages: 1 }); setDeliveryPageInfo(deliveryResult || { totalPages: 1 })
    } catch (error) { setToast(error.message) } finally { setLoading(false) }
  }, [availablePage, canOperate, deliveryPage, setToast])
  useEffect(() => { load() }, [load])

  async function toggleAvailability() {
    try { setSummary(await api.setRiderAvailability(!summary?.availableForDelivery)) } catch (error) { setToast(error.message) }
  }
  async function accept(orderId) {
    try { await api.acceptDelivery(orderId); await load(); setToast('Delivery accepted.') } catch (error) { setToast(error.message) }
  }
  function completed(updated) {
    setDeliveries((current) => current.map((order) => order.id === updated.id ? updated : order)); load()
  }

  if (!user || user.role !== 'RIDER') return <main className="role-gate"><Bike /><h1>Rider workspace</h1><p>Sign in with a rider account to find and manage deliveries.</p>{user ? <button className="primary-button" onClick={signOut}>Sign out</button> : <button className="primary-button" onClick={() => setAuthOpen(true)}>Rider sign in</button>}<Link to="/">Back home</Link></main>
  if (!canOperate) return <main className="role-gate"><Bike /><h1>Finish rider onboarding</h1><p>Complete verification and wait for administrator approval before accepting deliveries.</p><Link className="primary-button" to="/account">Open onboarding</Link><Link to="/">Back home</Link></main>
  if (loading && !summary) return <main className="owner-loading">Loading rider workspace…</main>

  return <main className="operations-page rider-page">
    <header><div><Link to="/" className="wordmark">QuickBite</Link><span>Rider workspace</span></div><button className="secondary-button" onClick={signOut}>Sign out</button></header>
    <section><div className="rider-title"><div><h1>Your route.</h1><p>Manage availability, deliveries, proof and earnings.</p></div><button className={summary?.availableForDelivery ? 'availability-toggle active' : 'availability-toggle'} onClick={toggleAvailability}>{summary?.availableForDelivery ? 'Available for deliveries' : 'Go available'}</button></div>
      <div className="operations-stats"><article><Bike /><span>Status</span><strong>{summary?.availableForDelivery ? 'Online' : 'Offline'}</strong></article><article><PackageCheck /><span>Active</span><strong>{summary?.activeDeliveries || 0}</strong></article><article><CheckCircle2 /><span>Completed</span><strong>{summary?.completedDeliveries || 0}</strong></article><article><span>₦</span><span>Earnings</span><strong>₦{Number(summary?.totalDeliveryEarnings || 0).toLocaleString()}</strong></article></div>
      <div className="operations-grid"><article className="operations-card"><h2>Ready for pickup <b>{available.length}</b></h2>{available.length === 0 ? <p className="operations-empty">No unassigned orders are ready.</p> : available.map((order) => <div className="delivery-card" key={order.id}><PackageCheck /><div><strong>#{order.id} · {order.restaurantName}</strong><span><MapPin />{order.deliveryAddress}</span><small>₦{Number(order.totalAmount).toLocaleString()}</small></div><button className="primary-button" disabled={!summary?.availableForDelivery} onClick={() => accept(order.id)}>Accept</button></div>)}<PaginationControls page={availablePage} totalPages={availablePageInfo.totalPages} onPageChange={setAvailablePage} label="available deliveries" /></article>
        <article className="operations-card"><h2>My deliveries <b>{deliveries.length}</b></h2>{deliveries.length === 0 ? <p className="operations-empty">Accepted deliveries and history appear here.</p> : deliveries.map((order) => <div className="delivery-card rider-delivery" key={order.id}><Bike /><div><strong>#{order.id} · {order.restaurantName}</strong><span><MapPin />{order.deliveryAddress}</span><small>{order.orderStatus.replaceAll('_', ' ')}</small><div className="delivery-links"><a href={`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(order.deliveryAddress)}`} target="_blank" rel="noreferrer"><ExternalLink />Navigate</a>{order.restaurantPhoneNumber ? <a href={`tel:${order.restaurantPhoneNumber}`}><Phone />Restaurant</a> : null}{order.customerPhoneNumber ? <a href={`tel:${order.customerPhoneNumber}`}><Phone />Customer</a> : null}</div></div>{order.orderStatus === 'OUT_FOR_DELIVERY' ? <DeliveryProofForm orderId={order.id} onComplete={completed} onToast={setToast} /> : order.deliveryEvidenceUrl ? <a href={order.deliveryEvidenceUrl} target="_blank" rel="noreferrer">View proof</a> : null}</div>)}<PaginationControls page={deliveryPage} totalPages={deliveryPageInfo.totalPages} onPageChange={setDeliveryPage} label="delivery history" /></article>
      </div>
    </section>
  </main>
}
