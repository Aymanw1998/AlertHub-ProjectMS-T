import { apiRequest } from './client'

export type SignInRequest = {
  username: string
  password: string
}

export type SignInResponse = {
  token: string
  userId: number
  username: string
  roles?: string[]
}

export type SignupRequest = {
  username: string
  email: string
  password: string
  phone?: string
}

export async function signIn(payload: SignInRequest): Promise<SignInResponse> {
  return apiRequest<SignInResponse>('/api/auth/signin', {
    method: 'POST',
    body: payload,
  })
}

export async function signUp(payload: SignupRequest): Promise<void> {
  await apiRequest('/api/auth/signup', {
    method: 'POST',
    body: payload,
  })
}
