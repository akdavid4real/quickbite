import { ArrowLeft, Check, Clock3, MapPin, Plus, Star, Store, Truck } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { menuItems, restaurants } from '../data'
import { api } from '../lib/api'
import { normalizeMenuItem, normalizeRestaurant } from '../lib/normalizers'
import { useApp } from '../store/AppContext'

export default function RestaurantPage() {
  const { restaurantId } = useParams()
  const fallbackRestaurant = restaurants.find((item) => item.id === Number(restaurantId))
  const fallbackItems = useMemo(
    () => fallbackRestaurant
      ? menuItems
        .filter((item) => item.restaurantId === fallbackRestaurant.id)
        .map((item) => ({ ...item, isLocalPreview: true }))
      : [],
    [fallbackRestaurant],
  )
  const [restaurant, setRestaurant] = useState(fallbackRestaurant || null)
  const [items, setItems] = useState(fallbackItems)
  const [category, setCategory] = useState('All')
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState('')
  const { addItem } = useApp()

  useEffect(() => {
    let active = true
    setLoading(true)
    setLoadError('')

    Promise.allSettled([api.restaurant(restaurantId), api.menu(restaurantId)])
      .then(([restaurantResult, menuResult]) => {
        if (!active) return
        const liveRestaurant = restaurantResult.status === 'fulfilled'
          ? normalizeRestaurant(restaurantResult.value)
          : null
        const nextRestaurant = liveRestaurant || fallbackRestaurant

        if (!nextRestaurant) {
          setRestaurant(null)
          setItems([])
          setLoadError(restaurantResult.reason?.message || 'We could not find that restaurant.')
          return
        }

        setRestaurant(nextRestaurant)
        if (liveRestaurant) {
          setItems(menuResult.status === 'fulfilled' && Array.isArray(menuResult.value)
            ? menuResult.value.map((item) => normalizeMenuItem(item, nextRestaurant))
            : [])
          if (menuResult.status === 'rejected') setLoadError('The restaurant loaded, but its menu could not be fetched.')
        } else {
          setItems(fallbackItems)
        }
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => { active = false }
  }, [fallbackItems, fallbackRestaurant, restaurantId])

  const categories = ['All', ...new Set(items.map((item) => item.category).filter(Boolean))]
  const visibleItems = category === 'All' ? items : items.filter((item) => item.category === category)

  if (!restaurant && loading) {
    return <section className="page-shell page-state"><p>Loading restaurant and menu…</p></section>
  }

  if (!restaurant) {
    return (
      <section className="page-shell page-state">
        <Store size={38} />
        <h1>Restaurant unavailable</h1>
        <p>{loadError || 'We could not find that restaurant.'}</p>
        <Link className="primary-button" to="/">Browse restaurants</Link>
      </section>
    )
  }

  return (
    <div className="restaurant-page">
      <div className="restaurant-banner">
        <img src={restaurant.image} alt={`${restaurant.name} food`} />
        <div className="restaurant-banner-shade" />
        <Link className="restaurant-floating-back" to="/"><ArrowLeft size={18} />All restaurants</Link>
      </div>

      <section className="page-shell restaurant-detail-head">
        <div>
          <div>
            <div className="restaurant-title-line">
              <h1>{restaurant.name}</h1>
              <span className={restaurant.isOpen ? 'restaurant-open-chip' : 'restaurant-open-chip closed'}><Check size={13} />{restaurant.isOpen ? 'Open now' : 'Closed'}</span>
            </div>
            <p>{restaurant.description}</p>
            {restaurant.address ? <span className="restaurant-address"><MapPin size={15} />{restaurant.address}</span> : null}
          </div>
          <div className="detail-meta">
            <span><Star fill="currentColor" size={18} />{Number(restaurant.rating || 0).toFixed(1)}</span>
            <span><Clock3 size={18} />{restaurant.deliveryTime}</span>
            <span><Truck size={18} />₦{Number(restaurant.deliveryFee || 0).toLocaleString()} delivery</span>
          </div>
        </div>
      </section>

      <section className="menu-category-bar">
        <div className="page-shell">
          {categories.map((itemCategory) => <button className={category === itemCategory ? 'active' : ''} type="button" onClick={() => setCategory(itemCategory)} key={itemCategory}>{itemCategory}</button>)}
        </div>
      </section>

      <section className="page-shell menu-section">
        <div className="section-heading">
          <div><p className="menu-eyebrow">Made to order</p><h2>{category === 'All' ? 'Full menu' : category}</h2></div>
          <p>{visibleItems.length} {visibleItems.length === 1 ? 'dish' : 'dishes'}</p>
        </div>

        {loadError ? <p className="menu-load-note">{loadError}</p> : null}
        <div className="menu-grid">
          {visibleItems.map((item) => (
            <article className="menu-card" key={item.id}>
              <div className="menu-card-image"><img src={item.imageURL} alt={item.name} />{item.category === 'Popular' ? <span>Popular</span> : null}</div>
              <div className="menu-card-copy">
                <div><h3>{item.name}</h3><p>{item.description}</p></div>
                <div className="menu-card-foot">
                  <strong>₦{Number(item.price).toLocaleString()}</strong>
                  <button className="menu-add-button" disabled={!item.isAvailable || !restaurant.isOpen} onClick={() => addItem(item)} type="button" aria-label={`Add ${item.name} to cart`}>
                    <Plus size={18} />{item.isAvailable ? 'Add' : 'Unavailable'}
                  </button>
                </div>
              </div>
            </article>
          ))}
        </div>

        {!loading && visibleItems.length === 0 ? (
          <div className="empty-menu">
            <Store size={30} />
            <h3>No dishes in this section</h3>
            <p>{items.length === 0 ? 'This restaurant has not published its menu yet.' : 'Choose another menu category.'}</p>
          </div>
        ) : null}
      </section>
    </div>
  )
}
