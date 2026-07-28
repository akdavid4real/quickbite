import { LogOut, MapPin, Menu, Search, ShoppingBag, UserRound, X } from 'lucide-react'
import { useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useApp } from '../store/AppContext'

export default function Header() {
  const { cartCount, setCartOpen, setAuthOpen, signOut, user } = useApp()
  const [mobileOpen, setMobileOpen] = useState(false)
  const navigate = useNavigate()

  function search(event) {
    event.preventDefault()
    const query = new FormData(event.currentTarget).get('query')?.trim()
    navigate(query ? `/?q=${encodeURIComponent(query)}#restaurants` : '/#restaurants')
  }

  return (
    <header className="site-header">
      <div className="header-inner">
        <NavLink to="/" className="wordmark" aria-label="QuickBite home">QuickBite</NavLink>
        <nav className={mobileOpen ? 'main-nav is-open' : 'main-nav'} aria-label="Main navigation">
          <NavLink to="/" end>Home</NavLink>
          <a href="/#restaurants">Restaurants</a>
          <NavLink to="/orders">My orders</NavLink>
        </nav>

        <div className="header-tools">
          <button className="location-button" type="button">
            <MapPin size={18} />
            <span>Lagos</span>
          </button>
          <form className="header-search" role="search" onSubmit={search}>
            <Search size={19} />
            <input name="query" aria-label="Search for food or restaurants" placeholder="Search food or restaurants" />
          </form>
          <button className="icon-label-button cart-button" type="button" onClick={() => setCartOpen(true)}>
            <ShoppingBag size={21} />
            <span>Cart</span>
            {cartCount > 0 ? <b>{cartCount}</b> : null}
          </button>
          <button
            aria-label={user ? `Sign out ${user.name}` : 'Sign in or create an account'}
            className="icon-label-button account-button"
            title={user ? 'Sign out' : 'Account'}
            type="button"
            onClick={user ? signOut : () => setAuthOpen(true)}
          >
            {user ? <LogOut size={21} /> : <UserRound size={21} />}
            <span>{user ? user.name?.split(' ')[0] || 'Sign out' : 'Account'}</span>
          </button>
          <button className="mobile-menu" type="button" aria-label="Toggle navigation" onClick={() => setMobileOpen((open) => !open)}>
            {mobileOpen ? <X size={23} /> : <Menu size={23} />}
          </button>
        </div>
      </div>
    </header>
  )
}
