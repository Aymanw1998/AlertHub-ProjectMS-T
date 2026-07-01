import { Navigate } from 'react-router-dom'
import { hasToken } from '../lib/tokenStorage'

type ProtectedRouteProps = {
  children: React.ReactNode
}

export function ProtectedRoute({ children }: ProtectedRouteProps) {
  if (!hasToken()) {
    return <Navigate to="/login" replace />
  }

  return children
}
