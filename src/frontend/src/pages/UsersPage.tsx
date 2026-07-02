import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import {
  addUserRoles,
  createUser,
  deleteUser,
  listRoles,
  listUsers,
  removeUserRoles,
  type RoleRecord,
  type UserAccount,
  type UserPayload,
} from '../api/alerthubApi'
import { useAuth } from '../auth/useAuth'
import {
  EmptyState,
  InlineAlert,
  PageHeader,
  StatCard,
  StatusBadge,
} from '../components/ui'
import { normalizeError } from '../lib/format'

type UserForm = {
  username: string
  email: string
  phone: string
  password: string
  roles: string[]
}

const defaultUserForm: UserForm = {
  username: '',
  email: '',
  phone: '',
  password: '',
  roles: ['read'],
}

function roleNames(user: UserAccount) {
  return user.roles.map((role) => role.role)
}

function isAdminUser(user: UserAccount) {
  return user.username.trim().toLowerCase() === 'admin'
}

function isReservedAdminUsername(username: string) {
  return username.trim().toLowerCase() === 'admin'
}

export function UsersPage() {
  const { auth } = useAuth()
  const [users, setUsers] = useState<UserAccount[]>([])
  const [roles, setRoles] = useState<RoleRecord[]>([])
  const [form, setForm] = useState<UserForm>(defaultUserForm)
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null)
  const [selectedRoles, setSelectedRoles] = useState<string[]>([])
  const [message, setMessage] = useState<string | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const loadUsers = useCallback(async () => {
    if (!auth?.token) {
      return
    }

    setIsLoading(true)
    setErrorMessage(null)
    try {
      const [nextUsers, nextRoles] = await Promise.all([
        listUsers(auth.token),
        listRoles(auth.token),
      ])
      setUsers(nextUsers)
      setRoles(nextRoles)
      const editableUsers = nextUsers.filter((user) => !isAdminUser(user))
      const currentUserIsEditable = editableUsers.some(
        (user) => user.id === selectedUserId,
      )

      if (!currentUserIsEditable && editableUsers.length > 0) {
        setSelectedUserId(editableUsers[0].id)
        setSelectedRoles(roleNames(editableUsers[0]))
      }

      if (editableUsers.length === 0) {
        setSelectedUserId(null)
        setSelectedRoles([])
      }
    } catch (error) {
      setErrorMessage(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }, [auth, selectedUserId])

  useEffect(() => {
    void loadUsers()
  }, [loadUsers])

  const editableUsers = useMemo(
    () => users.filter((user) => !isAdminUser(user)),
    [users],
  )

  const selectedUser = useMemo(
    () => editableUsers.find((user) => user.id === selectedUserId) ?? null,
    [editableUsers, selectedUserId],
  )

  async function onCreateUser(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!auth?.token) {
      return
    }

    if (isReservedAdminUsername(form.username)) {
      setMessage(null)
      setErrorMessage('The admin account is protected and cannot be created or edited here.')
      return
    }

    const payload: UserPayload = {
      username: form.username.trim(),
      email: form.email.trim(),
      phone: form.phone.trim(),
      password: form.password,
      roles: form.roles,
    }

    setMessage(null)
    setErrorMessage(null)
    try {
      await createUser(auth.token, payload)
      setMessage('User created successfully.')
      setForm(defaultUserForm)
      await loadUsers()
    } catch (error) {
      setErrorMessage(normalizeError(error))
    }
  }

  async function onDeleteUser(userId: number) {
    if (!auth?.token) {
      return
    }

    const user = users.find((item) => item.id === userId)
    if (user && isAdminUser(user)) {
      setMessage(null)
      setErrorMessage('The admin account is protected and cannot be deleted here.')
      return
    }

    setMessage(null)
    setErrorMessage(null)
    try {
      await deleteUser(auth.token, userId)
      setMessage('User deleted successfully.')
      await loadUsers()
    } catch (error) {
      setErrorMessage(normalizeError(error))
    }
  }

  async function onSaveRoles() {
    if (!auth?.token || !selectedUser || isAdminUser(selectedUser)) {
      return
    }

    const currentRoles = roleNames(selectedUser)
    const rolesToAdd = selectedRoles.filter((role) => !currentRoles.includes(role))
    const rolesToRemove = currentRoles.filter(
      (role) => !selectedRoles.includes(role),
    )

    setMessage(null)
    setErrorMessage(null)
    try {
      if (rolesToAdd.length > 0) {
        await addUserRoles(auth.token, selectedUser.id, rolesToAdd)
      }
      if (rolesToRemove.length > 0) {
        await removeUserRoles(auth.token, selectedUser.id, rolesToRemove)
      }
      setMessage('Permissions updated.')
      await loadUsers()
    } catch (error) {
      setErrorMessage(normalizeError(error))
    }
  }

  function toggleFormRole(roleName: string) {
    setForm((current) => ({
      ...current,
      roles: current.roles.includes(roleName)
        ? current.roles.filter((role) => role !== roleName)
        : [...current.roles, roleName],
    }))
  }

  function toggleSelectedRole(roleName: string) {
    setSelectedRoles((current) =>
      current.includes(roleName)
        ? current.filter((role) => role !== roleName)
        : [...current, roleName],
    )
  }

  if (auth?.username !== 'admin') {
    return (
      <>
        <PageHeader
          title="User Management"
          description="This area is available to the admin account only."
        />
        <InlineAlert
          message="GatewayMS protects /api/user/** and /api/role/** for the admin account."
          tone="orange"
        />
      </>
    )
  }

  return (
    <>
      <PageHeader
        title="User & Security Dashboard"
        description="Manage users and role-based permissions through UserMS."
        actions={
          <button className="secondary-button" onClick={loadUsers} type="button">
            Refresh
          </button>
        }
      />

      <InlineAlert message={message} tone="green" />
      <InlineAlert message={errorMessage} tone="red" />

      <section className="stats-grid">
        <StatCard
          detail="registered accounts"
          label="Total Users"
          value={isLoading ? '...' : editableUsers.length}
        />
        <StatCard
          detail="roles in UserMS"
          label="Permissions"
          tone="green"
          value={roles.length}
        />
        <StatCard
          detail="users with createAction"
          label="Action Creators"
          tone="purple"
          value={
            users.filter((user) => roleNames(user).includes('createAction'))
              .filter((user) => !isAdminUser(user)).length
          }
        />
      </section>

      <section className="dashboard-grid">
        <article className="panel">
          <div className="panel-header">
            <h2>Create New User</h2>
            <StatusBadge tone="blue">POST /api/user/create</StatusBadge>
          </div>
          <form className="form-grid" onSubmit={onCreateUser}>
            <label className="form-field">
              Username
              <input
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    username: event.target.value,
                  }))
                }
                required
                value={form.username}
              />
            </label>
            <label className="form-field">
              Email
              <input
                onChange={(event) =>
                  setForm((current) => ({ ...current, email: event.target.value }))
                }
                required
                type="email"
                value={form.email}
              />
            </label>
            <label className="form-field">
              Phone
              <input
                onChange={(event) =>
                  setForm((current) => ({ ...current, phone: event.target.value }))
                }
                required
                value={form.phone}
              />
            </label>
            <label className="form-field">
              Password
              <input
                minLength={6}
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    password: event.target.value,
                  }))
                }
                required
                type="password"
                value={form.password}
              />
            </label>
            <div className="metric-picker full-width">
              <div className="chip-grid">
                {roles.map((role) => (
                  <button
                    className={
                      form.roles.includes(role.role)
                        ? 'select-chip selected'
                        : 'select-chip'
                    }
                    key={role.id}
                    onClick={() => toggleFormRole(role.role)}
                    type="button"
                  >
                    {role.role}
                  </button>
                ))}
              </div>
            </div>
            <div className="form-actions">
              <button type="submit">Create User</button>
            </div>
          </form>
        </article>

        <article className="panel">
          <div className="panel-header">
            <h2>Manage User Permissions</h2>
            {selectedUser ? (
              <StatusBadge tone="green">{selectedUser.username}</StatusBadge>
            ) : null}
          </div>
          <div className="permission-list">
            {selectedUser ? (
              roles.map((role) => (
                <label className="permission-row" key={role.id}>
                  <span>{role.role}</span>
                  <input
                    checked={selectedRoles.includes(role.role)}
                    onChange={() => toggleSelectedRole(role.role)}
                    type="checkbox"
                  />
                </label>
              ))
            ) : (
              <EmptyState message="No editable users. The admin account is hidden and protected." />
            )}
          </div>
          <button disabled={!selectedUser} onClick={onSaveRoles} type="button">
            Save Changes
          </button>
        </article>
      </section>

      <section className="panel">
        <div className="panel-header">
          <h2>Registered Users</h2>
          <StatusBadge tone="blue">{editableUsers.length} users</StatusBadge>
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
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {editableUsers.map((user) => (
                <tr key={user.id}>
                  <td>{user.id}</td>
                  <td>{user.username}</td>
                  <td>{user.email}</td>
                  <td>{user.phone}</td>
                  <td>
                    <div className="role-cell">
                      {user.roles.map((role) => (
                        <StatusBadge key={role.id} tone="slate">
                          {role.role}
                        </StatusBadge>
                      ))}
                    </div>
                  </td>
                  <td className="table-actions">
                    <button
                      className="icon-button"
                      onClick={() => {
                        setSelectedUserId(user.id)
                        setSelectedRoles(roleNames(user))
                      }}
                      type="button"
                    >
                      Permissions
                    </button>
                    <button
                      className="danger-button"
                      onClick={() => void onDeleteUser(user.id)}
                      type="button"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {editableUsers.length === 0 ? (
          <EmptyState message="No editable users returned from UserMS. The admin account is intentionally hidden." />
        ) : null}
      </section>
    </>
  )
}
