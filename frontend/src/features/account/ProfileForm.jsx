import { useState } from 'react'
import { api } from '../../lib/api'

export default function ProfileForm({ profile, onSaved, onToast }) {
  const [saving, setSaving] = useState(false)

  async function submit(event) {
    event.preventDefault()
    setSaving(true)
    try {
      const updated = await api.updateProfile(Object.fromEntries(new FormData(event.currentTarget)))
      onSaved(updated)
      onToast('Profile updated.')
    } catch (error) {
      onToast(error.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="account-card">
      <div><p className="eyebrow">Personal details</p><h2>Your profile</h2></div>
      <form className="account-form" onSubmit={submit}>
        <label>Full name<input name="name" defaultValue={profile.name || ''} required /></label>
        <label>Phone number<input name="phoneNumber" defaultValue={profile.phoneNumber || ''} required /></label>
        <label className="wide">Primary address<input name="address" defaultValue={profile.address || ''} /></label>
        <label className="wide">Email<input value={profile.email || ''} disabled /></label>
        <button className="primary-button" disabled={saving} type="submit">{saving ? 'Saving…' : 'Save profile'}</button>
      </form>
    </section>
  )
}
