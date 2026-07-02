import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  listActions,
  listMetrics,
  listProcessorLoaderData,
  processAction,
  type ActionRecord,
  type LoaderItem,
  type Metric,
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

export function ProcessorPage() {
  const { auth, hasRole } = useAuth()
  const [loaderData, setLoaderData] = useState<LoaderItem[]>([])
  const [actions, setActions] = useState<ActionRecord[]>([])
  const [metrics, setMetrics] = useState<Metric[]>([])
  const [selectedActionId, setSelectedActionId] = useState<number | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [message, setMessage] = useState<string | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const loadProcessor = useCallback(async () => {
    if (!auth?.token) {
      return
    }

    setIsLoading(true)
    setErrorMessage(null)
    try {
      const [nextLoaderData, nextActions, nextMetrics] = await Promise.all([
        listProcessorLoaderData(auth.token),
        listActions(auth.token),
        listMetrics(auth.token),
      ])
      setLoaderData(nextLoaderData)
      setActions(nextActions)
      setMetrics(nextMetrics)
    } catch (error) {
      setErrorMessage(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }, [auth])

  useEffect(() => {
    void loadProcessor()
  }, [loadProcessor])

  const activeActions = useMemo(
    () => actions.filter((action) => action.is_enabled && !action.is_deleted),
    [actions],
  )

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

  const labels = useMemo(() => {
    return loaderData.reduce<Record<string, number>>((accumulator, item) => {
      accumulator[item.label] = (accumulator[item.label] ?? 0) + 1
      return accumulator
    }, {})
  }, [loaderData])
  const topLabel = Object.entries(labels).sort((a, b) => b[1] - a[1])[0]
  const selectedAction =
    actions.find((action) => action.id === selectedActionId) ?? null
  const canProcess = hasRole('triggerProcess')

  async function onProcessSelectedAction() {
    if (!auth?.token || !selectedActionId) {
      return
    }

    setMessage(null)
    setErrorMessage(null)
    try {
      await processAction(auth.token, selectedActionId)
      setMessage(`Action ${selectedActionId} was sent to ProcessorMS.`)
      await loadProcessor()
    } catch (error) {
      setErrorMessage(normalizeError(error))
    }
  }

  return (
    <>
      <PageHeader
        title="Processor Dashboard"
        description="Inspect the data ProcessorMS can evaluate through GatewayMS."
        actions={
          <button className="secondary-button" onClick={loadProcessor} type="button">
            Refresh
          </button>
        }
      />

      <InlineAlert message={message} tone="green" />
      <InlineAlert message={errorMessage} tone="red" />

      <section className="stats-grid">
        <StatCard
          detail="actions ready to evaluate"
          label="Active Actions"
          value={isLoading ? '...' : activeActions.length}
        />
        <StatCard
          detail="conditions available"
          label="Metrics"
          tone="green"
          value={metrics.length}
        />
        <StatCard
          detail="rows pulled from LoaderMS"
          label="Loader Rows"
          tone="purple"
          value={loaderData.length}
        />
        <StatCard
          detail={topLabel ? `${topLabel[1]} rows` : 'no labels'}
          label="Top Label"
          tone="orange"
          value={topLabel?.[0] ?? '-'}
        />
      </section>

      <section className="panel selected-action-panel">
        <div className="panel-header">
          <h2>Run Action Through Processor</h2>
          <StatusBadge tone="blue">ActionMS manual trigger</StatusBadge>
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
            disabled={!selectedAction || !canProcess}
            onClick={() => void onProcessSelectedAction()}
            type="button"
          >
            Run Selected
          </button>
        </div>
        <p className="panel-note">
          The button calls ActionMS `process/{'{id}'}`. ActionMS publishes the
          job to Kafka, then ProcessorMS consumes and evaluates it.
        </p>
      </section>

      <section className="dashboard-grid">
        <article className="panel">
          <div className="panel-header">
            <h2>Processor Flow</h2>
            <StatusBadge tone="green">Live via /api/processor</StatusBadge>
          </div>
          <div className="flow-list">
            {[
              'Read scheduled action',
              'Load metric condition',
              'Query LoaderMS task rows',
              'Evaluate TRUE/FALSE',
              'Push email/SMS notification',
              'Write operational log',
            ].map((step, index) => (
              <div className="flow-step" key={step}>
                <span>{index + 1}</span>
                <strong>{step}</strong>
              </div>
            ))}
          </div>
        </article>

        <article className="panel">
          <div className="panel-header">
            <h2>Loader Data Used by Processor</h2>
            <StatusBadge tone="blue">{loaderData.length} rows</StatusBadge>
          </div>
          <p className="panel-note">
            Yes, these rows come from LoaderMS. ProcessorMS uses them as the
            input data when evaluating metric conditions.
          </p>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Time</th>
                  <th>Task</th>
                  <th>Label</th>
                  <th>Developer</th>
                  <th>Project</th>
                </tr>
              </thead>
              <tbody>
                {loaderData.slice(0, 10).map((item) => (
                  <tr key={item.id}>
                    <td>{formatDateTime(item.timestamp)}</td>
                    <td>{item.task_number}</td>
                    <td>{item.label}</td>
                    <td>{item.developer_id}</td>
                    <td>{item.project}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {loaderData.length === 0 ? (
            <EmptyState message="ProcessorMS did not return loader rows." />
          ) : null}
        </article>
      </section>
    </>
  )
}
