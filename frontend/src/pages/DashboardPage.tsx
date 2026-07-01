import { Link } from 'react-router-dom'
import { AppLayout } from '../components/AppLayout'
import { getUser } from '../lib/tokenStorage'

export function DashboardPage() {
  const user = getUser()

  return (
    <AppLayout eyebrow="Dashboard" title={`Welcome, ${user?.username ?? 'user'}`}>
        <div className="summary-grid">
          <article>
            <span>Identity</span>
            <strong>UserMS :1009</strong>
          </article>
          <article>
            <span>User id</span>
            <strong>{user?.userId ?? '-'}</strong>
          </article>
          <article>
            <span>Roles</span>
            <strong>{user?.roles.length ?? 0}</strong>
          </article>
        </div>

        <section className="section action-section">
          <div>
            <h3>Identity management</h3>
            <p className="muted">View the users and role permissions loaded from UserMS.</p>
          </div>
          <Link className="primary-link" to="/users">
            Open users
          </Link>
        </section>

        <section className="section action-section">
          <div>
            <h3>Metric management</h3>
            <p className="muted">Create and manage monitoring metrics loaded from MetricMS.</p>
          </div>
          <Link className="primary-link" to="/metrics">
            Open metrics
          </Link>
        </section>

        <section className="section action-section">
          <div>
            <h3>Action automation</h3>
            <p className="muted">Create scheduled email or SMS actions connected to metric conditions.</p>
          </div>
          <Link className="primary-link" to="/actions">
            Open actions
          </Link>
        </section>

        <section className="section action-section">
          <div>
            <h3>Pipeline status</h3>
            <p className="muted">Check which microservices are online before testing deeper workflows.</p>
          </div>
          <Link className="primary-link" to="/status">
            Open status
          </Link>
        </section>

        <section className="section">
          <h3>Permissions</h3>
          <div className="role-list">
            {user?.roles.map((role) => (
              <span key={role}>{role}</span>
            ))}
          </div>
        </section>
    </AppLayout>
  )
}
