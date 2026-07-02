import { useCallback, useEffect, useState } from 'react'
import {
  listActions,
  listLoaderData,
  listLogs,
  listMetrics,
  listUsers,
  type ActionRecord,
  type LoaderItem,
  type LogEntry,
  type Metric,
  type UserAccount,
} from '../api/alerthubApi'
import { useAuth } from '../auth/useAuth'
import {
  EmptyState,
  InlineAlert,
  PageHeader,
  StatCard,
  StatusBadge,
} from '../components/ui'
import { compactNumber, formatDateTime, normalizeError } from '../lib/format'

type DashboardData = {
  loader: LoaderItem[]
  metrics: Metric[]
  actions: ActionRecord[]
  logs: LogEntry[]
  users: UserAccount[]
}

const emptyData: DashboardData = {
  loader: [],
  metrics: [],
  actions: [],
  logs: [],
  users: [],
}

export function DashboardPage() {
  const { auth } = useAuth()
  const [data, setData] = useState<DashboardData>(emptyData)
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const loadDashboard = useCallback(async () => {
    if (!auth?.token) {
      return
    }

    setIsLoading(true)
    setErrorMessage(null)
    try {
      const [loader, metrics, actions, logs, users] = await Promise.all([
        listLoaderData(auth.token),
        listMetrics(auth.token),
        listActions(auth.token),
        listLogs(auth.token),
        auth.username === 'admin'
          ? listUsers(auth.token)
          : Promise.resolve<UserAccount[]>([]),
      ])
      setData({ loader, metrics, actions, logs, users })
    } catch (error) {
      setErrorMessage(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }, [auth])

  useEffect(() => {
    void loadDashboard()
  }, [loadDashboard])

  const activeActions = data.actions.filter(
    (action) => action.is_enabled && !action.is_deleted,
  )
  const errorLogs = data.logs.filter((log) =>
    log.logLevel.toLowerCase().includes('error'),
  )
  const projects = new Set(data.loader.map((item) => item.project)).size
  const latestTask = data.loader[0]

  return (
    <>
      <PageHeader
        title="Alert Hub Dashboard"
        description="Live operational overview from the Gateway and backend microservices."
        actions={
          <button className="secondary-button" onClick={loadDashboard} type="button">
            Refresh
          </button>
        }
      />

      <InlineAlert message={errorMessage} tone="red" />

      <section className="stats-grid">
        <StatCard
          detail="rows in LoaderMS"
          label="Loaded Tasks"
          tone="blue"
          value={isLoading ? '...' : compactNumber(data.loader.length)}
        />
        <StatCard
          detail="saved monitoring rules"
          label="Metrics"
          tone="green"
          value={isLoading ? '...' : data.metrics.length}
        />
        <StatCard
          detail={`${activeActions.length} active`}
          label="Actions"
          tone="purple"
          value={isLoading ? '...' : data.actions.length}
        />
        <StatCard
          detail={`${errorLogs.length} errors`}
          label="Logs"
          tone={errorLogs.length > 0 ? 'red' : 'orange'}
          value={isLoading ? '...' : data.logs.length}
        />
      </section>

      <section className="dashboard-grid">
        <article className="panel">
          <div className="panel-header">
            <h2>Microservice Coverage</h2>
            <StatusBadge tone="green">Gateway online</StatusBadge>
          </div>
          <div className="service-grid">
            {[
              ['LoaderMS', data.loader.length, 'Task ingestion'],
              ['MetricMS', data.metrics.length, 'Conditions'],
              ['ActionMS', data.actions.length, 'Responses'],
              ['LoggerMS', data.logs.length, 'Observability'],
              ['UserMS', data.users.length, 'Access control'],
              ['EvaluationMS', projects, 'Reports'],
            ].map(([name, count, detail]) => (
              <div className="service-tile" key={name}>
                <strong>{name}</strong>
                <span>{detail}</span>
                <b>{count}</b>
              </div>
            ))}
          </div>
        </article>

        <article className="panel">
          <div className="panel-header">
            <h2>Latest Loader Row</h2>
            <StatusBadge tone="blue">{projects} projects</StatusBadge>
          </div>
          {latestTask ? (
            <dl className="details-list">
              <div>
                <dt>Task</dt>
                <dd>{latestTask.task_number}</dd>
              </div>
              <div>
                <dt>Project</dt>
                <dd>{latestTask.project}</dd>
              </div>
              <div>
                <dt>Label</dt>
                <dd>{latestTask.label}</dd>
              </div>
              <div>
                <dt>Developer</dt>
                <dd>{latestTask.developer_id}</dd>
              </div>
              <div>
                <dt>Timestamp</dt>
                <dd>{formatDateTime(latestTask.timestamp)}</dd>
              </div>
            </dl>
          ) : (
            <EmptyState message="No loader rows returned yet." />
          )}
        </article>
      </section>
    </>
  )
}
