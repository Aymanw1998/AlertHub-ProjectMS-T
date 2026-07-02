import { apiRequest } from './client'

export const LABELS = [
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

export const ACTION_TYPES = ['email', 'sms'] as const

export const RUN_DAYS = [
  'Sunday',
  'Monday',
  'Tuesday',
  'Wednesday',
  'Thursday',
  'Friday',
  'Saturday',
  'All',
] as const

export type Label = (typeof LABELS)[number]
export type ActionType = (typeof ACTION_TYPES)[number]
export type RunDay = (typeof RUN_DAYS)[number]

export type LoaderItem = {
  id: number
  timestamp: string
  owner_id: number
  project: string
  tag: string
  label: Label | string
  developer_id: string
  task_number: string
  environment: string
  user_story: string
  task_point: number
  sprint: string
}

export type Metric = {
  id: number
  user_id: number
  name: string
  label: Label | string
  threshold: number
  time_frame_hours: number
}

export type MetricPayload = Omit<Metric, 'id'>

export type ActionRecord = {
  id: number
  owner_id: string
  name: string
  create_date?: string
  action_type: ActionType | string
  run_on_time: string
  run_on_day: RunDay | string
  message: string
  to: string
  is_enabled: boolean
  is_deleted: boolean
  last_update?: string
  last_run?: string
  condition: string
}

export type ActionPayload = Omit<
  ActionRecord,
  'id' | 'create_date' | 'last_update' | 'last_run'
>

export type LogEntry = {
  id: string
  timestamp: string
  serviceName: string
  logLevel: string
  message: string
}

export type RoleRecord = {
  id: number
  role: string
}

export type UserAccount = {
  id: number
  username: string
  email: string
  phone: string
  roles: RoleRecord[]
}

export type UserPayload = {
  username: string
  email: string
  phone: string
  password: string
  roles: string[]
}

export type DeveloperLabelCountResponse = {
  developerId: string
  label: Label | string
  sinceDays: number
  taskCount: number
}

export type LabelAggregateResponse = {
  developerId: string
  sinceDays: number
  labelCounts: Record<string, number>
}

export type TaskAmountResponse = {
  developerId: string
  sinceDays: number
  taskAmount: number
}

function query(params: Record<string, string | number>) {
  return new URLSearchParams(
    Object.entries(params).map(([key, value]) => [key, String(value)]),
  ).toString()
}

export function listLoaderData(token: string) {
  return apiRequest<LoaderItem[]>('/api/loader/get-all', { token })
}

export function triggerLoaderScan(token: string) {
  return apiRequest<string>('/api/loader/scan', { token })
}

export function listMetrics(token: string) {
  return apiRequest<Metric[]>('/api/metric/get-all', { token })
}

export function createMetric(token: string, payload: MetricPayload) {
  return apiRequest<Metric>('/api/metric/create', {
    method: 'POST',
    token,
    body: payload,
  })
}

export function updateMetric(
  token: string,
  metricId: number,
  payload: MetricPayload,
) {
  return apiRequest<Metric>(`/api/metric/update/${metricId}`, {
    method: 'PUT',
    token,
    body: payload,
  })
}

export function deleteMetric(token: string, metricId: number) {
  return apiRequest<void>(`/api/metric/delete/${metricId}`, {
    method: 'DELETE',
    token,
  })
}

export function listActions(token: string) {
  return apiRequest<ActionRecord[]>('/api/action/get-all', { token })
}

export function createAction(token: string, payload: ActionPayload) {
  return apiRequest<ActionRecord>('/api/action/create', {
    method: 'POST',
    token,
    body: payload,
  })
}

export function updateAction(
  token: string,
  actionId: number,
  payload: ActionPayload,
) {
  return apiRequest<ActionRecord>(`/api/action/update/${actionId}`, {
    method: 'PUT',
    token,
    body: payload,
  })
}

export function deleteAction(token: string, actionId: number) {
  return apiRequest<void>(`/api/action/delete/${actionId}`, {
    method: 'DELETE',
    token,
  })
}

export function restoreAction(token: string, actionId: number) {
  return apiRequest<ActionRecord>(`/api/action/restore/${actionId}`, {
    method: 'PATCH',
    token,
  })
}

export function processAction(token: string, actionId: number) {
  return apiRequest<ActionRecord>(`/api/action/process/${actionId}`, {
    method: 'POST',
    token,
  })
}

export function listProcessorLoaderData(token: string) {
  return apiRequest<LoaderItem[]>('/api/processor/get-all-data-loader', {
    token,
  })
}

export function listLogs(token: string) {
  return apiRequest<LogEntry[]>('/api/logger/get-all', { token })
}

export function listUsers(token: string) {
  return apiRequest<UserAccount[]>('/api/user/get-all', { token })
}

export function listRoles(token: string) {
  return apiRequest<RoleRecord[]>('/api/role/get-all', { token })
}

export function createUser(token: string, payload: UserPayload) {
  return apiRequest<UserAccount>('/api/user/create', {
    method: 'POST',
    token,
    body: payload,
  })
}

export function deleteUser(token: string, userId: number) {
  return apiRequest<string>(`/api/user/delete/${userId}`, {
    method: 'DELETE',
    token,
  })
}

export function addUserRoles(
  token: string,
  userId: number,
  roles: string[],
) {
  return apiRequest<UserAccount>(`/api/user/add-roles/${userId}`, {
    method: 'PATCH',
    token,
    body: { roles },
  })
}

export function removeUserRoles(
  token: string,
  userId: number,
  roles: string[],
) {
  return apiRequest<UserAccount>(`/api/user/remove-roles/${userId}`, {
    method: 'PATCH',
    token,
    body: { roles },
  })
}

export function getDeveloperWithMostLabel(
  token: string,
  label: string,
  since: number,
) {
  return apiRequest<DeveloperLabelCountResponse>(
    `/api/evaluation/developer/most-label?${query({ label, since })}`,
    { token },
  )
}

export function getLabelAggregate(
  token: string,
  developerId: string,
  since: number,
) {
  return apiRequest<LabelAggregateResponse>(
    `/api/evaluation/developer/${developerId}/label-aggregate?${query({
      since,
    })}`,
    { token },
  )
}

export function getTaskAmount(
  token: string,
  developerId: string,
  since: number,
) {
  return apiRequest<TaskAmountResponse>(
    `/api/evaluation/developer/${developerId}/task-amount?${query({ since })}`,
    { token },
  )
}
