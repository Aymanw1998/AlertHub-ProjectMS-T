import { useNavigate } from 'react-router-dom'
import { clearToken, getToken } from '../lib/tokenStorage'

export function DashboardPage() {
  const navigate = useNavigate()
  const token = getToken()

  function handleLogout() {
    clearToken()
    navigate('/login', { replace: true })
  }

  return (
    <main className="auth-shell">
      <section className="auth-card">
        <h1>Dashboard</h1>
        <p className="muted">You are logged in.</p>
        <p className="token-preview">
          JWT: <code>{token ?? 'No token found'}</code>
        </p>
        <button type="button" onClick={handleLogout}>
          Logout
        </button>
      </section>
    </main>
  )
}
