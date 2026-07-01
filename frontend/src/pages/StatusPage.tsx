import { useEffect, useMemo, useState } from 'react'
import { AppLayout } from '../components/AppLayout'
import { checkService, services, type ServiceStatus } from '../lib/statusApi'

function createInitialStatuses(): ServiceStatus[] {
  return services.map((service) => ({
    ...service,
    state: 'checking',
  }))
}

export function StatusPage() {
  const [statuses, setStatuses] = useState<ServiceStatus[]>(createInitialStatuses)
  const [checking, setChecking] = useState(false)

  const onlineCount = useMemo(
    () => statuses.filter((service) => service.state === 'online').length,
    [statuses],
  )
  const offlineCount = useMemo(
    () => statuses.filter((service) => service.state === 'offline').length,
    [statuses],
  )

  async function refreshStatuses() {
    setChecking(true)
    setStatuses(createInitialStatuses())

    const results = await Promise.all(services.map((service) => checkService(service)))
    setStatuses(results)
    setChecking(false)
  }

  useEffect(() => {
    refreshStatuses()
  }, [])

  return (
    <AppLayout eyebrow="System" title="Pipeline Status">
      <div className="summary-grid">
        <article>
          <span>Online</span>
          <strong>{onlineCount}</strong>
        </article>
        <article>
          <span>Offline</span>
          <strong>{offlineCount}</strong>
        </article>
        <article>
          <span>Total services</span>
          <strong>{statuses.length}</strong>
        </article>
      </div>

      <section className="section">
        <div className="section-header">
          <h3>Microservices</h3>
          <button className="secondary-button" disabled={checking} onClick={refreshStatuses}>
            {checking ? 'Checking...' : 'Refresh'}
          </button>
        </div>

        <div className="status-grid">
          {statuses.map((service) => (
            <article key={service.name} className={`status-card ${service.state}`}>
              <div>
                <span className={`state-dot ${service.state}`} />
                <strong>{service.label}</strong>
              </div>
              <p>{service.role}</p>
              <dl>
                <div>
                  <dt>Port</dt>
                  <dd>{service.port}</dd>
                </div>
                <div>
                  <dt>Needed for</dt>
                  <dd>{service.requiredFor}</dd>
                </div>
                <div>
                  <dt>Status</dt>
                  <dd>{service.state}</dd>
                </div>
                <div>
                  <dt>Detail</dt>
                  <dd>
                    {service.latencyMs ? `${service.detail} in ${service.latencyMs}ms` : service.detail ?? '-'}
                  </dd>
                </div>
              </dl>
            </article>
          ))}
        </div>
      </section>
    </AppLayout>
  )
}
