const JWT_STORAGE_KEY = 'auth_jwt'

export function getToken(): string | null {
  return localStorage.getItem(JWT_STORAGE_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(JWT_STORAGE_KEY, token)
}

export function clearToken(): void {
  localStorage.removeItem(JWT_STORAGE_KEY)
}
