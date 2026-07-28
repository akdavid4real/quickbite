import { X } from 'lucide-react'
import { useState } from 'react'
import { api } from '../lib/api'
import { useApp } from '../store/AppContext'

export default function AuthModal() {
  const { authenticate, isAuthOpen, setAuthOpen, setToast } = useApp()
  const [mode, setMode] = useState('login')
  const [loading, setLoading] = useState(false)

  async function submit(event) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    setLoading(true)
    try {
      const payload = Object.fromEntries(form)
      const result = mode === 'login'
        ? await api.login(payload)
        : await api.register(payload)
      await authenticate(result)
      setToast(`Welcome${result.name ? `, ${result.name}` : ''}.`)
      setAuthOpen(false)
    } catch (error) {
      setToast(error.message)
    } finally {
      setLoading(false)
    }
  }

  if (!isAuthOpen) return null

  return (
    <div className="modal-layer" role="dialog" aria-modal="true" aria-label="Account">
      <button className="modal-scrim" type="button" aria-label="Close" onClick={() => setAuthOpen(false)} />
      <section className="auth-modal">
        <button className="modal-close square-button" type="button" onClick={() => setAuthOpen(false)}><X size={20} /></button>
        <p className="auth-wordmark">QuickBite</p>
        <h2>{mode === 'login' ? 'Welcome back.' : 'Create your account.'}</h2>
        <p>{mode === 'login' ? 'Sign in to see your orders and checkout faster.' : 'Your next favourite meal is a few taps away.'}</p>
        <form onSubmit={submit}>
          {mode === 'register' ? <label>Full name<input name="name" required placeholder="Your name" /></label> : null}
          <label>Email<input name="email" type="email" required placeholder="you@example.com" /></label>
          <label>Password<input name="password" type="password" minLength="6" required placeholder="At least 6 characters" /></label>
          {mode === 'register' ? (
            <>
              <label>Phone number<input name="phoneNumber" required placeholder="0800 000 0000" /></label>
              <label>Address<input name="address" placeholder="Your delivery address" /></label>
              <label>Account type
                <select name="role" defaultValue="CUSTOMER">
                  <option value="CUSTOMER">Customer</option>
                  <option value="RESTAURANT_OWNER">Restaurant owner</option>
                  <option value="RIDER">Delivery rider</option>
                </select>
              </label>
            </>
          ) : null}
          <button className="primary-button full-button" disabled={loading} type="submit">{loading ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create account'}</button>
        </form>
        <button className="text-button" type="button" onClick={() => setMode(mode === 'login' ? 'register' : 'login')}>
          {mode === 'login' ? 'New to QuickBite? Create an account' : 'Already have an account? Sign in'}
        </button>
      </section>
    </div>
  )
}
