const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export const actionTypes = ['email', 'sms'] as const
export const runOnDays = [
  'Sunday',
  'Monday',
  'Tuesday',
  'Wednesday',
  'Thursday',
  'Friday',
  'Saturday',
  'All',
] as const

export type ActionType = (typeof actionTypes)[number]
export type RunOnDay = (typeof runOnDays)[number]

export type AlertAction = {
  id: number
  owner_id: string
  name: string
  create_date: string
  action_type: ActionType
  run_on_time: string
  run_on_day: RunOnDay
  message: string
  to: string
  is_enabled: boolean
  is_deleted: boolean
  last_update: string
  last_run: string | null
  condition: string
}

export type ActionRequest = Pick<
  AlertAction,
  'owner_id' | 'name' | 'action_type' | 'run_on_time' | 'run_on_day' | 'message' | 'to' | 'condition'
>

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

export function getActions() {
  return request<AlertAction[]>('/api/action/get-all')
}

export function createAction(data: ActionRequest) {
  return request<AlertAction>('/api/action/create', jsonOptions('POST', data))
}

export function updateAction(id: number, data: ActionRequest & { is_enabled?: boolean }) {
  return request<AlertAction>(`/api/action/update/${id}`, jsonOptions('PUT', data))
}

export function deleteAction(id: number) {
  return request<void>(`/api/action/delete/${id}`, { method: 'DELETE' })
}

export function triggerAction(id: number) {
  return request<AlertAction>(`/api/action/process/${id}`, { method: 'POST' })
}
