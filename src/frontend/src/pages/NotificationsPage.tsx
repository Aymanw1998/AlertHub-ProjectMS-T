import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  listActions,
  listLogs,
  type ActionRecord,
  type LogEntry,
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

function notificationTone(message: string) {
  const normalized = message.toLowerCase()
  if (normalized.includes('fail') || normalized.includes('error')) {
    return 'red' as const
  }
  if (normalized.includes('sent') || normalized.includes('success')) {
    return 'green' as const
  }
  return 'orange' as const
}

export function NotificationsPage() {
  const { auth } = useAuth()
  const [actions, setActions] = useState<ActionRecord[]>([])
  const [logs, setLogs] = useState<LogEntry[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const loadNotifications = useCallback(async () => {
    if (!auth?.token) {
      return
    }

    setIsLoading(true)
    setErrorMessage(null)
    try {
      const [nextActions, nextLogs] = await Promise.all([
        listActions(auth.token),
        listLogs(auth.token),
      ])
      setActions(nextActions)
      setLogs(nextLogs)
    } catch (error) {
      setErrorMessage(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }, [auth])

  useEffect(() => {
    void loadNotifications()
  }, [loadNotifications])

  const notificationLogs = useMemo(() => {
    return logs.filter((log) => {
      const text = `${log.serviceName} ${log.message}`.toLowerCase()
      return text.includes('email') || text.includes('sms') || text.includes('notification')
    })
  }, [logs])

  const emailActions = actions.filter((action) => action.action_type === 'email')
  const smsActions = actions.filter((action) => action.action_type === 'sms')
  const failedLogs = notificationLogs.filter((log) =>
    `${log.logLevel} ${log.message}`.toLowerCase().includes('fail'),
  )

  return (
    <>
      <PageHeader
        title="Notifications Dashboard"
        description="Track email and SMS activity derived from ActionMS and LoggerMS."
        actions={
          <button
            className="secondary-button"
            onClick={loadNotifications}
            type="button"
          >
            Refresh
          </button>
        }
      />

      <InlineAlert message={errorMessage} tone="red" />

      <section className="stats-grid">
        <StatCard
          detail="email action definitions"
          label="Email Actions"
          value={isLoading ? '...' : emailActions.length}
        />
        <StatCard
          detail="sms action definitions"
          label="SMS Actions"
          tone="green"
          value={smsActions.length}
        />
        <StatCard
          detail="notification related logs"
          label="Delivery Events"
          tone="purple"
          value={notificationLogs.length}
        />
        <StatCard
          detail="failed delivery logs"
          label="Failures"
          tone={failedLogs.length > 0 ? 'red' : 'orange'}
          value={failedLogs.length}
        />
      </section>

      <section className="dashboard-grid">
        <article className="panel">
          <div className="panel-header">
            <h2>Notification Queue</h2>
            <StatusBadge tone="blue">ActionMS source</StatusBadge>
          </div>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Action</th>
                  <th>Type</th>
                  <th>Recipient</th>
                  <th>Message</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {actions.slice(0, 12).map((action) => (
                  <tr key={action.id}>
                    <td>{action.name}</td>
                    <td>{action.action_type}</td>
                    <td>{action.to}</td>
                    <td>{action.message}</td>
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
                            ? 'Ready'
                            : 'Disabled'}
                      </StatusBadge>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {actions.length === 0 ? (
            <EmptyState message="No actions are available as notification definitions." />
          ) : null}
        </article>

        <article className="panel">
          <div className="panel-header">
            <h2>Delivery Logs</h2>
            <StatusBadge tone="green">LoggerMS source</StatusBadge>
          </div>
          <div className="timeline">
            {notificationLogs.slice(0, 10).map((log) => (
              <div className="timeline-item" key={log.id}>
                <StatusBadge tone={notificationTone(log.message)}>
                  {log.logLevel}
                </StatusBadge>
                <div>
                  <strong>{log.serviceName}</strong>
                  <span>{formatDateTime(log.timestamp)}</span>
                  <p>{log.message}</p>
                </div>
              </div>
            ))}
          </div>
          {notificationLogs.length === 0 ? (
            <EmptyState message="No notification-related logs returned yet." />
          ) : null}
        </article>
      </section>
    </>
  )
}
