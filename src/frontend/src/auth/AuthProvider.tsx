import {
  useCallback,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { signIn, type SignInResponse, type SignupRequest } from '../api/authApi'
import { signUp } from '../api/authApi'
import {
  AuthContext,
  type AuthContextValue,
  type AuthState,
} from './authContext'

const AUTH_STORAGE_KEY = 'alerthub-auth'

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
    roles: Array.isArray(response.roles) ? response.roles : [],
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
      return auth.username === 'admin' || auth.roles.includes(role)
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
