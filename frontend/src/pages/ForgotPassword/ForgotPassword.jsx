import { ArrowLeft, Eye, EyeOff, KeyRound, LockKeyhole, Mail } from "lucide-react";
import { useState } from "react";
import { Link, useNavigate } from "react-router";

import AuthLayout from "../../components/AuthLayout/AuthLayout.jsx";
import { authApi } from "../../services/authApi.js";
import { getPasswordChecks, isValidEmail } from "../../utils/authValidation.js";
import "./ForgotPassword.css";

function ForgotPassword() {
  const navigate = useNavigate();
  const [step, setStep] = useState("email");
  const [email, setEmail] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [errors, setErrors] = useState({});
  const [status, setStatus] = useState({ type: "", text: "" });
  const [submitting, setSubmitting] = useState(false);

  const clearFieldError = (field) => {
    setErrors((current) => {
      if (!current[field]) return current;
      const next = { ...current };
      delete next[field];
      return next;
    });
    setStatus({ type: "", text: "" });
  };

  const checkEmail = async (event) => {
    event.preventDefault();
    if (submitting) return;
    const trimmedEmail = email.trim();
    if (!trimmedEmail || !isValidEmail(trimmedEmail)) {
      setErrors({ email: trimmedEmail ? "Enter a valid email address." : "Email address is required." });
      return;
    }

    setSubmitting(true);
    setErrors({});
    setStatus({ type: "", text: "" });
    try {
      const result = await authApi.checkCustomerForgotPasswordEmail({ email: trimmedEmail });
      if (!result.exists) {
        setStatus({ type: "error", text: "No account found with this email." });
        return;
      }
      setEmail(trimmedEmail);
      setStep("password");
    } catch (error) {
      setStatus({ type: "error", text: error.body?.message || error.message || "Could not check this email." });
    } finally {
      setSubmitting(false);
    }
  };

  const changePassword = async (event) => {
    event.preventDefault();
    if (submitting) return;
    const checks = getPasswordChecks(newPassword);
    const nextErrors = {};
    if (!newPassword) nextErrors.newPassword = "New password is required.";
    else if (!checks.length || !checks.uppercase || !checks.lowercase || !checks.number || !checks.special) {
      nextErrors.newPassword = "Use 8-128 characters with uppercase, lowercase, number, and special character.";
    }
    if (!confirmPassword) nextErrors.confirmPassword = "Confirm password is required.";
    else if (confirmPassword !== newPassword) nextErrors.confirmPassword = "Passwords do not match.";
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length) return;

    setSubmitting(true);
    setStatus({ type: "", text: "" });
    try {
      await authApi.changeCustomerForgottenPassword({ email, newPassword, confirmPassword });
      setStatus({ type: "success", text: "Password changed successfully." });
      setTimeout(() => navigate("/login", { replace: true }), 1200);
    } catch (error) {
      setStatus({ type: "error", text: error.body?.message || error.message || "Could not change password." });
    } finally {
      setSubmitting(false);
    }
  };

  const passwordField = (id, label, value, setter, visible, toggle, errorKey) => (
    <div className="auth-field">
      <label htmlFor={id}>{label} <span aria-hidden="true">*</span></label>
      <div className="auth-input-wrap">
        <LockKeyhole aria-hidden="true" size={17} />
        <input id={id} type={visible ? "text" : "password"} autoComplete="new-password" value={value}
          aria-invalid={Boolean(errors[errorKey])} aria-describedby={errors[errorKey] ? `${id}-error` : undefined}
          onChange={(event) => { setter(event.target.value); clearFieldError(errorKey); }} />
        <button className="auth-password-toggle" type="button" aria-label={visible ? `Hide ${label.toLowerCase()}` : `Show ${label.toLowerCase()}`} onClick={toggle}>
          {visible ? <EyeOff aria-hidden="true" size={18} /> : <Eye aria-hidden="true" size={18} />}
        </button>
      </div>
      {errors[errorKey] && <span className="auth-field-error" id={`${id}-error`}>{errors[errorKey]}</span>}
    </div>
  );

  return (
    <AuthLayout compact>
      <div className="forgot-password-icon" aria-hidden="true"><KeyRound size={23} /></div>
      <header className="auth-form-header forgot-password-header">
        <span className="auth-form-eyebrow">Account Recovery</span>
        <h1>{step === "email" ? "Forgot Password" : "Reset Password"}</h1>
        <p>{step === "email" ? "Enter the email address used for your LankaStay account." : `Create a new password for ${email}.`}</p>
      </header>

      {step === "email" ? (
        <form className="auth-form forgot-password-form" noValidate onSubmit={checkEmail}>
          <div className="auth-field">
            <label htmlFor="forgot-email">Email Address <span aria-hidden="true">*</span></label>
            <div className="auth-input-wrap">
              <Mail aria-hidden="true" size={17} />
              <input id="forgot-email" type="email" autoComplete="email" placeholder="name@example.com" value={email}
                aria-invalid={Boolean(errors.email)} aria-describedby={errors.email ? "forgot-email-error" : undefined}
                onChange={(event) => { setEmail(event.target.value); clearFieldError("email"); }} />
            </div>
            {errors.email && <span className="auth-field-error" id="forgot-email-error">{errors.email}</span>}
          </div>
          <button className="auth-submit-button" type="submit" disabled={submitting}><KeyRound aria-hidden="true" size={17} />{submitting ? "Checking..." : "Continue"}</button>
          {status.text && <p className={`auth-form-status auth-form-status--${status.type}`} role="alert">{status.text}</p>}
        </form>
      ) : (
        <form className="auth-form forgot-password-form" noValidate onSubmit={changePassword}>
          {passwordField("forgot-new-password", "New Password", newPassword, setNewPassword, showPassword, () => setShowPassword((value) => !value), "newPassword")}
          {passwordField("forgot-confirm-password", "Confirm Password", confirmPassword, setConfirmPassword, showConfirmPassword, () => setShowConfirmPassword((value) => !value), "confirmPassword")}
          <p className="forgot-password-requirements">Use 8-128 characters with uppercase, lowercase, number, and special character.</p>
          <button className="auth-submit-button" type="submit" disabled={submitting}><KeyRound aria-hidden="true" size={17} />{submitting ? "Changing Password..." : "Change Password"}</button>
          {status.text && <p className={`auth-form-status auth-form-status--${status.type}`} role={status.type === "error" ? "alert" : "status"}>{status.text}</p>}
        </form>
      )}

      <Link className="forgot-back-link" to="/login"><ArrowLeft aria-hidden="true" size={15} />Back to Sign In</Link>
    </AuthLayout>
  );
}

export default ForgotPassword;
