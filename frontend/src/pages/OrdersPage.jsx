import { Check, ChefHat, LocateFixed, PackageCheck, Phone, RefreshCw, Star, Truck, XCircle } from 'lucide-react'
import { useEffect, useState } from 'react'
import { restaurants } from '../data'
import { api } from '../lib/api'
import { useApp } from '../store/AppContext'
import OrderReviewForm from '../features/reviews/OrderReviewForm'
import PaginationControls from '../components/PaginationControls'

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

function OrderCard({ order, reviewed, onCancelled, onReview, onToast }) {
  const fallbackRestaurant = restaurants.find((restaurant) => restaurant.id === order.restaurantId)
  const image = order.orderItems?.[0]?.imageURL || fallbackRestaurant?.image || '/assets/hero-jollof.png'
  const itemSummary = order.orderItems?.map((item) => `${item.quantity}× ${item.itemName}`).join(', ') || 'Order items'
  const activeStage = statusStage[order.orderStatus] ?? 0
  const cancelled = order.orderStatus === 'CANCELLED'
  const cancellable = order.orderStatus === 'PENDING' || (order.orderStatus === 'CONFIRMED' && order.paymentMethod === 'CASH_ON_DELIVERY')

  async function cancel() {
    try {
      onCancelled(await api.cancelOrder(order.id))
      onToast(`Order #${order.id} cancelled.`)
    } catch (error) {
      onToast(error.message)
    }
  }

  async function retryPayment() {
    try {
      const payment = await api.initializePayment(order.id)
      if (payment.paymentURL) window.location.assign(payment.paymentURL)
    } catch (error) {
      onToast(error.message)
    }
  }

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
      <div className="order-actions">
        {order.restaurantPhoneNumber ? <a className="secondary-button" href={`tel:${order.restaurantPhoneNumber}`}><Phone />Call restaurant</a> : null}
        {order.riderPhoneNumber ? <a className="secondary-button" href={`tel:${order.riderPhoneNumber}`}><Phone />Call rider</a> : null}
        {order.paymentMethod === 'PAYSTACK' && order.orderStatus === 'PENDING' ? <button className="secondary-button" type="button" onClick={retryPayment}><RefreshCw />Retry payment</button> : null}
        {cancellable ? <button className="danger-button" type="button" onClick={cancel}><XCircle />Cancel order</button> : null}
        {order.orderStatus === 'DELIVERED' && !reviewed ? <OrderReviewForm order={order} onSubmitted={onReview} onToast={onToast} /> : null}
        {reviewed ? <span className="reviewed-chip"><Star size={15} />Reviewed</span> : null}
      </div>
    </article>
  )
}

export default function OrdersPage() {
  const { setAuthOpen, setToast, user } = useApp()
  const [orders, setOrders] = useState([])
  const [reviews, setReviews] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [page, setPage] = useState(0)
  const [pageInfo, setPageInfo] = useState({ totalPages: 1 })
  const userId = user?.id

  useEffect(() => {
    if (!userId || user.role !== 'CUSTOMER') {
      setOrders([])
      return
    }

    let active = true
    setLoading(true)
    setError('')
    Promise.all([api.myOrdersPage(page), api.myReviews()])
      .then(([result, reviewResult]) => {
        if (!active) return
        const nextOrders = result?.content || []
        setOrders(nextOrders.toSorted((a, b) => new Date(b.createdAt) - new Date(a.createdAt)))
        setPageInfo(result || { totalPages: 1 })
        setReviews(Array.isArray(reviewResult) ? reviewResult : [])
      })
      .catch((requestError) => {
        if (active) setError(requestError.message)
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => { active = false }
  }, [page, userId, user?.role])

  function orderCancelled(updated) {
    setOrders((current) => current.map((order) => order.id === updated.id ? updated : order))
  }

  function reviewSubmitted(review) {
    setReviews((current) => [...current, review])
  }

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
          {orders.map((order) => <OrderCard order={order} reviewed={reviews.some((review) => review.orderId === order.id)} onCancelled={orderCancelled} onReview={reviewSubmitted} onToast={setToast} key={order.id} />)}
          <PaginationControls page={page} totalPages={pageInfo.totalPages} onPageChange={setPage} label="orders" />
        </div>
      )}
    </section>
  )
}
