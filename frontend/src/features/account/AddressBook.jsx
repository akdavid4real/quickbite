import { MapPin, Pencil, Plus, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { api } from '../../lib/api'

const emptyDraft = { id: null, label: '', address: '', isDefault: false }

export default function AddressBook({ addresses, onChange, onToast }) {
  const [draft, setDraft] = useState(emptyDraft)
  const [saving, setSaving] = useState(false)

  async function submit(event) {
    event.preventDefault()
    setSaving(true)
    const payload = Object.fromEntries(new FormData(event.currentTarget))
    payload.isDefault = payload.isDefault === 'on'
    try {
      if (draft.id) await api.updateAddress(draft.id, payload)
      else await api.createAddress(payload)
      onChange(await api.savedAddresses())
      setDraft(emptyDraft)
      onToast(draft.id ? 'Address updated.' : 'Address saved.')
    } catch (error) {
      onToast(error.message)
    } finally {
      setSaving(false)
    }
  }

  async function remove(id) {
    try {
      await api.deleteAddress(id)
      onChange(await api.savedAddresses())
      onToast('Address removed.')
    } catch (error) {
      onToast(error.message)
    }
  }

  return (
    <section className="account-card">
      <div><p className="eyebrow">Faster checkout</p><h2>Saved addresses</h2></div>
      <div className="address-list">
        {addresses.length === 0 ? <p>No saved addresses yet.</p> : addresses.map((entry) => (
          <article key={entry.id}>
            <MapPin /><div><strong>{entry.label}{entry.isDefault ? ' · Default' : ''}</strong><span>{entry.address}</span></div>
            <button aria-label={`Edit ${entry.label}`} onClick={() => setDraft(entry)} type="button"><Pencil /></button>
            <button aria-label={`Delete ${entry.label}`} onClick={() => remove(entry.id)} type="button"><Trash2 /></button>
          </article>
        ))}
      </div>
      <form className="account-form" key={draft.id || 'new'} onSubmit={submit}>
        <label>Label<input name="label" defaultValue={draft.label} placeholder="Home or Work" required /></label>
        <label className="wide">Full address<input name="address" defaultValue={draft.address} minLength="8" required /></label>
        <label className="checkbox-label"><input name="isDefault" type="checkbox" defaultChecked={draft.isDefault} />Use as default</label>
        <div className="form-actions">
          <button className="primary-button" disabled={saving} type="submit"><Plus />{saving ? 'Saving…' : draft.id ? 'Update address' : 'Add address'}</button>
          {draft.id ? <button className="secondary-button" onClick={() => setDraft(emptyDraft)} type="button">Cancel</button> : null}
        </div>
      </form>
    </section>
  )
}
