import { ArrowLeft, KeyRound, Mail } from "lucide-react";
import { useState } from "react";
import { Link } from "react-router";

import AuthLayout from "../../components/AuthLayout/AuthLayout.jsx";
import { authApi } from "../../services/authApi.js";
import { isValidEmail } from "../../utils/authValidation.js";
import "./ForgotPassword.css";

function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [status, setStatus] = useState({ type: "", text: "" });

  const handlePasswordReset = (event) => {
    event.preventDefault();
    const trimmedEmail = email.trim();

    if (!trimmedEmail) {
      setError("Email address is required.");
      setStatus({ type: "error", text: "Please enter your email address." });
      return;
    }
    if (!isValidEmail(trimmedEmail)) {
      setError("Enter a valid email address.");
      setStatus({ type: "error", text: "Please review the highlighted field." });
      return;
    }

    setError("");
    authApi.forgotPassword({ email: trimmedEmail })
      .then((res) => {
        setStatus({
          type: "success",
          text: res.message || "If an account exists for that email, password reset instructions have been sent.",
        });
      })
      .catch((err) => {
        const msg = err.body?.message || err.message || "Could not request password reset.";
        setStatus({ type: "error", text: msg });
      });
  };

  return (
    <AuthLayout compact>
      <div className="forgot-password-icon" aria-hidden="true">
        <KeyRound size={23} />
      </div>
      <header className="auth-form-header forgot-password-header">
        <span className="auth-form-eyebrow">Account Recovery</span>
        <h1>Forgot Your Password?</h1>
        <p>Enter your email address and we’ll prepare a password reset request.</p>
      </header>

      <form className="auth-form forgot-password-form" noValidate onSubmit={handlePasswordReset}>
        <div className="auth-field">
          <label htmlFor="forgot-email">Email Address <span aria-hidden="true">*</span></label>
          <div className="auth-input-wrap">
            <Mail aria-hidden="true" size={17} />
            <input
              id="forgot-email"
              name="email"
              type="email"
              autoComplete="email"
              placeholder="name@example.com"
              value={email}
              aria-invalid={Boolean(error)}
              aria-describedby={error ? "forgot-email-error" : undefined}
              onChange={(event) => {
                setEmail(event.target.value);
                setError("");
                setStatus({ type: "", text: "" });
              }}
            />
          </div>
          {error && <span className="auth-field-error" id="forgot-email-error">{error}</span>}
        </div>

        <button className="auth-submit-button" type="submit">
          <KeyRound aria-hidden="true" size={17} />
          Request Password Reset
        </button>

        {status.text && (
          <p className={`auth-form-status auth-form-status--${status.type}`} role={status.type === "error" ? "alert" : "status"} aria-live="polite">
            {status.text}
          </p>
        )}

        <Link className="forgot-back-link" to="/login">
          <ArrowLeft aria-hidden="true" size={15} />
          Back to Sign In
        </Link>
      </form>
    </AuthLayout>
  );
}

export default ForgotPassword;
