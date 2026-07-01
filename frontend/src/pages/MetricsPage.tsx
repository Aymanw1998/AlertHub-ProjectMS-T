import { type FormEvent, useEffect, useMemo, useState } from 'react'
import { AppLayout } from '../components/AppLayout'
import { hasPermission } from '../lib/permissions'
import { getUser } from '../lib/tokenStorage'
import {
  createMetric,
  deleteMetric,
  getMetrics,
  metricLabels,
  updateMetric,
  type Metric,
  type MetricLabel,
  type MetricRequest,
} from '../lib/metricsApi'

function createEmptyMetricForm(userId: number): MetricRequest {
  return {
    user_id: userId,
    name: '',
    label: 'bug',
    threshold: 80,
    time_frame_hours: 6,
  }
}

export function MetricsPage() {
  const user = getUser()
  const currentUserId = user?.userId ?? 1
  const [metrics, setMetrics] = useState<Metric[]>([])
  const [form, setForm] = useState<MetricRequest>(() => createEmptyMetricForm(currentUserId))
  const [editingMetricId, setEditingMetricId] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const canCreateMetric = hasPermission('createMetric')
  const canUpdateMetric = hasPermission('updateMetric')
  const canDeleteMetric = hasPermission('deleteMetric')
  const canUseMetricForm = editingMetricId ? canUpdateMetric : canCreateMetric

  const averageThreshold = useMemo(() => {
    if (metrics.length === 0) {
      return 0
    }

    const total = metrics.reduce((sum, metric) => sum + metric.threshold, 0)
    return Math.round(total / metrics.length)
  }, [metrics])

  async function loadData() {
    try {
      setLoading(true)
      setError('')
      setMetrics(await getMetrics())
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not load metrics')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  function updateForm(field: keyof MetricRequest, value: string) {
    setForm((current) => ({
      ...current,
      [field]:
        field === 'user_id' || field === 'threshold' || field === 'time_frame_hours'
          ? Number(value)
          : value,
    }))
  }

  function resetForm() {
    setEditingMetricId(null)
    setForm(createEmptyMetricForm(currentUserId))
  }

  function startEditMetric(metric: Metric) {
    if (!canUpdateMetric) {
      return
    }

    setEditingMetricId(metric.id)
    setForm({
      user_id: metric.user_id,
      name: metric.name,
      label: metric.label,
      threshold: metric.threshold,
      time_frame_hours: metric.time_frame_hours,
    })
    setError('')
    setNotice('')
  }

  async function handleSaveMetric(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!canUseMetricForm) {
      setError('You do not have permission to save metrics')
      return
    }

    try {
      setSaving(true)
      setError('')
      setNotice('')
      const payload = {
        ...form,
        user_id: editingMetricId ? form.user_id : currentUserId,
      }

      if (editingMetricId) {
        await updateMetric(editingMetricId, payload)
        setNotice(`Metric ${form.name} updated`)
      } else {
        await createMetric(payload)
        setNotice(`Metric ${form.name} created`)
      }

      resetForm()
      await loadData()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not save metric')
    } finally {
      setSaving(false)
    }
  }

  async function handleDeleteMetric(metric: Metric) {
    if (!canDeleteMetric) {
      return
    }

    const confirmed = window.confirm(`Delete metric "${metric.name}"?`)
    if (!confirmed) {
      return
    }

    try {
      setError('')
      setNotice('')
      await deleteMetric(metric.id)
      setNotice(`Metric ${metric.name} deleted`)
      await loadData()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not delete metric')
    }
  }

  return (
    <AppLayout eyebrow="Monitoring" title="Metrics">
      <div className="summary-grid">
        <article>
          <span>Metrics</span>
          <strong>{metrics.length}</strong>
        </article>
        <article>
          <span>Average threshold</span>
          <strong>{averageThreshold}</strong>
        </article>
        <article>
          <span>Service</span>
          <strong>MetricMS :1011</strong>
        </article>
      </div>

        {error && <p className="error">{error}</p>}
        {notice && <p className="success">{notice}</p>}
        {!canCreateMetric && !canUpdateMetric && (
          <p className="notice">You can view metrics, but your user does not have metric write permissions.</p>
        )}

      {(canCreateMetric || canUpdateMetric) && (
      <section className="section">
          <div className="section-header">
          <h3>{editingMetricId ? 'Edit metric' : 'Create metric'}</h3>
          <div className="section-actions">
            {editingMetricId && (
              <button className="secondary-small-button" type="button" onClick={resetForm}>
                Cancel edit
              </button>
            )}
            <span className="status-pill">1 to 24 hours</span>
          </div>
        </div>

        <form className="management-form" onSubmit={handleSaveMetric}>
          <label>
            Creator user ID
            <input
              disabled
              type="number"
              value={currentUserId}
            />
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
            Label
            <select
              value={form.label}
              onChange={(event) => updateForm('label', event.target.value as MetricLabel)}
            >
              {metricLabels.map((label) => (
                <option key={label} value={label}>
                  {label}
                </option>
              ))}
            </select>
          </label>
          <label>
            Threshold
            <input
              required
              min="0"
              type="number"
              value={form.threshold}
              onChange={(event) => updateForm('threshold', event.target.value)}
            />
          </label>
          <label>
            Time frame hours
            <input
              required
              max="24"
              min="1"
              type="number"
              value={form.time_frame_hours}
              onChange={(event) => updateForm('time_frame_hours', event.target.value)}
            />
          </label>

          <button disabled={saving || !canUseMetricForm}>
            {saving ? 'Saving...' : editingMetricId ? 'Save metric' : 'Create metric'}
          </button>
        </form>
      </section>
      )}

      <section className="section">
        <div className="section-header">
          <h3>All metrics</h3>
          {loading && <span className="status-pill">Loading</span>}
        </div>

        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Label</th>
                <th>User ID</th>
                <th>Threshold</th>
                <th>Time frame</th>
                {(canUpdateMetric || canDeleteMetric) && <th>Action</th>}
              </tr>
            </thead>
            <tbody>
              {metrics.map((metric) => (
                <tr key={metric.id}>
                  <td>{metric.id}</td>
                  <td>{metric.name}</td>
                  <td>
                    <span className="label-pill">{metric.label}</span>
                  </td>
                  <td>{metric.user_id}</td>
                  <td>{metric.threshold}</td>
                  <td>{metric.time_frame_hours}h</td>
                  {(canUpdateMetric || canDeleteMetric) && (
                  <td>
                    <div className="row-actions">
                      {canUpdateMetric && <button onClick={() => startEditMetric(metric)}>Edit</button>}
                      {canDeleteMetric && (
                        <button className="danger-button" onClick={() => handleDeleteMetric(metric)}>
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
