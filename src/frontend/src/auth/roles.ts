export const AppRoles = {
  admin: 'admin',
  read: 'read',
  createAction: 'createAction',
  updateAction: 'updateAction',
  deleteAction: 'deleteAction',
  createMetric: 'createMetric',
  updateMetric: 'updateMetric',
  deleteMetric: 'deleteMetric',
  triggerScan: 'triggerScan',
  triggerProcess: 'triggerProcess',
  triggerEvaluation: 'triggerEvaluation',
} as const

export type AppRole = (typeof AppRoles)[keyof typeof AppRoles]
