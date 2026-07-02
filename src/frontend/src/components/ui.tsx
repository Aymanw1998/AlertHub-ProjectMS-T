import type { ReactNode } from 'react'

type Tone = 'blue' | 'green' | 'orange' | 'red' | 'purple' | 'slate'

export function PageHeader({
  title,
  description,
  actions,
}: {
  title: string
  description: string
  actions?: ReactNode
}) {
  return (
    <div className="page-header">
      <div>
        <h1>{title}</h1>
        <p>{description}</p>
      </div>
      {actions ? <div className="page-actions">{actions}</div> : null}
    </div>
  )
}

export function StatCard({
  label,
  value,
  detail,
  tone = 'blue',
}: {
  label: string
  value: string | number
  detail: string
  tone?: Tone
}) {
  return (
    <article className="stat-card">
      <div className={`stat-icon tone-${tone}`}>{label.slice(0, 2)}</div>
      <div>
        <p>{label}</p>
        <strong>{value}</strong>
        <span>{detail}</span>
      </div>
    </article>
  )
}

export function StatusBadge({
  children,
  tone = 'slate',
}: {
  children: ReactNode
  tone?: Tone
}) {
  return <span className={`status-badge tone-${tone}`}>{children}</span>
}

export function InlineAlert({
  message,
  tone = 'blue',
}: {
  message: string | null
  tone?: Tone
}) {
  if (!message) {
    return null
  }

  return <p className={`inline-alert tone-${tone}`}>{message}</p>
}

export function EmptyState({ message }: { message: string }) {
  return <div className="empty-state">{message}</div>
}
