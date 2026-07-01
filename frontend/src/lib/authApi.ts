const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? ''

export type SigninRequest = {
  username: string
  password: string
}

export type SigninResponse = {
  token: string
  userId: number
  username: string
  roles: string[]
}

export type SignupRequest = {
  username: string
  email: string
  phone: string
  password: string
}

export type SignupResponse = {
  userId: number
  username: string
  message: string
}

async function request<T>(path: string, body: unknown): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  })

  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || `Request failed with ${response.status}`)
  }

  return response.json() as Promise<T>
}

export function signin(data: SigninRequest) {
  return request<SigninResponse>('/api/auth/signin', data)
}

export function signup(data: SignupRequest) {
  return request<SignupResponse>('/api/auth/signup', data)
}
