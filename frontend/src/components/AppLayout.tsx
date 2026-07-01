import { NavLink, useNavigate } from 'react-router-dom'
import { clearAuth } from '../lib/tokenStorage'

type AppLayoutProps = {
  eyebrow: string
  title: string
  children: React.ReactNode
}

export function AppLayout({ eyebrow, title, children }: AppLayoutProps) {
  const navigate = useNavigate()

  function logout() {
    clearAuth()
    navigate('/login')
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <h1>Alert Hub</h1>
        <nav>
          <NavLink to="/dashboard">Dashboard</NavLink>
          <NavLink to="/users">Users & Roles</NavLink>
          <NavLink to="/metrics">Metrics</NavLink>
          <NavLink to="/actions">Actions</NavLink>
          <NavLink to="/status">Pipeline Status</NavLink>
        </nav>
      </aside>

      <section className="content">
        <header className="topbar">
          <div>
            <p className="eyebrow">{eyebrow}</p>
            <h2>{title}</h2>
          </div>
          <button className="secondary-button" onClick={logout}>
            Logout
          </button>
        </header>

        {children}
      </section>
    </main>
  )
}
