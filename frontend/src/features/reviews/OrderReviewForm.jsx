import { Star } from 'lucide-react'
import { useState } from 'react'
import { api } from '../../lib/api'

export default function OrderReviewForm({ order, onSubmitted, onToast }) {
  const [open, setOpen] = useState(false)
  const [saving, setSaving] = useState(false)

  async function submit(event) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    setSaving(true)
    try {
      const review = await api.createReview({
        orderId: order.id,
        restaurantId: order.restaurantId,
        rating: Number(form.get('rating')),
        comment: form.get('comment'),
      })
      onSubmitted(review)
      setOpen(false)
      onToast('Thanks for reviewing your order.')
    } catch (error) {
      onToast(error.message)
    } finally {
      setSaving(false)
    }
  }

  if (!open) return <button className="secondary-button" type="button" onClick={() => setOpen(true)}><Star />Review order</button>
  return (
    <form className="order-review-form" onSubmit={submit}>
      <label>Rating<select name="rating" defaultValue="5">{[5, 4, 3, 2, 1].map((rating) => <option key={rating} value={rating}>{rating} star{rating === 1 ? '' : 's'}</option>)}</select></label>
      <label>Comment<textarea name="comment" maxLength="1000" placeholder="How was the food and service?" /></label>
      <div><button className="primary-button" disabled={saving} type="submit">{saving ? 'Submitting…' : 'Submit review'}</button><button className="text-button" type="button" onClick={() => setOpen(false)}>Cancel</button></div>
    </form>
  )
}
