import { useState, type FormEvent } from 'react'
import {
  LABELS,
  getDeveloperWithMostLabel,
  getLabelAggregate,
  getTaskAmount,
  type DeveloperLabelCountResponse,
  type Label,
  type LabelAggregateResponse,
  type TaskAmountResponse,
} from '../api/alerthubApi'
import { useAuth } from '../auth/useAuth'
import {
  InlineAlert,
  PageHeader,
  StatCard,
  StatusBadge,
} from '../components/ui'
import { normalizeError } from '../lib/format'

export function EvaluationPage() {
  const { auth } = useAuth()
  const [label, setLabel] = useState<Label>('bug')
  const [developerId, setDeveloperId] = useState('101')
  const [since, setSince] = useState(7)
  const [mostLabel, setMostLabel] =
    useState<DeveloperLabelCountResponse | null>(null)
  const [aggregate, setAggregate] = useState<LabelAggregateResponse | null>(null)
  const [taskAmount, setTaskAmount] = useState<TaskAmountResponse | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  async function runAll(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!auth?.token) {
      return
    }

    setIsLoading(true)
    setErrorMessage(null)
    try {
      const [nextMostLabel, nextAggregate, nextTaskAmount] = await Promise.all([
        getDeveloperWithMostLabel(auth.token, label, since),
        getLabelAggregate(auth.token, developerId, since),
        getTaskAmount(auth.token, developerId, since),
      ])
      setMostLabel(nextMostLabel)
      setAggregate(nextAggregate)
      setTaskAmount(nextTaskAmount)
    } catch (error) {
      setErrorMessage(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const aggregateTotal = aggregate
    ? Object.values(aggregate.labelCounts).reduce((sum, value) => sum + value, 0)
    : 0

  return (
    <>
      <PageHeader
        title="Evaluation Dashboard"
        description="Run predefined EvaluationMS reports against LoaderMS task data."
      />

      <InlineAlert message={errorMessage} tone="red" />

      <section className="stats-grid">
        <StatCard
          detail={`label: ${label}`}
          label="Top Developer"
          value={mostLabel?.developerId ?? '-'}
        />
        <StatCard
          detail={`developer ${developerId}`}
          label="Task Amount"
          tone="green"
          value={taskAmount?.taskAmount ?? '-'}
        />
        <StatCard
          detail={`${since} days`}
          label="Aggregate Total"
          tone="purple"
          value={aggregateTotal}
        />
      </section>

      <section className="panel">
        <div className="panel-header">
          <h2>Run Evaluation Query</h2>
          <StatusBadge tone="blue">GET /api/evaluation</StatusBadge>
        </div>
        <form className="form-grid" onSubmit={runAll}>
          <label className="form-field">
            Label
            <select
              onChange={(event) => setLabel(event.target.value as Label)}
              value={label}
            >
              {LABELS.map((nextLabel) => (
                <option key={nextLabel} value={nextLabel}>
                  {nextLabel}
                </option>
              ))}
            </select>
          </label>
          <label className="form-field">
            Developer ID
            <input
              onChange={(event) => setDeveloperId(event.target.value)}
              required
              value={developerId}
            />
          </label>
          <label className="form-field">
            Since Days
            <input
              min={1}
              onChange={(event) => setSince(Number(event.target.value))}
              required
              type="number"
              value={since}
            />
          </label>
          <div className="form-actions">
            <button disabled={isLoading} type="submit">
              {isLoading ? 'Running...' : 'Run Query'}
            </button>
          </div>
        </form>
      </section>

      <section className="dashboard-grid">
        <article className="panel">
          <h2>Developer With Most Label Occurrences</h2>
          <dl className="details-list">
            <div>
              <dt>Developer</dt>
              <dd>{mostLabel?.developerId ?? '-'}</dd>
            </div>
            <div>
              <dt>Label</dt>
              <dd>{mostLabel?.label ?? label}</dd>
            </div>
            <div>
              <dt>Task Count</dt>
              <dd>{mostLabel?.taskCount ?? '-'}</dd>
            </div>
          </dl>
        </article>

        <article className="panel">
          <h2>Label Distribution</h2>
          <div className="bar-list">
            {Object.entries(aggregate?.labelCounts ?? {}).map(
              ([labelName, value]) => (
                <div className="bar-row" key={labelName}>
                  <span>{labelName}</span>
                  <div>
                    <i style={{ width: `${Math.max(8, value * 8)}px` }} />
                  </div>
                  <strong>{value}</strong>
                </div>
              ),
            )}
          </div>
        </article>

        <article className="panel">
          <h2>Total Tasks for Developer</h2>
          <div className="big-number">{taskAmount?.taskAmount ?? '-'}</div>
          <p className="panel-note">
            Developer {developerId}, last {since} days.
          </p>
        </article>
      </section>
    </>
  )
}
