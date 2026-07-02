import { useCallback, useEffect, useMemo, useState } from 'react'
import { listLogs, type LogEntry } from '../api/alerthubApi'
import { useAuth } from '../auth/useAuth'
import {
  EmptyState,
  InlineAlert,
  PageHeader,
  StatCard,
  StatusBadge,
} from '../components/ui'
import { formatDateTime, normalizeError } from '../lib/format'

function toneForLog(level: string) {
  const normalized = level.toLowerCase()
  if (normalized.includes('error')) {
    return 'red' as const
  }
  if (normalized.includes('warn')) {
    return 'orange' as const
  }
  return 'blue' as const
}

export function LogsPage() {
  const { auth } = useAuth()
  const [logs, setLogs] = useState<LogEntry[]>([])
  const [query, setQuery] = useState('')
  const [levelFilter, setLevelFilter] = useState('all')
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const loadLogs = useCallback(async () => {
    if (!auth?.token) {
      return
    }

    setIsLoading(true)
    setErrorMessage(null)
    try {
      setLogs(await listLogs(auth.token))
    } catch (error) {
      setErrorMessage(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }, [auth])

  useEffect(() => {
    void loadLogs()
  }, [loadLogs])

  const filteredLogs = useMemo(() => {
    const term = query.trim().toLowerCase()
    return logs.filter((log) => {
      const matchesLevel =
        levelFilter === 'all' ||
        log.logLevel.toLowerCase() === levelFilter.toLowerCase()
      const matchesSearch =
        !term ||
        [log.serviceName, log.logLevel, log.message]
          .join(' ')
          .toLowerCase()
          .includes(term)
      return matchesLevel && matchesSearch
    })
  }, [logs, query, levelFilter])

  const levels = Array.from(new Set(logs.map((log) => log.logLevel))).filter(
    Boolean,
  )
  const errors = logs.filter((log) =>
    log.logLevel.toLowerCase().includes('error'),
  )
  const warnings = logs.filter((log) =>
    log.logLevel.toLowerCase().includes('warn'),
  )
  const services = new Set(logs.map((log) => log.serviceName)).size

  return (
    <>
      <PageHeader
        title="Logger Dashboard"
        description="Monitor LoggerMS records from MongoDB through GatewayMS."
        actions={
          <button className="secondary-button" onClick={loadLogs} type="button">
            Refresh
          </button>
        }
      />

      <InlineAlert message={errorMessage} tone="red" />

      <section className="stats-grid">
        <StatCard
          detail="records in LoggerMS"
          label="Total Logs"
          value={isLoading ? '...' : logs.length}
        />
        <StatCard detail="error level logs" label="Errors" tone="red" value={errors.length} />
        <StatCard
          detail="warning level logs"
          label="Warnings"
          tone="orange"
          value={warnings.length}
        />
        <StatCard detail="services observed" label="Services" tone="green" value={services} />
      </section>

      <section className="panel">
        <div className="panel-header">
          <h2>System Logs History</h2>
          <div className="filter-row">
            <select
              aria-label="Filter log level"
              onChange={(event) => setLevelFilter(event.target.value)}
              value={levelFilter}
            >
              <option value="all">All Levels</option>
              {levels.map((level) => (
                <option key={level} value={level}>
                  {level}
                </option>
              ))}
            </select>
            <input
              aria-label="Search logs"
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Search logs..."
              value={query}
            />
          </div>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>Service</th>
                <th>Level</th>
                <th>Message</th>
              </tr>
            </thead>
            <tbody>
              {filteredLogs.slice(0, 60).map((log) => (
                <tr key={log.id}>
                  <td>{formatDateTime(log.timestamp)}</td>
                  <td>{log.serviceName}</td>
                  <td>
                    <StatusBadge tone={toneForLog(log.logLevel)}>
                      {log.logLevel}
                    </StatusBadge>
                  </td>
                  <td>{log.message}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {filteredLogs.length === 0 ? (
          <EmptyState message="No logs returned from LoggerMS yet." />
        ) : null}
      </section>
    </>
  )
}
