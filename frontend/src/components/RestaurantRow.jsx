import { Clock3, Plus, Star, Truck } from 'lucide-react'
import { Link } from 'react-router-dom'

export default function RestaurantRow({ restaurant }) {
  return (
    <article className="restaurant-row">
      <Link to={`/restaurants/${restaurant.id}`} className="restaurant-image-link" aria-label={`View ${restaurant.name}`}>
        <img src={restaurant.image} alt="" />
      </Link>
      <div className="restaurant-copy">
        <Link to={`/restaurants/${restaurant.id}`}><h3>{restaurant.name}</h3></Link>
        <p>{restaurant.tags.join(' · ')}</p>
      </div>
      <div className="restaurant-meta">
        <span><Clock3 size={18} />{restaurant.deliveryTime}</span>
        <span><Truck size={18} />₦{restaurant.deliveryFee.toLocaleString()} delivery</span>
        <span className="rating"><Star size={18} fill="currentColor" />{restaurant.rating}</span>
      </div>
      <Link className="square-button" to={`/restaurants/${restaurant.id}`} aria-label={`View the ${restaurant.name} menu`}>
        <Plus size={20} />
      </Link>
    </article>
  )
}
