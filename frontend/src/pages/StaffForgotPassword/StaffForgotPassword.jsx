import { Mail, Send } from 'lucide-react'
import { useState } from 'react'
import StaffAuthShell from '../../components/StaffAuthShell/StaffAuthShell.jsx'
import { authApi } from '../../services/authApi.js'
import { isValidEmail } from '../../utils/authValidation.js'

function StaffForgotPassword() {
  const [email, setEmail] = useState('')
  const [error, setError] = useState('')
  const [status, setStatus] = useState('')

  const handleSubmit = (event) => {
    event.preventDefault()
    const trimmedEmail = email.trim()
    if (!trimmedEmail) {
      setError('Work email address is required.')
      return
    }
    if (!isValidEmail(trimmedEmail)) {
      setError('Enter a valid email address.')
      return
    }

    setError('')
    authApi.forgotPassword({ email: trimmedEmail })
      .then((res) => {
        setStatus(res.message || 'If an account exists for that email, password reset instructions have been sent.')
      })
      .catch((err) => {
        setError(err.body?.message || err.message || 'Could not request password reset.')
      })
  }

  return (
    <StaffAuthShell eyebrow="Account Recovery" title="Forgot Your Password?" description="Enter the email address associated with your staff account.">
      <form className="staff-auth-form" noValidate onSubmit={handleSubmit}>
        <div className="staff-auth-field">
          <label htmlFor="staff-recovery-email">Work Email Address <span aria-hidden="true">*</span></label>
          <div className="staff-auth-input"><Mail aria-hidden="true" size={18} /><input id="staff-recovery-email" type="email" autoComplete="email" value={email} aria-invalid={Boolean(error)} aria-describedby={error ? 'staff-recovery-email-error' : undefined} onChange={(event) => { setEmail(event.target.value); setError(''); setStatus('') }} /></div>
          {error && <small className="staff-auth-error" id="staff-recovery-email-error" role="alert">{error}</small>}
        </div>
        <button className="staff-auth-submit" type="submit"><Send aria-hidden="true" size={17} />Continue</button>
        {status && <p className="staff-auth-status" role="status" aria-live="polite">{status}</p>}
        <p className="staff-auth-note">If an eligible staff account exists for this email, password reset instructions will be provided.</p>
      </form>
    </StaffAuthShell>
  )
}

export default StaffForgotPassword
