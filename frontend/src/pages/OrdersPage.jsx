import { Check, ChefHat, LocateFixed, PackageCheck, Truck } from 'lucide-react'
import { useEffect, useState } from 'react'
import { restaurants } from '../data'
import { api } from '../lib/api'
import { useApp } from '../store/AppContext'

const steps = [
  { label: 'Confirmed', icon: <Check /> },
  { label: 'Preparing', icon: <ChefHat /> },
  { label: 'On the way', icon: <Truck /> },
  { label: 'Delivered', icon: <PackageCheck /> },
]

const statusStage = {
  PENDING: 0,
  CONFIRMED: 0,
  PREPARING: 1,
  READY_FOR_PICKUP: 1,
  OUT_FOR_DELIVERY: 2,
  DELIVERED: 3,
}

function formatCurrency(value) {
  return `₦${Number(value || 0).toLocaleString()}`
}

function formatDate(value) {
  if (!value) return 'Recently'
  return new Intl.DateTimeFormat('en-NG', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

function OrderCard({ order }) {
  const fallbackRestaurant = restaurants.find((restaurant) => restaurant.id === order.restaurantId)
  const image = fallbackRestaurant?.image || '/assets/hero-jollof.png'
  const itemSummary = order.orderItems?.map((item) => `${item.quantity}× ${item.itemName}`).join(', ') || 'Order items'
  const activeStage = statusStage[order.orderStatus] ?? 0
  const cancelled = order.orderStatus === 'CANCELLED'

  return (
    <article className={cancelled ? 'order-detail-card cancelled-order' : 'order-detail-card'}>
      <div className="order-detail-summary">
        <img src={image} alt="" />
        <div>
          <span>Order #QB-{order.id}</span>
          <h2>{order.restaurantName}</h2>
          <p>{itemSummary}</p>
          <small>{formatDate(order.createdAt)} · {order.paymentMethod === 'PAYSTACK' ? 'Paystack' : 'Cash on delivery'}</small>
        </div>
        <strong>{formatCurrency(order.totalAmount)}</strong>
      </div>
      {cancelled ? (
        <div className="cancelled-message"><strong>Order cancelled</strong><span>This order will not be prepared or delivered.</span></div>
      ) : (
        <div className="order-timeline">
          {steps.map(({ label, icon }, index) => (
            <div className={index < activeStage ? 'complete' : index === activeStage ? 'active' : ''} key={label}>
              <span>{icon}</span>
              <strong>{order.orderStatus === 'PENDING' && index === 0 ? 'Order received' : label}</strong>
              <small>{index === activeStage ? 'Current status' : index < activeStage ? 'Completed' : 'Coming up'}</small>
            </div>
          ))}
        </div>
      )}
      <div className="order-map-placeholder">
        <LocateFixed size={22} />
        <div>
          <strong>Delivering to {order.deliveryAddress}</strong>
          <span>{order.riderName ? `${order.riderName} is handling your delivery.` : 'Your rider will appear here after pickup.'}</span>
        </div>
      </div>
    </article>
  )
}

export default function OrdersPage() {
  const { setAuthOpen, user } = useApp()
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const userId = user?.id

  useEffect(() => {
    if (!userId || user.role !== 'CUSTOMER') {
      setOrders([])
      return
    }

    let active = true
    setLoading(true)
    setError('')
    api.myOrders()
      .then((result) => {
        if (!active) return
        const nextOrders = Array.isArray(result) ? result : []
        setOrders(nextOrders.toSorted((a, b) => new Date(b.createdAt) - new Date(a.createdAt)))
      })
      .catch((requestError) => {
        if (active) setError(requestError.message)
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => { active = false }
  }, [userId, user?.role])

  return (
    <section className="orders-page page-shell">
      <div className="orders-intro">
        <h1>Your orders.</h1>
        <p>Everything you’ve ordered, and exactly where it is.</p>
      </div>

      {!user ? (
        <div className="orders-state">
          <PackageCheck size={34} />
          <h2>Sign in to see your orders</h2>
          <p>Your current and previous deliveries will appear here.</p>
          <button className="primary-button" type="button" onClick={() => setAuthOpen(true)}>Sign in</button>
        </div>
      ) : user.role !== 'CUSTOMER' ? (
        <div className="orders-state">
          <h2>Customer account required</h2>
          <p>Restaurant owners and riders manage orders from their own dashboards.</p>
        </div>
      ) : loading ? (
        <div className="orders-state"><p>Loading your orders…</p></div>
      ) : error ? (
        <div className="orders-state">
          <h2>Orders could not be loaded</h2>
          <p>{error}</p>
        </div>
      ) : orders.length === 0 ? (
        <div className="orders-state">
          <PackageCheck size={34} />
          <h2>No orders yet</h2>
          <p>Your first QuickBite delivery will appear here after checkout.</p>
        </div>
      ) : (
        <div className="orders-list">
          {orders.map((order) => <OrderCard order={order} key={order.id} />)}
        </div>
      )}
    </section>
  )
}
