const API_BASE_URL = 'http://localhost:1007'

type AuthPayload = {
  username: string
  password: string
}

type AuthResponse = {
  token?: string
  jwt?: string
  accessToken?: string
}

async function postAuth(endpoint: string, payload: AuthPayload): Promise<string> {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    const errorText = await response.text()
    throw new Error(errorText || 'Authentication request failed.')
  }

  const data = (await response.json()) as AuthResponse
  const token = data.token ?? data.jwt ?? data.accessToken

  if (!token) {
    throw new Error('Missing JWT token in API response.')
  }

  return token
}

export function login(payload: AuthPayload): Promise<string> {
  return postAuth('/api/auth/signin', payload)
}

export function signup(payload: AuthPayload): Promise<string> {
  return postAuth('/api/auth/signup', payload)
}
