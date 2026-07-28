import { useEffect } from 'react'
import { useApp } from '../store/AppContext'

export default function Toast() {
  const { toast, setToast } = useApp()

  useEffect(() => {
    if (!toast) return undefined
    const timeout = window.setTimeout(() => setToast(''), 3200)
    return () => window.clearTimeout(timeout)
  }, [toast, setToast])

  return toast ? <div className="toast" role="status">{toast}</div> : null
}
