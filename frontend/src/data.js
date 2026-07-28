export const restaurants = [
  {
    id: 1,
    name: 'Jollof & Co.',
    description: 'Firewood flavour, generous portions, familiar comfort.',
    cuisineType: 'NIGERIAN',
    tags: ['Nigerian', 'Swallow', 'Grills'],
    deliveryTime: '25–35 min',
    deliveryFee: 500,
    rating: 4.6,
    image: '/assets/menu/jollof-chicken.png',
    isOpen: true,
  },
  {
    id: 2,
    name: "Mama T's Kitchen",
    description: 'Rich soups and proper home-style classics.',
    cuisineType: 'NIGERIAN',
    tags: ['Nigerian', 'Soups', 'Swallow'],
    deliveryTime: '30–40 min',
    deliveryFee: 400,
    rating: 4.5,
    image: '/assets/efo-riro.png',
    isOpen: true,
  },
  {
    id: 3,
    name: 'Suya Republic',
    description: 'Smoky skewers, bold yaji and late-night favourites.',
    cuisineType: 'GRILLS',
    tags: ['Grills', 'Suya', 'Small chops'],
    deliveryTime: '20–30 min',
    deliveryFee: 600,
    rating: 4.7,
    image: '/assets/suya.png',
    isOpen: true,
  },
]

export const menuItems = [
  { id: 11, restaurantId: 1, name: 'Party Jollof & Chicken', description: 'Smoky party jollof with two pieces of flame-grilled chicken.', category: 'Popular', price: 4500, imageURL: '/assets/menu/jollof-chicken.png', isAvailable: true },
  { id: 12, restaurantId: 1, name: 'Nigerian Jollof Rice', description: 'Deeply seasoned tomato rice with peppers and our house spice blend.', category: 'Rice', price: 2800, imageURL: '/assets/menu/jollof-rice.webp', isAvailable: true },
  { id: 13, restaurantId: 1, name: 'Fried Rice & Chicken', description: 'Nigerian fried rice with vegetables, prawns and seasoned chicken.', category: 'Rice', price: 4200, imageURL: '/assets/menu/fried-rice.png', isAvailable: true },
  { id: 14, restaurantId: 1, name: 'Classic Beef Suya', description: 'Smoky beef skewers with yaji, onions, tomato and cucumber.', category: 'Grills', price: 3500, imageURL: '/assets/menu/beef-suya.webp', isAvailable: true },
  { id: 15, restaurantId: 1, name: 'Grilled Tilapia', description: 'Whole grilled tilapia finished with a bright Nigerian pepper sauce.', category: 'Grills', price: 8500, imageURL: '/assets/menu/grilled-tilapia.png', isAvailable: true },
  { id: 16, restaurantId: 1, name: 'Moi Moi', description: 'Steamed bean pudding—soft, savoury and perfect beside rice.', category: 'Sides', price: 1200, imageURL: '/assets/menu/moi-moi.png', isAvailable: true },
  { id: 17, restaurantId: 1, name: 'Goat Meat Pepper Soup', description: 'Aromatic pepper soup with tender goat meat and warming spices.', category: 'Soups', price: 4800, imageURL: '/assets/menu/pepper-soup.png', isAvailable: true },
  { id: 21, restaurantId: 2, name: 'Efo Riro & Pounded Yam', description: 'Deeply seasoned vegetable stew with assorted meat.', category: 'Swallow', price: 5200, imageURL: '/assets/efo-riro.png', isAvailable: true },
  { id: 22, restaurantId: 2, name: 'Efo Riro Bowl', description: 'Assorted meat, smoked fish and rich greens.', category: 'Soups', price: 3900, imageURL: '/assets/efo-riro.png', isAvailable: true },
  { id: 31, restaurantId: 3, name: 'Classic Beef Suya', description: 'Smoky beef skewers, onions, cabbage and yaji.', category: 'Grills', price: 3500, imageURL: '/assets/suya.png', isAvailable: true },
  { id: 32, restaurantId: 3, name: 'Suya Party Box', description: 'A generous sharing box with vegetables and extra spice.', category: 'Popular', price: 7500, imageURL: '/assets/suya.png', isAvailable: true },
]

export const ownerOrders = [
  { id: 5921, customer: 'Tosin A.', items: 2, total: 7500, status: 'PENDING', time: '10:24 AM', address: '12 Admiralty Way, Lekki Phase 1', payment: 'Cash on delivery' },
  { id: 5920, customer: 'Chinedu K.', items: 3, total: 12200, status: 'CONFIRMED', time: '10:18 AM', address: 'Ikoyi, Lagos', payment: 'Paystack' },
  { id: 5919, customer: 'Amaka O.', items: 1, total: 4000, status: 'PREPARING', time: '10:12 AM', address: 'Victoria Island, Lagos', payment: 'Paystack' },
  { id: 5918, customer: 'Bola S.', items: 4, total: 15500, status: 'PREPARING', time: '10:05 AM', address: 'Oniru, Lagos', payment: 'Cash on delivery' },
  { id: 5917, customer: 'Ibrahim M.', items: 2, total: 7000, status: 'READY_FOR_PICKUP', time: '9:58 AM', address: 'Lekki Phase 1, Lagos', payment: 'Paystack' },
  { id: 5916, customer: 'Adaobi E.', items: 1, total: 3500, status: 'CONFIRMED', time: '9:45 AM', address: 'Yaba, Lagos', payment: 'Paystack' },
]

export const categories = ['All', 'Nigerian', 'Breakfast', 'Swallow', 'Grills', 'Small chops', 'Healthy', 'Drinks']
