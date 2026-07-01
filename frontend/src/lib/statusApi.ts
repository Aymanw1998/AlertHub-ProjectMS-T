export type ServiceStatusName =
  | 'security'
  | 'user'
  | 'loader'
  | 'metric'
  | 'action'
  | 'processor'
  | 'email'
  | 'sms'
  | 'logger'
  | 'evaluation'

export type ServiceStatus = {
  name: ServiceStatusName
  label: string
  port: number
  role: string
  requiredFor: string
  state: 'checking' | 'online' | 'offline'
  latencyMs?: number
  detail?: string
}

export const services: Omit<ServiceStatus, 'state' | 'latencyMs' | 'detail'>[] = [
  {
    name: 'security',
    label: 'SecurityMS',
    port: 1008,
    role: 'Authentication',
    requiredFor: 'Login and signup',
  },
  {
    name: 'user',
    label: 'UserMS',
    port: 1009,
    role: 'Users and roles',
    requiredFor: 'Users page and auth lookup',
  },
  {
    name: 'loader',
    label: 'LoaderMS',
    port: 1010,
    role: 'Platform data',
    requiredFor: 'Processor condition checks',
  },
  {
    name: 'metric',
    label: 'MetricMS',
    port: 1011,
    role: 'Metrics',
    requiredFor: 'Metrics and action conditions',
  },
  {
    name: 'action',
    label: 'ActionMS',
    port: 1012,
    role: 'Actions',
    requiredFor: 'Actions page and trigger',
  },
  {
    name: 'processor',
    label: 'ProcessorMS',
    port: 1013,
    role: 'Condition evaluation',
    requiredFor: 'Notification pipeline',
  },
  {
    name: 'email',
    label: 'EmailMS',
    port: 1014,
    role: 'Email delivery',
    requiredFor: 'Email notifications',
  },
  {
    name: 'sms',
    label: 'SmsMS',
    port: 1015,
    role: 'SMS delivery',
    requiredFor: 'SMS notifications',
  },
  {
    name: 'logger',
    label: 'LoggerMS',
    port: 1016,
    role: 'Logs',
    requiredFor: 'Service logging',
  },
  {
    name: 'evaluation',
    label: 'EvaluationMS',
    port: 1017,
    role: 'Reports',
    requiredFor: 'Evaluation summaries',
  },
]

export async function checkService(
  service: Omit<ServiceStatus, 'state' | 'latencyMs' | 'detail'>,
  timeoutMs = 1800,
): Promise<ServiceStatus> {
  const controller = new AbortController()
  const timeout = window.setTimeout(() => controller.abort(), timeoutMs)
  const started = performance.now()

  try {
    const response = await fetch(`/status/${service.name}`, {
      cache: 'no-store',
      signal: controller.signal,
    })

    const latencyMs = Math.round(performance.now() - started)

    if (!response.ok) {
      return {
        ...service,
        state: 'offline',
        latencyMs,
        detail: `HTTP ${response.status}`,
      }
    }

    return {
      ...service,
      state: 'online',
      latencyMs,
      detail: 'OpenAPI docs responded',
    }
  } catch (err) {
    return {
      ...service,
      state: 'offline',
      detail: err instanceof DOMException && err.name === 'AbortError' ? 'Timed out' : 'No response',
    }
  } finally {
    window.clearTimeout(timeout)
  }
}
