const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export const metricLabels = [
  'bug',
  'documentation',
  'enhancement',
  'help_wanted',
  'duplicate',
  'invalid',
  'wontfix',
  'good_first_issue',
  'question',
] as const

export type MetricLabel = (typeof metricLabels)[number]

export type Metric = {
  id: number
  user_id: number
  name: string
  label: MetricLabel
  threshold: number
  time_frame_hours: number
}

export type MetricRequest = Omit<Metric, 'id'>

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, options)

  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || `Request failed with ${response.status}`)
  }

  if (response.status === 204) {
    return undefined as T
  }

  const text = await response.text()
  if (!text) {
    return undefined as T
  }

  try {
    return JSON.parse(text) as T
  } catch {
    return text as T
  }
}

function jsonOptions(method: string, body: unknown): RequestInit {
  return {
    method,
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  }
}

export function getMetrics() {
  return request<Metric[]>('/api/metric/get-all')
}

export function createMetric(data: MetricRequest) {
  return request<Metric>('/api/metric/create', jsonOptions('POST', data))
}

export function updateMetric(id: number, data: MetricRequest) {
  return request<Metric>(`/api/metric/update/${id}`, jsonOptions('PUT', data))
}

export function deleteMetric(id: number) {
  return request<void>(`/api/metric/delete/${id}`, { method: 'DELETE' })
}
