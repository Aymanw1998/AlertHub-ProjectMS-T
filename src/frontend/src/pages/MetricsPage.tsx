import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import {
  LABELS,
  createMetric,
  deleteMetric,
  listMetrics,
  updateMetric,
  type Label,
  type Metric,
  type MetricPayload,
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

type MetricForm = {
  name: string
  label: Label
  threshold: number
  time_frame_hours: number
}

const defaultForm: MetricForm = {
  name: 'bug_10_12',
  label: 'bug',
  threshold: 10,
  time_frame_hours: 12,
}

export function MetricsPage() {
  const { auth, hasRole } = useAuth()
  const [metrics, setMetrics] = useState<Metric[]>([])
  const [form, setForm] = useState<MetricForm>(defaultForm)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [message, setMessage] = useState<string | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [query, setQuery] = useState('')

  const loadMetrics = useCallback(async () => {
    if (!auth?.token) {
      return
    }

    setIsLoading(true)
    setErrorMessage(null)
    try {
      setMetrics(await listMetrics(auth.token))
    } catch (error) {
      setErrorMessage(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }, [auth])

  useEffect(() => {
    void loadMetrics()
  }, [loadMetrics])

  function toPayload(): MetricPayload {
    return {
      user_id: auth?.userId ?? 0,
      name: form.name.trim(),
      label: form.label,
      threshold: Number(form.threshold),
      time_frame_hours: Number(form.time_frame_hours),
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
        await updateMetric(auth.token, editingId, toPayload())
        setMessage('Metric updated successfully.')
      } else {
        await createMetric(auth.token, toPayload())
        setMessage('Metric created successfully.')
      }
      setEditingId(null)
      setForm(defaultForm)
      await loadMetrics()
    } catch (error) {
      setErrorMessage(normalizeError(error))
    }
  }

  async function onDelete(metricId: number) {
    if (!auth?.token) {
      return
    }

    setErrorMessage(null)
    try {
      await deleteMetric(auth.token, metricId)
      setMessage('Metric deleted.')
      await loadMetrics()
    } catch (error) {
      setErrorMessage(normalizeError(error))
    }
  }

  const filteredMetrics = useMemo(() => {
    const term = query.trim().toLowerCase()
    return term
      ? metrics.filter((metric) =>
          [metric.name, metric.label, metric.user_id]
            .join(' ')
            .toLowerCase()
            .includes(term),
        )
      : metrics
  }, [metrics, query])

  const canCreate = hasRole('createMetric')
  const canUpdate = hasRole('updateMetric')
  const canDelete = hasRole('deleteMetric')

  return (
    <>
      <PageHeader
        title="Metric Dashboard"
        description="Create and manage MetricMS rules used by the processor."
        actions={
          <button className="secondary-button" onClick={loadMetrics} type="button">
            Refresh
          </button>
        }
      />

      <InlineAlert message={message} tone="green" />
      <InlineAlert message={errorMessage} tone="red" />

      <section className="stats-grid">
        <StatCard
          detail="all saved metrics"
          label="Total Metrics"
          value={isLoading ? '...' : metrics.length}
        />
        <StatCard
          detail="usable by ProcessorMS"
          label="Active Metrics"
          tone="green"
          value={metrics.length}
        />
        <StatCard
          detail="threshold sum"
          label="Total Threshold"
          tone="orange"
          value={metrics.reduce((sum, metric) => sum + metric.threshold, 0)}
        />
      </section>

      <section className="panel">
        <div className="panel-header">
          <h2>{editingId ? 'Update Metric' : 'Create New Metric'}</h2>
          <StatusBadge tone="blue">POST /api/metric/create</StatusBadge>
        </div>
        <form className="form-grid" onSubmit={onSubmit}>
          <label className="form-field">
            Metric Name
            <input
              onChange={(event) =>
                setForm((current) => ({ ...current, name: event.target.value }))
              }
              required
              value={form.name}
            />
          </label>
          <label className="form-field">
            Label
            <select
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  label: event.target.value as Label,
                }))
              }
              value={form.label}
            >
              {LABELS.map((label) => (
                <option key={label} value={label}>
                  {label}
                </option>
              ))}
            </select>
          </label>
          <label className="form-field">
            Threshold
            <input
              min={0}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  threshold: Number(event.target.value),
                }))
              }
              required
              type="number"
              value={form.threshold}
            />
          </label>
          <label className="form-field">
            Time Frame Hours
            <input
              max={24}
              min={1}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  time_frame_hours: Number(event.target.value),
                }))
              }
              required
              type="number"
              value={form.time_frame_hours}
            />
          </label>
          <div className="form-actions">
            <button disabled={editingId ? !canUpdate : !canCreate} type="submit">
              {editingId ? 'Save Metric' : 'Create Metric'}
            </button>
            {editingId ? (
              <button
                className="secondary-button"
                onClick={() => {
                  setEditingId(null)
                  setForm(defaultForm)
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
          <h2>Saved Metrics</h2>
          <input
            aria-label="Search metrics"
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Search metrics..."
            value={query}
          />
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Label</th>
                <th>Threshold</th>
                <th>Time Frame</th>
                <th>Owner</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredMetrics.map((metric) => (
                <tr key={metric.id}>
                  <td>{metric.id}</td>
                  <td>{metric.name}</td>
                  <td>
                    <StatusBadge tone={metric.label === 'bug' ? 'red' : 'blue'}>
                      {metric.label}
                    </StatusBadge>
                  </td>
                  <td>{metric.threshold}</td>
                  <td>{metric.time_frame_hours}h</td>
                  <td>{metric.user_id}</td>
                  <td className="table-actions">
                    <button
                      className="icon-button"
                      disabled={!canUpdate}
                      onClick={() => {
                        setEditingId(metric.id)
                        setForm({
                          name: metric.name,
                          label: metric.label as Label,
                          threshold: metric.threshold,
                          time_frame_hours: metric.time_frame_hours,
                        })
                      }}
                      type="button"
                    >
                      Edit
                    </button>
                    <button
                      className="danger-button"
                      disabled={!canDelete}
                      onClick={() => void onDelete(metric.id)}
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
        {filteredMetrics.length === 0 ? (
          <EmptyState message="No metrics returned from MetricMS yet." />
        ) : null}
      </section>
    </>
  )
}
