import { restaurants as demoRestaurants } from '../data'

const DEFAULT_DELIVERY_TIME = '25–40 min'
const DEFAULT_DELIVERY_FEE = 500

export function normalizeRestaurant(restaurant, index = 0) {
  const fallback = demoRestaurants[index % demoRestaurants.length]
  const cuisine = restaurant.cuisineType?.replaceAll('_', ' ')

  return {
    ...fallback,
    ...restaurant,
    image: restaurant.logoURL || restaurant.image || fallback.image,
    tags: cuisine ? [cuisine] : (restaurant.tags || fallback.tags),
    deliveryTime: restaurant.deliveryTime || fallback.deliveryTime || DEFAULT_DELIVERY_TIME,
    deliveryFee: restaurant.deliveryFee ?? fallback.deliveryFee ?? DEFAULT_DELIVERY_FEE,
    rating: restaurant.rating ?? 0,
  }
}

export function normalizeMenuItem(item, restaurant) {
  return {
    ...item,
    imageURL: item.imageURL || restaurant?.image || '/assets/hero-jollof.png',
    isAvailable: item.isAvailable !== false,
  }
}
