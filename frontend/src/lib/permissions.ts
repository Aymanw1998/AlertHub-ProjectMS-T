import { getUser } from './tokenStorage'

export type Permission =
  | 'createAction'
  | 'updateAction'
  | 'deleteAction'
  | 'createMetric'
  | 'updateMetric'
  | 'deleteMetric'
  | 'triggerScan'
  | 'triggerProcess'
  | 'triggerEvaluation'
  | 'read'

export function hasPermission(permission: Permission) {
  return getUser()?.roles.includes(permission) ?? false
}

export function hasAnyPermission(permissions: Permission[]) {
  return permissions.some((permission) => hasPermission(permission))
}

export function hasAllPermissions(permissions: Permission[]) {
  return permissions.every((permission) => hasPermission(permission))
}
