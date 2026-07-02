import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../../auth/useAuth'
import { AppRoles } from '../../auth/roles'

type NavItem = {
  label: string
  path: string
  role?: string
  marker: string
}

const navItems: NavItem[] = [
  { label: 'Dashboard', path: '/dashboard', marker: 'DB' },
  { label: 'Loader', path: '/loader', marker: 'LD' },
  { label: 'Metrics', path: '/metrics', marker: 'MT' },
  { label: 'Actions', path: '/actions', marker: 'AC' },
  { label: 'Processor', path: '/processor', marker: 'PR', role: AppRoles.triggerProcess },
  { label: 'Notifications', path: '/notifications', marker: 'NT' },
  { label: 'Evaluation', path: '/evaluation', marker: 'EV', role: AppRoles.triggerEvaluation },
  { label: 'User Management', path: '/users', marker: 'US', role: AppRoles.admin },
  { label: 'Logger', path: '/logs', marker: 'LG' },
]

export function DashboardLayout() {
  const { auth, hasRole, logout } = useAuth()

  const availableItems = navItems.filter((item) =>
    item.role ? hasRole(item.role) : true,
  )

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand-block">
          <div className="brand-mark">AH</div>
          <div>
            <h1 className="sidebar-title">Alert Hub</h1>
            <p className="sidebar-subtitle">Microservices</p>
          </div>
        </div>
        <nav className="sidebar-nav">
          {availableItems.map((item) => (
            <NavLink
              key={item.path}
              className={({ isActive }) =>
                `nav-link${isActive ? ' nav-link-active' : ''}`
              }
              to={item.path}
            >
              <span className="nav-marker">{item.marker}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-user">
          <div className="avatar">{auth?.username.slice(0, 2).toUpperCase()}</div>
          <div>
            <strong>{auth?.username}</strong>
            <span>{auth?.username === 'admin' ? 'Administrator' : 'Operator'}</span>
          </div>
        </div>
      </aside>

      <main className="content">
        <header className="topbar">
          <div>
            <p className="topbar-label">Gateway</p>
            <p className="topbar-user">Connected through /api</p>
          </div>
          <div className="role-strip">
            {auth?.roles.slice(0, 4).map((role) => (
              <span key={role}>{role}</span>
            ))}
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
