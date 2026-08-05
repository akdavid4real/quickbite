import { ArrowRight, Check, ChefHat, Clock3, Coffee, CookingPot, CupSoda, Flame, Ham, LocateFixed, Salad, Search, Soup, Truck, Utensils } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import RestaurantRow from '../components/RestaurantRow'
import PaginationControls from '../components/PaginationControls'
import { categories, restaurants } from '../data'
import { api } from '../lib/api'
import { normalizeRestaurant } from '../lib/normalizers'
import { useApp } from '../store/AppContext'

const categoryIcons = [Utensils, CookingPot, Coffee, Soup, Flame, Ham, Salad, CupSoda]

function ActiveOrderCard({ order, signedIn }) {
  if (!order) {
    return (
      <aside className="active-order active-order-empty">
        <Clock3 size={25} />
        <h3>No active order</h3>
        <p>{signedIn ? 'Your next order will be tracked here.' : 'Sign in and place an order to track it here.'}</p>
        <a className="secondary-button full-button" href="#restaurants">Browse food</a>
      </aside>
    )
  }

  return (
    <aside className="active-order">
      <div className="active-order-head">
        <div><p>Active order</p><span>{order.restaurantName}</span></div>
        <strong>₦{Number(order.totalAmount || 0).toLocaleString()}</strong>
      </div>
      <div className="active-order-food">
        <img src="/assets/hero-jollof.png" alt="" />
        <div><strong>{order.orderItems?.[0]?.itemName || 'Your order'}</strong><span>{order.orderItems?.length || 0} items</span></div>
      </div>
      <ol className="order-progress">
        <li className={order.orderStatus !== 'PENDING' ? 'done' : 'current'}><span><Check size={14} /></span><div><strong>Order confirmed</strong><small>{order.orderStatus === 'PENDING' ? 'Waiting for the restaurant' : 'Accepted'}</small></div></li>
        <li className={['PREPARING', 'READY_FOR_PICKUP'].includes(order.orderStatus) ? 'current' : ['OUT_FOR_DELIVERY', 'DELIVERED'].includes(order.orderStatus) ? 'done' : ''}><span><ChefHat size={14} /></span><div><strong>Being prepared</strong><small>The kitchen is cooking.</small></div></li>
        <li className={order.orderStatus === 'OUT_FOR_DELIVERY' ? 'current' : order.orderStatus === 'DELIVERED' ? 'done' : ''}><span><Truck size={14} /></span><div><strong>On the way</strong><small>{order.riderName || 'Rider assignment follows pickup'}</small></div></li>
        <li className={order.orderStatus === 'DELIVERED' ? 'done' : ''}><span><LocateFixed size={14} /></span><div><strong>Delivered</strong><small>After handoff</small></div></li>
      </ol>
      <Link className="secondary-button full-button" to="/orders"><LocateFixed size={18} />Track order</Link>
    </aside>
  )
}

export default function HomePage() {
  const [restaurantList, setRestaurantList] = useState(restaurants)
  const [activeOrder, setActiveOrder] = useState(null)
  const [page, setPage] = useState(0)
  const [pageInfo, setPageInfo] = useState({ totalPages: 1 })
  const [searchParams, setSearchParams] = useSearchParams()
  const { user } = useApp()
  const query = searchParams.get('q')?.trim().toLowerCase() || ''
  const selectedCategory = searchParams.get('category') || 'All'

  useEffect(() => {
    let active = true
    api.restaurantsPage(page)
      .then((result) => {
        if (!active) return
        const items = result?.content || []
        setRestaurantList(items.map(normalizeRestaurant))
        setPageInfo(result || { totalPages: 1 })
      })
      .catch(() => {
        // Keep the local preview restaurants when the backend is unavailable.
      })
    return () => { active = false }
  }, [page])

  useEffect(() => {
    if (user?.role !== 'CUSTOMER') {
      setActiveOrder(null)
      return
    }
    let active = true
    api.myOrders()
      .then((result) => {
        if (!active || !Array.isArray(result)) return
        const current = result
          .filter((order) => !['DELIVERED', 'CANCELLED'].includes(order.orderStatus))
          .toSorted((a, b) => new Date(b.createdAt) - new Date(a.createdAt))[0]
        setActiveOrder(current || null)
      })
      .catch(() => setActiveOrder(null))
    return () => { active = false }
  }, [user?.id, user?.role])

  const visibleRestaurants = restaurantList.filter((restaurant) => {
    const searchable = [restaurant.name, restaurant.description, ...(restaurant.tags || [])].join(' ').toLowerCase()
    return (!query || searchable.includes(query))
      && (selectedCategory === 'All' || searchable.includes(selectedCategory.toLowerCase()))
  })

  function chooseCategory(category) {
    const next = new URLSearchParams(searchParams)
    if (category === 'All') next.delete('category')
    else next.set('category', category)
    setSearchParams(next)
  }

  return (
    <>
      <section className="hero">
        <div className="hero-copy">
          <h1>Good food,<br />without the long wait.</h1>
          <p>Order from trusted restaurants around you and track every step to your door.</p>
          <a className="primary-button hero-button" href="#restaurants"><Search size={20} />Find food</a>
        </div>
        <div className="hero-media"><img src="/assets/hero-jollof.png" alt="Jollof rice, grilled chicken and plantain" /></div>
      </section>

      <section className="category-rail" aria-label="Food categories">
        <div className="page-shell category-track">
          {categories.map((category, index) => {
            const CategoryIcon = categoryIcons[index]
            return (
              <button aria-label={category} className={selectedCategory === category ? 'category-item active' : 'category-item'} onClick={() => chooseCategory(category)} type="button" key={category}>
                <span><CategoryIcon size={20} strokeWidth={1.8} /></span>{category}
              </button>
            )
          })}
        </div>
      </section>

      <section className="discovery page-shell" id="restaurants">
        <div className="restaurants-column">
          <div className="section-heading">
            <h2>{query ? `Results for “${query}”` : 'Popular near you'}</h2>
            {(query || selectedCategory !== 'All') ? <Link to="/#restaurants">Clear filters</Link> : <a href="#explore">See all restaurants <ArrowRight size={17} /></a>}
          </div>
          <div className="restaurant-list">
            {visibleRestaurants.length === 0 ? <div className="restaurant-empty"><h3>No matching restaurants</h3><p>Try another search or category.</p></div> : visibleRestaurants.map((restaurant) => <RestaurantRow restaurant={restaurant} key={restaurant.id} />)}
          </div>
          <PaginationControls page={page} totalPages={pageInfo.totalPages} onPageChange={setPage} label="restaurants" />
        </div>
        <ActiveOrderCard order={activeOrder} signedIn={Boolean(user)} />
      </section>

      <section className="explore page-shell" id="explore">
        <div className="section-heading"><h2>More to explore</h2><p>Great kitchens and very good reasons not to cook tonight.</p></div>
        <div className="explore-grid">
          {visibleRestaurants.map((restaurant) => (
            <Link to={`/restaurants/${restaurant.id}`} className="explore-card" key={restaurant.id}>
              <img src={restaurant.image} alt="" />
              <div><h3>{restaurant.name}</h3><p>{restaurant.description}</p><span><Clock3 size={16} />{restaurant.deliveryTime}</span></div>
            </Link>
          ))}
        </div>
      </section>
      <footer className="site-footer"><div className="page-shell"><span className="wordmark">QuickBite</span><p>Made for hungry people in Lagos.</p><Link to="/owner">Restaurant owner dashboard</Link></div></footer>
    </>
  )
}
