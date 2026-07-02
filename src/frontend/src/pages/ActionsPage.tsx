import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import {
  ACTION_TYPES,
  RUN_DAYS,
  createAction,
  deleteAction,
  listActions,
  listMetrics,
  processAction,
  restoreAction,
  updateAction,
  type ActionPayload,
  type ActionRecord,
  type ActionType,
  type Metric,
  type RunDay,
} from '../api/alerthubApi'
import { useAuth } from '../auth/useAuth'
import {
  EmptyState,
  InlineAlert,
  PageHeader,
  StatCard,
  StatusBadge,
} from '../components/ui'
import { formatDateTime, normalizeError } from '../lib/format'

type ActionForm = {
  name: string
  action_type: ActionType
  run_on_time: string
  run_on_day: RunDay
  message: string
  to: string
  is_enabled: boolean
  condition: string
}

const defaultForm: ActionForm = {
  name: 'Bug Alert',
  action_type: 'email',
  run_on_time: '14:00',
  run_on_day: 'All',
  message: 'Too many matching tasks were detected.',
  to: 'manager@gmail.com',
  is_enabled: true,
  condition: '',
}

function normalizeTime(value: string) {
  return value.length === 5 ? `${value}:00` : value
}

function toInputTime(value: string) {
  return value ? value.slice(0, 5) : '14:00'
}

function conditionFromGroups(groups: number[][]) {
  const cleanedGroups = groups
    .map((group) => Array.from(new Set(group)).filter(Number.isFinite))
    .filter((group) => group.length > 0)

  return cleanedGroups.length > 0 ? JSON.stringify(cleanedGroups) : ''
}

function groupsFromCondition(condition: string) {
  try {
    const parsed = JSON.parse(condition) as unknown
    if (!Array.isArray(parsed)) {
      return []
    }

    return parsed
      .filter(Array.isArray)
      .map((group) =>
        group
          .map((value) => Number(value))
          .filter((value) => Number.isInteger(value) && value > 0),
      )
  } catch {
    return []
  }
}

export function ActionsPage() {
  const { auth, hasRole } = useAuth()
  const [actions, setActions] = useState<ActionRecord[]>([])
  const [metrics, setMetrics] = useState<Metric[]>([])
  const [form, setForm] = useState<ActionForm>(defaultForm)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [conditionGroups, setConditionGroups] = useState<number[][]>([])
  const [selectedActionId, setSelectedActionId] = useState<number | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [message, setMessage] = useState<string | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [query, setQuery] = useState('')

  const loadActions = useCallback(async () => {
    if (!auth?.token) {
      return
    }

    setIsLoading(true)
    setErrorMessage(null)
    try {
      const [nextActions, nextMetrics] = await Promise.all([
        listActions(auth.token),
        listMetrics(auth.token),
      ])
      setActions(nextActions)
      setMetrics(nextMetrics)
    } catch (error) {
      setErrorMessage(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }, [auth])

  useEffect(() => {
    void loadActions()
  }, [loadActions])

  useEffect(() => {
    if (metrics.length === 0 || conditionGroups.length > 0 || editingId) {
      return
    }

    const nextGroups = [[metrics[0].id]]
    setConditionGroups(nextGroups)
    setForm((current) => ({
      ...current,
      condition: conditionFromGroups(nextGroups),
    }))
  }, [conditionGroups.length, editingId, metrics])

  function toPayload(): ActionPayload {
    return {
      owner_id: String(auth?.userId ?? ''),
      name: form.name.trim(),
      action_type: form.action_type,
      run_on_time: normalizeTime(form.run_on_time),
      run_on_day: form.run_on_day,
      message: form.message.trim(),
      to: form.to.trim(),
      is_enabled: form.is_enabled,
      is_deleted: false,
      condition: form.condition.trim(),
    }
  }

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!auth?.token) {
      return
    }

    setMessage(null)
    setErrorMessage(null)
    try {
      if (editingId) {
        await updateAction(auth.token, editingId, toPayload())
        setMessage('Action updated successfully.')
      } else {
        await createAction(auth.token, toPayload())
        setMessage('Action created successfully.')
      }
      setEditingId(null)
      resetForm()
      await loadActions()
    } catch (error) {
      setErrorMessage(normalizeError(error))
    }
  }

  function resetForm() {
    const fallbackGroups = metrics.length > 0 ? [[metrics[0].id]] : []
    setConditionGroups(fallbackGroups)
    setForm({
      ...defaultForm,
      condition: conditionFromGroups(fallbackGroups),
    })
  }

  async function onDelete(actionId: number) {
    if (!auth?.token) {
      return
    }

    setErrorMessage(null)
    try {
      await deleteAction(auth.token, actionId)
      setMessage('Action soft-deleted.')
      await loadActions()
    } catch (error) {
      setErrorMessage(normalizeError(error))
    }
  }

  async function onProcess(actionId: number) {
    if (!auth?.token) {
      return
    }

    setErrorMessage(null)
    try {
      await processAction(auth.token, actionId)
      setMessage(`Action ${actionId} was pushed to processing.`)
      await loadActions()
    } catch (error) {
      setErrorMessage(normalizeError(error))
    }
  }

  async function onRestore(actionId: number) {
    if (!auth?.token) {
      return
    }

    setErrorMessage(null)
    try {
      await restoreAction(auth.token, actionId)
      setMessage(`Action ${actionId} was restored.`)
      await loadActions()
    } catch (error) {
      setErrorMessage(normalizeError(error))
    }
  }

  function applyConditionGroups(nextGroups: number[][]) {
    const safeGroups = nextGroups.length > 0 ? nextGroups : [[]]
    setConditionGroups(safeGroups)
    setForm((current) => ({
      ...current,
      condition: conditionFromGroups(safeGroups),
    }))
  }

  function toggleMetric(groupIndex: number, metricId: number) {
    applyConditionGroups(
      conditionGroups.map((group, index) => {
        if (index !== groupIndex) {
          return group
        }

        return group.includes(metricId)
          ? group.filter((id) => id !== metricId)
          : [...group, metricId]
      }),
    )
  }

  function addOrGroup() {
    applyConditionGroups([...conditionGroups, []])
  }

  function removeGroup(groupIndex: number) {
    applyConditionGroups(
      conditionGroups.filter((_, index) => index !== groupIndex),
    )
  }

  function updateConditionText(condition: string) {
    setForm((current) => ({ ...current, condition }))
    const parsedGroups = groupsFromCondition(condition)
    if (parsedGroups.length > 0) {
      setConditionGroups(parsedGroups)
    }
  }

  const filteredActions = useMemo(() => {
    const term = query.trim().toLowerCase()
    return term
      ? actions.filter((action) =>
          [
            action.name,
            action.action_type,
            action.to,
            action.condition,
            action.message,
          ]
            .join(' ')
            .toLowerCase()
            .includes(term),
        )
      : actions
  }, [actions, query])

  const activeActions = useMemo(
    () => actions.filter((action) => action.is_enabled && !action.is_deleted),
    [actions],
  )
  const selectedAction =
    actions.find((action) => action.id === selectedActionId) ?? null
  const canCreate = hasRole('createAction')
  const canUpdate = hasRole('updateAction')
  const canDelete = hasRole('deleteAction')
  const canProcess = hasRole('triggerProcess')

  useEffect(() => {
    if (activeActions.length === 0) {
      if (selectedActionId !== null) {
        setSelectedActionId(null)
      }
      return
    }

    if (
      !selectedActionId ||
      !activeActions.some((action) => action.id === selectedActionId)
    ) {
      setSelectedActionId(activeActions[0].id)
    }
  }, [activeActions, selectedActionId])

  return (
    <>
      <PageHeader
        title="Action Dashboard"
        description="Create automated actions based on MetricMS conditions."
        actions={
          <button className="secondary-button" onClick={loadActions} type="button">
            Refresh
          </button>
        }
      />

      <InlineAlert message={message} tone="green" />
      <InlineAlert message={errorMessage} tone="red" />

      <section className="stats-grid">
        <StatCard
          detail="all actions"
          label="Total Actions"
          value={isLoading ? '...' : actions.length}
        />
        <StatCard
          detail="enabled and not deleted"
          label="Active Actions"
          tone="green"
          value={activeActions.length}
        />
        <StatCard
          detail="available for condition builder"
          label="Metrics"
          tone="purple"
          value={metrics.length}
        />
        <StatCard
          detail="manually runnable"
          label="Processor Jobs"
          tone="orange"
          value={activeActions.length}
        />
      </section>

      <section className="panel selected-action-panel">
        <div className="panel-header">
          <h2>Run Selected Action</h2>
          <StatusBadge tone="blue">POST /api/action/process/:id</StatusBadge>
        </div>
        <div className="selected-action-controls">
          <label className="form-field">
            Choose Action
            <select
              onChange={(event) => setSelectedActionId(Number(event.target.value))}
              value={selectedActionId ?? ''}
            >
              {activeActions.map((action) => (
                <option key={action.id} value={action.id}>
                  #{action.id} - {action.name} ({action.condition})
                </option>
              ))}
            </select>
          </label>
          <button
            disabled={!selectedActionId || !canProcess || !selectedAction}
            onClick={() => selectedActionId && void onProcess(selectedActionId)}
            type="button"
          >
            Run Selected
          </button>
        </div>
        <p className="panel-note">
          This sends the selected ActionMS record to the processor queue. The
          backend updates last_run after the manual trigger succeeds.
        </p>
      </section>

      <section className="panel">
        <div className="panel-header">
          <h2>{editingId ? 'Update Action' : 'Create New Action'}</h2>
          <StatusBadge tone="blue">POST /api/action/create</StatusBadge>
        </div>
        <form className="form-grid action-form" onSubmit={onSubmit}>
          <label className="form-field">
            Action Name
            <input
              onChange={(event) =>
                setForm((current) => ({ ...current, name: event.target.value }))
              }
              required
              value={form.name}
            />
          </label>
          <label className="form-field">
            Type
            <select
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  action_type: event.target.value as ActionType,
                }))
              }
              value={form.action_type}
            >
              {ACTION_TYPES.map((type) => (
                <option key={type} value={type}>
                  {type}
                </option>
              ))}
            </select>
          </label>
          <label className="form-field">
            Recipient
            <input
              onChange={(event) =>
                setForm((current) => ({ ...current, to: event.target.value }))
              }
              required
              value={form.to}
            />
          </label>
          <label className="form-field">
            Run Time
            <input
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  run_on_time: event.target.value,
                }))
              }
              required
              step={1800}
              type="time"
              value={form.run_on_time}
            />
          </label>
          <label className="form-field">
            Run Day
            <select
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  run_on_day: event.target.value as RunDay,
                }))
              }
              value={form.run_on_day}
            >
              {RUN_DAYS.map((day) => (
                <option key={day} value={day}>
                  {day}
                </option>
              ))}
            </select>
          </label>
          <label className="form-field toggle-field">
            Enabled
            <input
              checked={form.is_enabled}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  is_enabled: event.target.checked,
                }))
              }
              type="checkbox"
            />
          </label>
          <label className="form-field full-width">
            Message
            <textarea
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  message: event.target.value,
                }))
              }
              required
              rows={3}
              value={form.message}
            />
          </label>
          <div className="metric-picker full-width">
            <div className="panel-header compact-header">
              <h3>Condition Builder</h3>
              <span>
                Metrics inside a group are AND. Separate groups are OR.
              </span>
            </div>
            <div className="condition-groups">
              {(conditionGroups.length > 0 ? conditionGroups : [[]]).map(
                (group, groupIndex) => (
                  <div className="condition-group" key={`group-${groupIndex}`}>
                    <div className="condition-group-header">
                      <strong>Group {groupIndex + 1}</strong>
                      <StatusBadge tone="purple">
                        {groupIndex === 0 ? 'AND' : 'OR group'}
                      </StatusBadge>
                      {conditionGroups.length > 1 ? (
                        <button
                          className="secondary-button"
                          onClick={() => removeGroup(groupIndex)}
                          type="button"
                        >
                          Remove
                        </button>
                      ) : null}
                    </div>
                    <div className="chip-grid">
                      {metrics.map((metric) => (
                        <button
                          className={
                            group.includes(metric.id)
                              ? 'select-chip selected'
                              : 'select-chip'
                          }
                          key={metric.id}
                          onClick={() => toggleMetric(groupIndex, metric.id)}
                          type="button"
                        >
                          #{metric.id} {metric.name}
                        </button>
                      ))}
                    </div>
                  </div>
                ),
              )}
              <button
                className="secondary-button add-or-button"
                onClick={addOrGroup}
                type="button"
              >
                Add OR Group
              </button>
            </div>
          </div>
          <label className="form-field full-width">
            Condition
            <input
              onChange={(event) => updateConditionText(event.target.value)}
              placeholder="[[1,2],[3]]"
              required
              value={form.condition}
            />
          </label>
          <div className="form-actions">
            <button disabled={editingId ? !canUpdate : !canCreate} type="submit">
              {editingId ? 'Save Action' : 'Create Action'}
            </button>
            {editingId ? (
              <button
                className="secondary-button"
                onClick={() => {
                  setEditingId(null)
                  resetForm()
                }}
                type="button"
              >
                Cancel
              </button>
            ) : null}
          </div>
        </form>
      </section>

      <section className="panel">
        <div className="panel-header">
          <h2>Saved Actions</h2>
          <input
            aria-label="Search actions"
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Search actions..."
            value={query}
          />
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Condition</th>
                <th>Type</th>
                <th>Recipient</th>
                <th>Run</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredActions.map((action) => (
                <tr key={action.id}>
                  <td>{action.id}</td>
                  <td>{action.name}</td>
                  <td>{action.condition}</td>
                  <td>{action.action_type}</td>
                  <td>{action.to}</td>
                  <td>
                    {action.run_on_day} {toInputTime(action.run_on_time)}
                  </td>
                  <td>
                    <StatusBadge
                      tone={
                        action.is_deleted
                          ? 'red'
                          : action.is_enabled
                            ? 'green'
                            : 'slate'
                      }
                    >
                      {action.is_deleted
                        ? 'Deleted'
                        : action.is_enabled
                          ? 'Active'
                          : 'Disabled'}
                    </StatusBadge>
                  </td>
                  <td className="table-actions">
                    <button
                      className="icon-button"
                      disabled={!canProcess || action.is_deleted}
                      onClick={() => void onProcess(action.id)}
                      type="button"
                    >
                      Run
                    </button>
                    <button
                      className="icon-button"
                      disabled={!canUpdate || action.is_deleted}
                      onClick={() => {
                        setEditingId(action.id)
                        setForm({
                          name: action.name,
                          action_type: action.action_type as ActionType,
                          run_on_time: toInputTime(action.run_on_time),
                          run_on_day: action.run_on_day as RunDay,
                          message: action.message,
                          to: action.to,
                          is_enabled: action.is_enabled,
                          condition: action.condition,
                        })
                        setConditionGroups(
                          groupsFromCondition(action.condition).length > 0
                            ? groupsFromCondition(action.condition)
                            : [[]],
                        )
                      }}
                      type="button"
                    >
                      Edit
                    </button>
                    <button
                      className="danger-button"
                      disabled={!canDelete || action.is_deleted}
                      onClick={() => void onDelete(action.id)}
                      type="button"
                    >
                      Delete
                    </button>
                    {action.is_deleted ? (
                      <button
                        className="icon-button"
                        disabled={!canUpdate}
                        onClick={() => void onRestore(action.id)}
                        type="button"
                      >
                        Restore
                      </button>
                    ) : null}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {filteredActions.length === 0 ? (
          <EmptyState message="No actions returned from ActionMS yet." />
        ) : null}
        <p className="panel-note">
          Last run values are updated by ActionMS after scheduled or manual
          processing. Latest shown: {formatDateTime(actions[0]?.last_run)}
        </p>
      </section>
    </>
  )
}
