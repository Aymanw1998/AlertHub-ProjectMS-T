import { type FormEvent, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { AppLayout } from '../components/AppLayout'
import { hasAllPermissions } from '../lib/permissions'
import {
  createUser,
  deleteUser,
  getRoles,
  getUsers,
  updateUser,
  type RoleResponseDTO,
  type UserRequestDTO,
  type UserResponseDTO,
} from '../lib/usersApi'

const emptyUserForm: UserRequestDTO = {
  username: '',
  email: '',
  phone: '',
  password: '',
  roles: ['read'],
}

export function UsersPage() {
  const [users, setUsers] = useState<UserResponseDTO[]>([])
  const [roles, setRoles] = useState<RoleResponseDTO[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [form, setForm] = useState<UserRequestDTO>(emptyUserForm)
  const [editingUserId, setEditingUserId] = useState<number | null>(null)
  const canManageUsers = hasAllPermissions([
    'createAction',
    'updateAction',
    'deleteAction',
    'createMetric',
    'updateMetric',
    'deleteMetric',
    'triggerProcess',
    'triggerScan',
    'triggerEvaluation',
  ])

  const roleCountByName = useMemo(() => {
    return users.reduce<Record<string, number>>((counts, user) => {
      user.roles.forEach((role) => {
        counts[role.role] = (counts[role.role] ?? 0) + 1
      })
      return counts
    }, {})
  }, [users])

  async function loadData() {
    try {
      setLoading(true)
      setError('')
      const [usersResponse, rolesResponse] = await Promise.all([getUsers(), getRoles()])

      setUsers(usersResponse)
      setRoles(rolesResponse)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not load users')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  function updateForm(field: keyof UserRequestDTO, value: string) {
    setForm((current) => ({
      ...current,
      [field]: value,
    }))
  }

  function toggleRole(roleName: string) {
    if (roleName === 'read') {
      return
    }

    setForm((current) => {
      const selected = current.roles.includes(roleName)
      const rolesNext = selected
        ? current.roles.filter((role) => role !== roleName)
        : [...current.roles, roleName]

      return {
        ...current,
        roles: rolesNext,
      }
    })
  }

  function resetForm() {
    setEditingUserId(null)
    setForm(emptyUserForm)
  }

  function startEditUser(user: UserResponseDTO) {
    if (!canManageUsers || user.username === 'admin') {
      return
    }

    setEditingUserId(user.id)
    setForm({
      username: user.username,
      email: user.email,
      phone: user.phone,
      password: '',
      roles: user.roles.map((role) => role.role),
    })
    setError('')
    setNotice('')
  }

  async function handleSaveUser(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!canManageUsers) {
      setError('You do not have permission to manage users')
      return
    }

    try {
      setSaving(true)
      setError('')
      setNotice('')
      const payload = {
        ...form,
        roles: form.roles.includes('read') ? form.roles : [...form.roles, 'read'],
      }

      if (editingUserId) {
        await updateUser(editingUserId, payload)
        setNotice(`User ${form.username} updated`)
      } else {
        await createUser(payload)
        setNotice(`User ${form.username} created`)
      }

      resetForm()
      await loadData()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not save user')
    } finally {
      setSaving(false)
    }
  }

  async function handleDeleteUser(user: UserResponseDTO) {
    if (!canManageUsers) {
      return
    }

    if (user.username === 'admin') {
      return
    }

    const confirmed = window.confirm(`Delete user "${user.username}"?`)
    if (!confirmed) {
      return
    }

    try {
      setError('')
      setNotice('')
      await deleteUser(user.id)
      setNotice(`User ${user.username} deleted`)
      await loadData()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not delete user')
    }
  }

  return (
    <AppLayout eyebrow="Identity" title="Users & Roles">
        <div className="summary-grid">
          <article>
            <span>Users</span>
            <strong>{users.length}</strong>
          </article>
          <article>
            <span>Roles</span>
            <strong>{roles.length}</strong>
          </article>
          <article>
            <span>Service</span>
            <strong>UserMS :1009</strong>
          </article>
        </div>

        <div className="page-actions">
          <Link className="secondary-link" to="/dashboard">
            Dashboard
          </Link>
          <Link className="secondary-link" to="/metrics">
            Metrics
          </Link>
        </div>

        {error && <p className="error">{error}</p>}
        {notice && <p className="success">{notice}</p>}
        {!canManageUsers && (
          <p className="notice">You can view users, but user management is available only to admin-style users.</p>
        )}

        {canManageUsers && (
        <section className="section">
          <div className="section-header">
            <h3>{editingUserId ? 'Edit user roles' : 'Create user'}</h3>
            <div className="section-actions">
              {editingUserId && (
                <button className="secondary-small-button" type="button" onClick={resetForm}>
                  Cancel edit
                </button>
              )}
              <span className="status-pill">Read role is required</span>
            </div>
          </div>

          <form className="management-form" onSubmit={handleSaveUser}>
            <label>
              Username
              <input
                required
                value={form.username}
                onChange={(event) => updateForm('username', event.target.value)}
              />
            </label>
            <label>
              Email
              <input
                required
                type="email"
                value={form.email}
                onChange={(event) => updateForm('email', event.target.value)}
              />
            </label>
            <label>
              Phone
              <input
                required
                value={form.phone}
                onChange={(event) => updateForm('phone', event.target.value)}
              />
            </label>
            <label>
              Password
              <input
                required={!editingUserId}
                type="password"
                placeholder={editingUserId ? 'Leave empty to keep current password' : ''}
                value={form.password}
                onChange={(event) => updateForm('password', event.target.value)}
              />
            </label>

            <fieldset>
              <legend>Roles</legend>
              <div className="checkbox-grid">
                {roles.map((role) => (
                  <label key={role.id} className="checkbox-row">
                    <input
                      type="checkbox"
                      checked={form.roles.includes(role.role)}
                      disabled={role.role === 'read'}
                      onChange={() => toggleRole(role.role)}
                    />
                    <span>{role.role}</span>
                  </label>
                ))}
              </div>
            </fieldset>

            <button disabled={saving}>{saving ? 'Saving...' : editingUserId ? 'Save roles' : 'Create user'}</button>
          </form>
        </section>
        )}

        <section className="section">
          <div className="section-header">
            <h3>All users</h3>
            {loading && <span className="status-pill">Loading</span>}
          </div>

          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Username</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th>Roles</th>
                  {canManageUsers && <th>Action</th>}
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id}>
                    <td>{user.id}</td>
                    <td>{user.username}</td>
                    <td>{user.email}</td>
                    <td>{user.phone}</td>
                    <td>
                      <div className="role-list compact">
                        {user.roles.map((role) => (
                          <span key={role.id}>{role.role}</span>
                        ))}
                      </div>
                    </td>
                    {canManageUsers && (
                    <td>
                      <div className="row-actions">
                        <button
                          disabled={user.username === 'admin'}
                          onClick={() => startEditUser(user)}
                        >
                          Edit roles
                        </button>
                        <button
                          className="danger-button"
                          disabled={user.username === 'admin'}
                          onClick={() => handleDeleteUser(user)}
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        <section className="section">
          <h3>Available roles</h3>
          <div className="role-grid">
            {roles.map((role) => (
              <article key={role.id}>
                <strong>{role.role}</strong>
                <span>{roleCountByName[role.role] ?? 0} user(s)</span>
              </article>
            ))}
          </div>
        </section>
    </AppLayout>
  )
}
