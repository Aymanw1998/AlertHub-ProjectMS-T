const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export type RoleResponseDTO = {
  id: number
  role: string
}

export type UserResponseDTO = {
  id: number
  username: string
  email: string
  phone: string
  roles: RoleResponseDTO[]
}

export type UserRequestDTO = {
  username: string
  email: string
  phone: string
  password: string
  roles: string[]
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, options)

  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || `Request failed with ${response.status}`)
  }

  if (response.status === 204) {
    return undefined as T
  }

  const text = await response.text()
  if (!text) {
    return undefined as T
  }

  try {
    return JSON.parse(text) as T
  } catch {
    return text as T
  }
}

function jsonOptions(method: string, body: unknown): RequestInit {
  return {
    method,
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  }
}

export function createUser(data: UserRequestDTO) {
  return request<UserResponseDTO>('/api/user/create', jsonOptions('POST', data))
}

export function updateUser(id: number, data: UserRequestDTO) {
  return request<UserResponseDTO>(`/api/user/update/${id}`, jsonOptions('PUT', data))
}

export function deleteUser(id: number) {
  return request<string>(`/api/user/delete/${id}`, { method: 'DELETE' })
}

export function getUsers() {
  return request<UserResponseDTO[]>('/api/user/get-all')
}

export function getRoles() {
  return request<RoleResponseDTO[]>('/api/role/get-all')
}
