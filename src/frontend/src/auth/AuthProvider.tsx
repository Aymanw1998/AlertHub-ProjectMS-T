import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { signIn, type SignInResponse, type SignupRequest } from '../api/authApi'
import { signUp } from '../api/authApi'

const AUTH_STORAGE_KEY = 'alerthub-auth'

type AuthState = {
  token: string
  userId: number
  username: string
  roles: string[]
}

type AuthContextValue = {
  auth: AuthState | null
  isAuthenticated: boolean
  signInWithPassword: (username: string, password: string) => Promise<void>
  signupNewUser: (payload: SignupRequest) => Promise<void>
  logout: () => void
  hasRole: (role: string) => boolean
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

function getInitialAuthState(): AuthState | null {
  const rawValue = localStorage.getItem(AUTH_STORAGE_KEY)
  if (!rawValue) {
    return null
  }

  try {
    return JSON.parse(rawValue) as AuthState
  } catch {
    localStorage.removeItem(AUTH_STORAGE_KEY)
    return null
  }
}

function toAuthState(response: SignInResponse): AuthState {
  return {
    token: response.token,
    userId: response.userId,
    username: response.username,
    roles: response.roles,
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [auth, setAuth] = useState<AuthState | null>(() => getInitialAuthState())

  const persistAuth = useCallback((nextAuth: AuthState | null) => {
    setAuth(nextAuth)
    if (nextAuth) {
      localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(nextAuth))
      return
    }
    localStorage.removeItem(AUTH_STORAGE_KEY)
  }, [])

  const signInWithPassword = useCallback(
    async (username: string, password: string) => {
      const response = await signIn({ username, password })
      persistAuth(toAuthState(response))
    },
    [persistAuth],
  )

  const signupNewUser = useCallback(async (payload: SignupRequest) => {
    await signUp(payload)
  }, [])

  const logout = useCallback(() => {
    persistAuth(null)
  }, [persistAuth])

  const hasRole = useCallback(
    (role: string) => {
      if (!auth) {
        return false
      }
      return auth.roles.includes(role)
    },
    [auth],
  )

  const value = useMemo<AuthContextValue>(
    () => ({
      auth,
      isAuthenticated: Boolean(auth?.token),
      signInWithPassword,
      signupNewUser,
      logout,
      hasRole,
    }),
    [auth, hasRole, logout, signInWithPassword, signupNewUser],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider')
  }

  return context
}
