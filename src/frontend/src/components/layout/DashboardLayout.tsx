import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../../auth/AuthProvider'
import { AppRoles } from '../../auth/roles'

type NavItem = {
  label: string
  path: string
  role?: string
}

const navItems: NavItem[] = [
  { label: 'Dashboard', path: '/dashboard' },
  { label: 'Loader', path: '/loader' },
  { label: 'Metrics', path: '/metrics' },
  { label: 'Actions', path: '/actions' },
  { label: 'Logs', path: '/logs' },
  { label: 'Evaluation', path: '/evaluation', role: AppRoles.triggerEvaluation },
  { label: 'Users', path: '/users', role: AppRoles.admin },
]

export function DashboardLayout() {
  const { auth, hasRole, logout } = useAuth()

  const availableItems = navItems.filter((item) =>
    item.role ? hasRole(item.role) : true,
  )

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <h1 className="sidebar-title">AlertHub</h1>
        <p className="sidebar-subtitle">Gateway-based operations console</p>
        <nav className="sidebar-nav">
          {availableItems.map((item) => (
            <NavLink
              key={item.path}
              className={({ isActive }) =>
                `nav-link${isActive ? ' nav-link-active' : ''}`
              }
              to={item.path}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <main className="content">
        <header className="topbar">
          <div>
            <p className="topbar-label">Signed in as</p>
            <p className="topbar-user">{auth?.username}</p>
          </div>
          <button className="logout-button" onClick={logout} type="button">
            Logout
          </button>
        </header>
        <section className="page-content">
          <Outlet />
        </section>
      </main>
    </div>
  )
}
