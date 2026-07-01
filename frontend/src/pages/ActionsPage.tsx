import { type FormEvent, useEffect, useMemo, useState } from 'react'
import { AppLayout } from '../components/AppLayout'
import { hasPermission } from '../lib/permissions'
import {
  actionTypes,
  createAction,
  deleteAction,
  getActions,
  runOnDays,
  triggerAction,
  updateAction,
  type ActionRequest,
  type ActionType,
  type AlertAction,
  type RunOnDay,
} from '../lib/actionsApi'
import { getMetrics, type Metric } from '../lib/metricsApi'
import { getUser } from '../lib/tokenStorage'

function createEmptyActionForm(ownerId: string, condition = '[[1]]'): ActionRequest {
  return {
    owner_id: ownerId,
    name: '',
    action_type: 'email',
    run_on_time: '12:00:00',
    run_on_day: 'Wednesday',
    message: '',
    to: '',
    condition,
  }
}

function conditionToString(groups: number[][]) {
  const selectedGroups = groups
    .map((group) => [...new Set(group)].sort((a, b) => a - b))
    .filter((group) => group.length > 0)

  return JSON.stringify(selectedGroups)
}

function parseCondition(condition: string) {
  try {
    const value = JSON.parse(condition) as unknown
    if (
      Array.isArray(value) &&
      value.every((group) => Array.isArray(group) && group.every((id) => typeof id === 'number'))
    ) {
      return value as number[][]
    }
  } catch {
    return []
  }

  return []
}

export function ActionsPage() {
  const user = getUser()
  const ownerId = String(user?.userId ?? 1)
  const [actions, setActions] = useState<AlertAction[]>([])
  const [metrics, setMetrics] = useState<Metric[]>([])
  const [conditionGroups, setConditionGroups] = useState<number[][]>([[1]])
  const [form, setForm] = useState<ActionRequest>(() => createEmptyActionForm(ownerId))
  const [editingActionId, setEditingActionId] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [busyActionId, setBusyActionId] = useState<number | null>(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const canCreateAction = hasPermission('createAction')
  const canUpdateAction = hasPermission('updateAction')
  const canDeleteAction = hasPermission('deleteAction')
  const canTriggerAction = hasPermission('triggerProcess')
  const canUseActionForm = editingActionId ? canUpdateAction : canCreateAction

  const activeCount = useMemo(
    () => actions.filter((action) => action.is_enabled && !action.is_deleted).length,
    [actions],
  )
  const selectedMetricCount = useMemo(
    () => conditionGroups.reduce((count, group) => count + group.length, 0),
    [conditionGroups],
  )
  const metricNameById = useMemo(() => {
    return metrics.reduce<Record<number, string>>((names, metric) => {
      names[metric.id] = metric.name
      return names
    }, {})
  }, [metrics])

  async function loadData() {
    try {
      setLoading(true)
      setError('')
      const [actionsResponse, metricsResponse] = await Promise.all([getActions(), getMetrics()])
      setActions(actionsResponse)
      setMetrics(metricsResponse)

      if (metricsResponse.length > 0 && selectedMetricCount === 0) {
        const defaultGroups = [[metricsResponse[0].id]]
        setConditionGroups(defaultGroups)
        setForm((current) => ({
          ...current,
          condition: conditionToString(defaultGroups),
        }))
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not load actions')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  function updateForm(field: keyof ActionRequest, value: string) {
    setForm((current) => ({
      ...current,
      [field]: value,
    }))
  }

  function replaceConditionGroups(nextGroups: number[][]) {
    const safeGroups = nextGroups.length > 0 ? nextGroups : [[]]
    setConditionGroups(safeGroups)
    setForm((current) => ({
      ...current,
      condition: conditionToString(safeGroups),
    }))
  }

  function toggleMetric(groupIndex: number, metricId: number) {
    const nextGroups = conditionGroups.map((group, index) => {
      if (index !== groupIndex) {
        return group
      }

      return group.includes(metricId)
        ? group.filter((id) => id !== metricId)
        : [...group, metricId]
    })

    replaceConditionGroups(nextGroups)
  }

  function addConditionGroup() {
    replaceConditionGroups([...conditionGroups, []])
  }

  function removeConditionGroup(groupIndex: number) {
    replaceConditionGroups(conditionGroups.filter((_, index) => index !== groupIndex))
  }

  function resetForm() {
    const defaultGroups = metrics.length > 0 ? [[metrics[0].id]] : [[1]]
    setEditingActionId(null)
    setConditionGroups(defaultGroups)
    setForm(createEmptyActionForm(ownerId, conditionToString(defaultGroups)))
  }

  function startEditAction(action: AlertAction) {
    if (!canUpdateAction) {
      return
    }

    const parsedGroups = parseCondition(action.condition)
    const nextGroups = parsedGroups.length > 0 ? parsedGroups : [[]]

    setEditingActionId(action.id)
    setConditionGroups(nextGroups)
    setForm({
      owner_id: action.owner_id,
      name: action.name,
      action_type: action.action_type,
      run_on_time: action.run_on_time,
      run_on_day: action.run_on_day,
      message: action.message,
      to: action.to,
      condition: conditionToString(nextGroups),
    })
    setError('')
    setNotice('')
  }

  async function handleSaveAction(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const condition = conditionToString(conditionGroups)

    if (!canUseActionForm) {
      setError('You do not have permission to save actions')
      return
    }

    if (condition === '[]') {
      setError('Select at least one metric condition')
      return
    }

    try {
      setSaving(true)
      setError('')
      setNotice('')
      const payload = {
        ...form,
        owner_id: editingActionId ? form.owner_id : ownerId,
        condition,
      }

      if (editingActionId) {
        await updateAction(editingActionId, payload)
        setNotice(`Action ${form.name} updated`)
      } else {
        await createAction(payload)
        setNotice(`Action ${form.name} created`)
      }

      resetForm()
      await loadData()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not save action')
    } finally {
      setSaving(false)
    }
  }

  async function handleTriggerAction(action: AlertAction) {
    if (!canTriggerAction) {
      return
    }

    try {
      setBusyActionId(action.id)
      setError('')
      setNotice('')
      await triggerAction(action.id)
      setNotice(`Action ${action.name} triggered`)
      await loadData()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not trigger action')
    } finally {
      setBusyActionId(null)
    }
  }

  async function handleDeleteAction(action: AlertAction) {
    if (!canDeleteAction) {
      return
    }

    const confirmed = window.confirm(`Delete action "${action.name}"?`)
    if (!confirmed) {
      return
    }

    try {
      setBusyActionId(action.id)
      setError('')
      setNotice('')
      await deleteAction(action.id)
      setNotice(`Action ${action.name} deleted`)
      await loadData()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not delete action')
    } finally {
      setBusyActionId(null)
    }
  }

  function formatCondition(condition: string) {
    const groups = parseCondition(condition)

    if (groups.length === 0) {
      return condition
    }

    return groups
      .map((group) =>
        group
          .map((metricId) => metricNameById[metricId] ?? `Metric #${metricId}`)
          .join(' AND '),
      )
      .join(' OR ')
  }

  return (
    <AppLayout eyebrow="Automation" title="Actions">
      <div className="summary-grid">
        <article>
          <span>Actions</span>
          <strong>{actions.length}</strong>
        </article>
        <article>
          <span>Active</span>
          <strong>{activeCount}</strong>
        </article>
        <article>
          <span>Metrics available</span>
          <strong>{metrics.length}</strong>
        </article>
      </div>

      {error && <p className="error">{error}</p>}
      {notice && <p className="success">{notice}</p>}
      {!canCreateAction && !canUpdateAction && (
        <p className="notice">You can view actions, but your user does not have action write permissions.</p>
      )}

      {(canCreateAction || canUpdateAction) && (
      <section className="section">
        <div className="section-header">
          <h3>{editingActionId ? 'Edit action' : 'Create action'}</h3>
          <div className="section-actions">
            {editingActionId && (
              <button className="secondary-small-button" type="button" onClick={resetForm}>
                Cancel edit
              </button>
            )}
            <span className="status-pill">Time must end in :00 or :30</span>
          </div>
        </div>

        <form className="management-form" onSubmit={handleSaveAction}>
          <label>
            Owner ID
            <input disabled value={ownerId} />
          </label>
          <label>
            Name
            <input
              required
              value={form.name}
              onChange={(event) => updateForm('name', event.target.value)}
            />
          </label>
          <label>
            Type
            <select
              value={form.action_type}
              onChange={(event) => updateForm('action_type', event.target.value as ActionType)}
            >
              {actionTypes.map((type) => (
                <option key={type} value={type}>
                  {type}
                </option>
              ))}
            </select>
          </label>
          <label>
            Run day
            <select
              value={form.run_on_day}
              onChange={(event) => updateForm('run_on_day', event.target.value as RunOnDay)}
            >
              {runOnDays.map((day) => (
                <option key={day} value={day}>
                  {day}
                </option>
              ))}
            </select>
          </label>
          <label>
            Run time
            <input
              required
              step="1800"
              type="time"
              value={form.run_on_time.slice(0, 5)}
              onChange={(event) => updateForm('run_on_time', `${event.target.value}:00`)}
            />
          </label>
          <label>
            To
            <input
              required
              value={form.to}
              onChange={(event) => updateForm('to', event.target.value)}
            />
          </label>
          <label className="wide-field">
            Message
            <input
              required
              value={form.message}
              onChange={(event) => updateForm('message', event.target.value)}
            />
          </label>

          <fieldset className="condition-builder">
            <legend>Condition</legend>
            {conditionGroups.map((group, groupIndex) => (
              <div key={groupIndex} className="condition-group">
                <div className="condition-group-header">
                  <strong>Group {groupIndex + 1}</strong>
                  <button
                    className="secondary-small-button"
                    type="button"
                    disabled={conditionGroups.length === 1}
                    onClick={() => removeConditionGroup(groupIndex)}
                  >
                    Remove group
                  </button>
                </div>
                <div className="checkbox-grid">
                  {metrics.map((metric) => (
                    <label key={metric.id} className="checkbox-row">
                      <input
                        type="checkbox"
                        checked={group.includes(metric.id)}
                        onChange={() => toggleMetric(groupIndex, metric.id)}
                      />
                      <span>
                        #{metric.id} {metric.name}
                      </span>
                    </label>
                  ))}
                </div>
              </div>
            ))}

            <div className="condition-footer">
              <button className="secondary-small-button" type="button" onClick={addConditionGroup}>
                Add OR group
              </button>
              <code>{conditionToString(conditionGroups)}</code>
            </div>
          </fieldset>

          <button disabled={saving || !canUseActionForm}>
            {saving ? 'Saving...' : editingActionId ? 'Save action' : 'Create action'}
          </button>
        </form>
      </section>
      )}

      <section className="section">
        <div className="section-header">
          <h3>All actions</h3>
          {loading && <span className="status-pill">Loading</span>}
        </div>

        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Type</th>
                <th>Run</th>
                <th>To</th>
                <th>Condition</th>
                <th>Status</th>
                <th>Last run</th>
                {(canUpdateAction || canTriggerAction || canDeleteAction) && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {actions.map((action) => (
                <tr key={action.id} className={action.is_deleted ? 'deleted-row' : undefined}>
                  <td>{action.id}</td>
                  <td>{action.name}</td>
                  <td>
                    <span className="label-pill">{action.action_type}</span>
                  </td>
                  <td>
                    {action.run_on_day} {action.run_on_time}
                  </td>
                  <td>{action.to}</td>
                  <td>
                    <span className="condition-text">{formatCondition(action.condition)}</span>
                    <code className="condition-code">{action.condition}</code>
                  </td>
                  <td>{action.is_deleted ? 'Deleted' : action.is_enabled ? 'Enabled' : 'Disabled'}</td>
                  <td>{action.last_run ?? '-'}</td>
                  {(canUpdateAction || canTriggerAction || canDeleteAction) && (
                  <td>
                    <div className="row-actions">
                      {canUpdateAction && (
                        <button
                          disabled={busyActionId === action.id || action.is_deleted}
                          onClick={() => startEditAction(action)}
                        >
                          Edit
                        </button>
                      )}
                      {canTriggerAction && (
                        <button
                          disabled={busyActionId === action.id || action.is_deleted}
                          onClick={() => handleTriggerAction(action)}
                        >
                          Trigger
                        </button>
                      )}
                      {canDeleteAction && (
                        <button
                          className="danger-button"
                          disabled={busyActionId === action.id || action.is_deleted}
                          onClick={() => handleDeleteAction(action)}
                        >
                          Delete
                        </button>
                      )}
                    </div>
                  </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </AppLayout>
  )
}
