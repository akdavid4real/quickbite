import { Navigate, Route, Routes } from 'react-router-dom'
import Header from './components/Header'
import CartDrawer from './components/CartDrawer'
import AuthModal from './components/AuthModal'
import Toast from './components/Toast'
import HomePage from './pages/HomePage'
import RestaurantPage from './pages/RestaurantPage'
import OrdersPage from './pages/OrdersPage'
import OwnerDashboard from './pages/OwnerDashboard'
import AdminDashboard from './pages/AdminDashboard'
import RiderDashboard from './pages/RiderDashboard'

function CustomerShell({ children }) {
  return (
    <>
      <Header />
      <main>{children}</main>
      <CartDrawer />
    </>
  )
}

export default function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<CustomerShell><HomePage /></CustomerShell>} />
        <Route path="/restaurants/:restaurantId" element={<CustomerShell><RestaurantPage /></CustomerShell>} />
        <Route path="/orders" element={<CustomerShell><OrdersPage /></CustomerShell>} />
        <Route path="/owner" element={<OwnerDashboard />} />
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/rider" element={<RiderDashboard />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      <AuthModal />
      <Toast />
    </>
  )
}
