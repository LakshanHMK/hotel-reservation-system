import { KeyRound, Mail } from 'lucide-react'
import { useState } from 'react'
import { useNavigate } from 'react-router'
import StaffAuthShell from '../../components/StaffAuthShell/StaffAuthShell.jsx'
import PasswordField from '../../components/StaffPasswordFields/StaffPasswordFields.jsx'
import { authApi } from '../../services/authApi.js'
import { isValidEmail } from '../../utils/authValidation.js'
import { getStaffPasswordError, validatePasswordConfirmation } from '../../utils/staffPasswordValidation.js'

function StaffForgotPassword() {
  const navigate = useNavigate()
  const [step, setStep] = useState('email')
  const [email, setEmail] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [visible, setVisible] = useState({ newPassword: false, confirmPassword: false })
  const [errors, setErrors] = useState({})
  const [status, setStatus] = useState({ type: '', text: '' })
  const [submitting, setSubmitting] = useState(false)

  const clear = (field) => {
    setErrors((current) => ({ ...current, [field]: '' }))
    setStatus({ type: '', text: '' })
  }

  const checkEmail = async (event) => {
    event.preventDefault()
    if (submitting) return
    const trimmedEmail = email.trim()
    if (!trimmedEmail || !isValidEmail(trimmedEmail)) {
      setErrors({ email: trimmedEmail ? 'Enter a valid email address.' : 'Work email address is required.' })
      return
    }

    setSubmitting(true)
    setErrors({})
    setStatus({ type: '', text: '' })
    try {
      const result = await authApi.checkStaffForgotPasswordEmail({ email: trimmedEmail })
      if (!result.exists) {
        setStatus({ type: 'error', text: 'No account found with this email.' })
        return
      }
      setEmail(trimmedEmail)
      setStep('password')
    } catch (error) {
      setStatus({ type: 'error', text: error.body?.message || error.message || 'Could not check this email.' })
    } finally {
      setSubmitting(false)
    }
  }

  const changePassword = async (event) => {
    event.preventDefault()
    if (submitting) return
    const nextErrors = {
      newPassword: getStaffPasswordError(newPassword),
      confirmPassword: validatePasswordConfirmation(newPassword, confirmPassword),
    }
    setErrors(nextErrors)
    if (Object.values(nextErrors).some(Boolean)) return

    setSubmitting(true)
    setStatus({ type: '', text: '' })
    try {
      await authApi.changeStaffForgottenPassword({ email, newPassword, confirmPassword })
      setStatus({ type: 'success', text: 'Password changed successfully.' })
      setTimeout(() => navigate('/staff/login', { replace: true }), 1200)
    } catch (error) {
      setStatus({ type: 'error', text: error.body?.message || error.message || 'Could not change password.' })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <StaffAuthShell
      eyebrow="Account Recovery"
      title={step === 'email' ? 'Forgot Password' : 'Reset Password'}
      description={step === 'email'
        ? 'Enter the email address associated with your staff account.'
        : `Create a new password for ${email}.`}
    >
      {step === 'email' ? (
        <form className="staff-auth-form" noValidate onSubmit={checkEmail}>
          <div className="staff-auth-field">
            <label htmlFor="staff-recovery-email">Work Email Address <span aria-hidden="true">*</span></label>
            <div className="staff-auth-input">
              <Mail aria-hidden="true" size={18} />
              <input id="staff-recovery-email" type="email" autoComplete="email" value={email}
                aria-invalid={Boolean(errors.email)} aria-describedby={errors.email ? 'staff-recovery-email-error' : undefined}
                onChange={(event) => { setEmail(event.target.value); clear('email') }} />
            </div>
            {errors.email && <small className="staff-auth-error" id="staff-recovery-email-error" role="alert">{errors.email}</small>}
          </div>
          <button className="staff-auth-submit" type="submit" disabled={submitting}><KeyRound aria-hidden="true" size={17} />{submitting ? 'Checking...' : 'Continue'}</button>
          {status.text && <p className={`staff-auth-status staff-auth-status--${status.type}`} role="alert">{status.text}</p>}
        </form>
      ) : (
        <form className="staff-auth-form" noValidate onSubmit={changePassword}>
          <PasswordField id="staff-forgot-new-password" label="New Password" value={newPassword}
            onChange={(value) => { setNewPassword(value); clear('newPassword') }} error={errors.newPassword}
            visible={visible.newPassword} onToggle={() => setVisible((current) => ({ ...current, newPassword: !current.newPassword }))} autoComplete="new-password" />
          <PasswordField id="staff-forgot-confirm-password" label="Confirm Password" value={confirmPassword}
            onChange={(value) => { setConfirmPassword(value); clear('confirmPassword') }} error={errors.confirmPassword}
            visible={visible.confirmPassword} onToggle={() => setVisible((current) => ({ ...current, confirmPassword: !current.confirmPassword }))} autoComplete="new-password" />
          <p className="staff-auth-note">Use 8-128 characters with uppercase, lowercase, number, and special character.</p>
          <button className="staff-auth-submit" type="submit" disabled={submitting}><KeyRound aria-hidden="true" size={17} />{submitting ? 'Changing Password...' : 'Change Password'}</button>
          {status.text && <p className={`staff-auth-status staff-auth-status--${status.type}`} role={status.type === 'error' ? 'alert' : 'status'}>{status.text}</p>}
        </form>
      )}
    </StaffAuthShell>
  )
}

export default StaffForgotPassword
