const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

type RequestOptions = {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  token?: string
  body?: unknown
}

export class ApiRequestError extends Error {
  status: number
  body: unknown

  constructor(status: number, message: string, body: unknown) {
    super(message)
    this.name = 'ApiRequestError'
    this.status = status
    this.body = body
  }
}

export async function apiRequest<TResponse>(
  path: string,
  options: RequestOptions = {},
): Promise<TResponse> {
  const headers: HeadersInit = {}

  if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }

  if (options.token) {
    headers.Authorization = `Bearer ${options.token}`
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: options.method ?? 'GET',
    headers,
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  })

  const contentType = response.headers.get('content-type') ?? ''
  const isJson = contentType.includes('application/json')
  const responseBody = response.status === 204
    ? undefined
    : isJson
      ? await response.json()
      : await response.text()

  if (!response.ok) {
    const message =
      typeof responseBody === 'string'
        ? responseBody
        : `Request failed with status ${response.status}`
    throw new ApiRequestError(response.status, message, responseBody)
  }

  if (response.status === 204) {
    return {} as TResponse
  }

  return responseBody as TResponse
}

export { API_BASE_URL }
