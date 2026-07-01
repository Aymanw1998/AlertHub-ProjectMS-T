import { useAuth } from '../auth/AuthProvider'

export function DashboardPage() {
  const { auth } = useAuth()

  return (
    <div className="card">
      <h2>Welcome to AlertHub</h2>
      <p>
        Frontend requests are configured to use GatewayMS at
        <code> http://localhost:1007</code>.
      </p>
      <p>
        Current user ID: <strong>{auth?.userId}</strong>
      </p>
      <p>
        Roles: <strong>{auth?.roles.join(', ') || 'none'}</strong>
      </p>
    </div>
  )
}
