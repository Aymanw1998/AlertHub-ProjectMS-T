import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { login } from '../lib/authApi'
import { setToken } from '../lib/tokenStorage'

const ADMIN_USERNAME = 'admin'
const ADMIN_PASSWORD = 'admin'

export function LoginPage() {
  const navigate = useNavigate()
  const [username, setUsername] = useState(ADMIN_USERNAME)
  const [password, setPassword] = useState(ADMIN_PASSWORD)
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setIsSubmitting(true)

    try {
      const token = await login({ username, password })
      setToken(token)
      navigate('/dashboard', { replace: true })
    } catch (submitError) {
      if (submitError instanceof Error) {
        setError(submitError.message)
      } else {
        setError('Login failed.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="auth-shell">
      <section className="auth-card">
        <h1>Login</h1>
        <p className="muted">Use admin/admin for local testing.</p>
        <form onSubmit={handleSubmit} className="auth-form">
          <label>
            Username
            <input
              type="text"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              required
            />
          </label>
          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </label>
          {error && <p className="error">{error}</p>}
          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Signing in...' : 'Sign in'}
          </button>
        </form>
        <p className="muted">
          No account yet? <Link to="/signup">Sign up</Link>
        </p>
      </section>
    </main>
  )
}
