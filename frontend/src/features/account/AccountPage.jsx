import { LogOut, UserRound } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../../lib/api'
import { useApp } from '../../store/AppContext'
import AddressBook from './AddressBook'
import ProfileForm from './ProfileForm'
import ProviderOnboarding from './ProviderOnboarding'

export default function AccountPage() {
  const { setAuthOpen, setToast, signOut, updateSessionUser, user } = useApp()
  const [profile, setProfile] = useState(null)
  const [addresses, setAddresses] = useState([])
  const [verification, setVerification] = useState(null)
  const [loading, setLoading] = useState(Boolean(user))
  const role = user?.role
  const userId = user?.id

  useEffect(() => {
    if (!userId) return
    let active = true
    const provider = role === 'RIDER' || role === 'RESTAURANT_OWNER'
    Promise.all([
      api.profile(),
      role === 'CUSTOMER' ? api.savedAddresses() : Promise.resolve([]),
      provider ? api.providerVerification() : Promise.resolve(null),
    ]).then(([profileResult, addressResult, verificationResult]) => {
      if (!active) return
      setProfile(profileResult)
      setAddresses(addressResult)
      setVerification(verificationResult)
      updateSessionUser(profileResult)
    }).catch((error) => setToast(error.message)).finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [role, setToast, updateSessionUser, userId])

  if (!user) return <main className="role-gate"><UserRound /><h1>Your account</h1><p>Sign in to manage your profile and delivery details.</p><button className="primary-button" onClick={() => setAuthOpen(true)}>Sign in</button><Link to="/">Back home</Link></main>
  if (loading || !profile) return <main className="owner-loading">Loading your account…</main>

  function profileSaved(updated) {
    setProfile(updated)
    updateSessionUser(updated)
  }

  return (
    <main className="account-page page-shell">
      <header><div><Link className="wordmark" to="/">QuickBite</Link><span>Account settings</span></div><button className="secondary-button" onClick={signOut}><LogOut />Sign out</button></header>
      <div className="account-heading"><p className="eyebrow">Account</p><h1>Good to see you, {profile.name?.split(' ')[0]}.</h1><p>Keep your details and access status up to date.</p></div>
      <div className="account-grid">
        <ProfileForm profile={profile} onSaved={profileSaved} onToast={setToast} />
        {role === 'CUSTOMER' ? <AddressBook addresses={addresses} onChange={setAddresses} onToast={setToast} /> : null}
        {role === 'RIDER' || role === 'RESTAURANT_OWNER' ? <ProviderOnboarding role={role} verification={verification} onChange={setVerification} onToast={setToast} /> : null}
      </div>
    </main>
  )
}
