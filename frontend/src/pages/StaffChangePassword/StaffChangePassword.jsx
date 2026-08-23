import { KeyRound } from 'lucide-react'
import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router'
import StaffAuthShell from '../../components/StaffAuthShell/StaffAuthShell.jsx'
import PasswordField from '../../components/StaffPasswordFields/StaffPasswordFields.jsx'
import { getStaffPasswordError, validatePasswordConfirmation } from '../../utils/staffPasswordValidation.js'
import useAuth from '../../context/useAuth.js'

function StaffChangePassword() {
  const navigate = useNavigate()
  const { user, loading, changeInitialPassword } = useAuth()
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [visible, setVisible] = useState({ newPassword: false, confirmPassword: false })
  const [errors, setErrors] = useState({})
  const [status, setStatus] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const updateField = (field, setter) => (value) => {
    setter(value)
    setErrors((current) => ({ ...current, [field]: '' }))
    setStatus('')
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    const nextErrors = {
      newPassword: getStaffPasswordError(newPassword),
      confirmPassword: validatePasswordConfirmation(newPassword, confirmPassword),
    }
    setErrors(nextErrors)
    if (Object.values(nextErrors).some(Boolean)) return

    setSubmitting(true)
    try {
      await changeInitialPassword({ newPassword, confirmNewPassword: confirmPassword })
      setNewPassword('')
      setConfirmPassword('')
      navigate('/management/dashboard', { replace: true })
    } catch (error) {
      setStatus(error.message)
    } finally { setSubmitting(false) }
  }

  if (loading) return <p role="status">Checking your secure session...</p>
  if (!user) return <Navigate to="/staff/login" replace />
  if (!user.mustChangePassword) return <Navigate to="/management/dashboard" replace />

  return (
    <StaffAuthShell eyebrow="Account Security" title="Create Your New Password" description="For security, you need to create a new password before accessing the management portal.">
      <form className="staff-auth-form" noValidate onSubmit={handleSubmit}>
        <PasswordField id="initial-new-password" label="New Password" value={newPassword} onChange={updateField('newPassword', setNewPassword)} error={errors.newPassword} visible={visible.newPassword} onToggle={() => setVisible((current) => ({ ...current, newPassword: !current.newPassword }))} autoComplete="new-password" />
        <PasswordField id="initial-confirm-password" label="Confirm New Password" value={confirmPassword} onChange={updateField('confirmPassword', setConfirmPassword)} error={errors.confirmPassword} visible={visible.confirmPassword} onToggle={() => setVisible((current) => ({ ...current, confirmPassword: !current.confirmPassword }))} autoComplete="new-password" />
        <p className="staff-auth-note">Use 12 to 128 characters. Spaces and symbols are supported.</p>
        <button className="staff-auth-submit" type="submit" disabled={submitting}><KeyRound aria-hidden="true" size={18} />{submitting ? 'Saving...' : 'Create New Password'}</button>
        {status && <p className="staff-auth-status" role="alert" aria-live="polite">{status}</p>}
      </form>
    </StaffAuthShell>
  )
}

export default StaffChangePassword
