import { BadgeCheck, Clock3, ShieldAlert } from 'lucide-react'
import { useState } from 'react'
import { api } from '../../lib/api'

export default function ProviderOnboarding({ role, verification, onChange, onToast }) {
  const [submitting, setSubmitting] = useState(false)
  const approved = verification?.accountStatus === 'ACTIVE' && verification?.verificationStatus === 'VERIFIED'

  async function submit(event) {
    event.preventDefault()
    setSubmitting(true)
    try {
      const result = await api.submitProviderVerification(Object.fromEntries(new FormData(event.currentTarget)))
      onChange(result)
      onToast(result.verificationStatus === 'VERIFIED' ? 'Identity verified. Admin approval is next.' : result.failureReason)
    } catch (error) {
      onToast(error.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="account-card provider-card">
      <div><p className="eyebrow">Provider onboarding</p><h2>{role === 'RIDER' ? 'Rider verification' : 'Restaurant-owner verification'}</h2></div>
      <div className={`verification-state ${approved ? 'approved' : ''}`}>
        {approved ? <BadgeCheck /> : verification?.verificationStatus === 'REJECTED' ? <ShieldAlert /> : <Clock3 />}
        <div><strong>{approved ? 'Approved to operate' : verification?.verificationStatus === 'VERIFIED' ? 'Identity verified · awaiting admin approval' : verification?.verificationStatus === 'REJECTED' ? 'Verification needs attention' : 'Verification required'}</strong>
          <span>{verification?.failureReason || 'The current integration is a mock provider and can be replaced without changing this workflow.'}</span></div>
      </div>
      {!approved ? (
        <form className="account-form" onSubmit={submit}>
          <label>Legal name<input name="legalName" required /></label>
          <label>Identity number<input name="identityNumber" minLength="6" required /></label>
          {role === 'RIDER'
            ? <label className="wide">Vehicle registration number<input name="vehicleRegistrationNumber" required /></label>
            : <label className="wide">Business registration number<input name="businessRegistrationNumber" required /></label>}
          <button className="primary-button" disabled={submitting} type="submit">{submitting ? 'Checking…' : 'Run mock verification'}</button>
        </form>
      ) : null}
    </section>
  )
}
