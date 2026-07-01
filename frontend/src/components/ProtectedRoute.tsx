import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { getToken } from '../lib/tokenStorage'

type ProtectedRouteProps = {
  children: ReactNode
}

export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const token = getToken()

  if (!token) {
    return <Navigate to="/login" replace />
  }

  return children
}
