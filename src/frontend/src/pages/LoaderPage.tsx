import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  listLoaderData,
  triggerLoaderScan,
  type LoaderItem,
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

export function LoaderPage() {
  const { auth, hasRole } = useAuth()
  const [items, setItems] = useState<LoaderItem[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [scanMessage, setScanMessage] = useState<string | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [query, setQuery] = useState('')

  const loadItems = useCallback(async () => {
    if (!auth?.token) {
      return
    }

    setIsLoading(true)
    setErrorMessage(null)
    try {
      setItems(await listLoaderData(auth.token))
    } catch (error) {
      setErrorMessage(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }, [auth])

  useEffect(() => {
    void loadItems()
  }, [loadItems])

  async function onScan() {
    if (!auth?.token) {
      return
    }

    setScanMessage(null)
    setErrorMessage(null)
    try {
      const message = await triggerLoaderScan(auth.token)
      setScanMessage(message || 'Scan completed.')
      await loadItems()
    } catch (error) {
      setErrorMessage(normalizeError(error))
    }
  }

  const filteredItems = useMemo(() => {
    const term = query.trim().toLowerCase()
    if (!term) {
      return items
    }

    return items.filter((item) =>
      [
        item.project,
        item.label,
        item.developer_id,
        item.task_number,
        item.environment,
        item.user_story,
      ]
        .join(' ')
        .toLowerCase()
        .includes(term),
    )
  }, [items, query])

  const uniqueDevelopers = new Set(items.map((item) => item.developer_id)).size
  const bugCount = items.filter((item) => item.label === 'bug').length
  const productionCount = items.filter(
    (item) => item.environment === 'production',
  ).length

  return (
    <>
      <PageHeader
        title="Loader Dashboard"
        description="Monitor task data loaded from provider integrations into LoaderMS."
        actions={
          <>
            <button className="secondary-button" onClick={loadItems} type="button">
              Refresh
            </button>
            <button
              disabled={!hasRole('triggerScan')}
              onClick={onScan}
              type="button"
            >
              Manual Scan
            </button>
          </>
        }
      />

      <InlineAlert message={scanMessage} tone="green" />
      <InlineAlert message={errorMessage} tone="red" />

      <section className="stats-grid">
        <StatCard
          detail="rows from LoaderMS"
          label="Rows Loaded"
          value={isLoading ? '...' : compactNumber(items.length)}
        />
        <StatCard
          detail="unique developer IDs"
          label="Developers"
          tone="green"
          value={uniqueDevelopers}
        />
        <StatCard
          detail="bug labeled tasks"
          label="Bug Tasks"
          tone="orange"
          value={bugCount}
        />
        <StatCard
          detail="production environment"
          label="Production"
          tone="purple"
          value={productionCount}
        />
      </section>

      <section className="panel">
        <div className="panel-header">
          <h2>Loader Data</h2>
          <input
            aria-label="Search loader rows"
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Search rows..."
            value={query}
          />
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Time</th>
                <th>Project</th>
                <th>Task</th>
                <th>Label</th>
                <th>Developer</th>
                <th>Environment</th>
                <th>Points</th>
              </tr>
            </thead>
            <tbody>
              {filteredItems.slice(0, 30).map((item) => (
                <tr key={item.id}>
                  <td>{formatDateTime(item.timestamp)}</td>
                  <td>{item.project}</td>
                  <td>{item.task_number}</td>
                  <td>
                    <StatusBadge tone={item.label === 'bug' ? 'red' : 'blue'}>
                      {item.label}
                    </StatusBadge>
                  </td>
                  <td>{item.developer_id}</td>
                  <td>{item.environment}</td>
                  <td>{item.task_point}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {filteredItems.length === 0 ? (
          <EmptyState message="No loader rows match the current filter." />
        ) : null}
      </section>
    </>
  )
}
