import { createContext } from 'react'
import type { SignupRequest } from '../api/authApi'

export type AuthState = {
  token: string
  userId: number
  username: string
  roles: string[]
}

export type AuthContextValue = {
  auth: AuthState | null
  isAuthenticated: boolean
  signInWithPassword: (username: string, password: string) => Promise<void>
  signupNewUser: (payload: SignupRequest) => Promise<void>
  logout: () => void
  hasRole: (role: string) => boolean
}

export const AuthContext = createContext<AuthContextValue | undefined>(
  undefined,
)
