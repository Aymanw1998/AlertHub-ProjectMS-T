import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

export function SignupPage() {
  const { signupNewUser } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setErrorMessage(null)
    setSuccessMessage(null)
    setIsSubmitting(true)

    try {
      await signupNewUser({ username, email, phone, password })
      setSuccessMessage('Signup successful. You can now sign in.')
      setTimeout(() => navigate('/login'), 900)
    } catch (error) {
      const message =
        error instanceof Error ? error.message : 'Signup failed. Try again.'
      setErrorMessage(message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="auth-container">
      <form className="auth-card" onSubmit={onSubmit}>
        <div className="auth-brand">
          <div className="brand-mark">AH</div>
          <div>
            <h1>Create account</h1>
            <p>Register through GatewayMS and SecurityMS.</p>
          </div>
        </div>

        <label className="form-field">
          Username
          <input
            autoComplete="username"
            onChange={(event) => setUsername(event.target.value)}
            required
            value={username}
          />
        </label>

        <label className="form-field">
          Email
          <input
            autoComplete="email"
            onChange={(event) => setEmail(event.target.value)}
            required
            type="email"
            value={email}
          />
        </label>

        <label className="form-field">
          Phone
          <input
            autoComplete="tel"
            onChange={(event) => setPhone(event.target.value)}
            required
            value={phone}
          />
        </label>

        <label className="form-field">
          Password
          <input
            autoComplete="new-password"
            minLength={6}
            onChange={(event) => setPassword(event.target.value)}
            required
            type="password"
            value={password}
          />
        </label>

        {errorMessage ? <p className="error-text">{errorMessage}</p> : null}
        {successMessage ? <p className="success-text">{successMessage}</p> : null}

        <button disabled={isSubmitting} type="submit">
          {isSubmitting ? 'Creating...' : 'Create account'}
        </button>

        <p className="auth-link">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </form>
    </div>
  )
}
