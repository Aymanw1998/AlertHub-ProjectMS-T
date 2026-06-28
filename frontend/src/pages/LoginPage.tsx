import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthProvider'

type RouterState = {
  from?: {
    pathname?: string
  }
}

export function LoginPage() {
  const { isAuthenticated, signInWithPassword } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const state = location.state as RouterState | null

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const fromPath = state?.from?.pathname || '/dashboard'

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />
  }

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setErrorMessage(null)
    setIsSubmitting(true)

    try {
      await signInWithPassword(username, password)
      navigate(fromPath, { replace: true })
    } catch (error) {
      const message =
        error instanceof Error ? error.message : 'Signin failed. Try again.'
      setErrorMessage(message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="auth-container">
      <form className="auth-card" onSubmit={onSubmit}>
        <h1>AlertHub Login</h1>
        <p>Sign in through GatewayMS to access the dashboard.</p>

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
          Password
          <input
            autoComplete="current-password"
            onChange={(event) => setPassword(event.target.value)}
            required
            type="password"
            value={password}
          />
        </label>

        {errorMessage ? <p className="error-text">{errorMessage}</p> : null}

        <button disabled={isSubmitting} type="submit">
          {isSubmitting ? 'Signing in...' : 'Sign in'}
        </button>

        <p className="auth-link">
          Need an account? <Link to="/signup">Create one</Link>
        </p>
      </form>
    </div>
  )
}
