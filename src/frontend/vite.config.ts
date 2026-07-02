import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import type { IncomingHttpHeaders } from 'node:http'

const gatewayTarget = process.env.VITE_API_TARGET ?? 'http://127.0.0.1:1007'
const skippedProxyHeaders = new Set([
  'accept-encoding',
  'connection',
  'content-length',
  'expect',
  'host',
  'keep-alive',
  'proxy-authenticate',
  'proxy-authorization',
  'te',
  'trailer',
  'transfer-encoding',
  'upgrade',
])

function toFetchHeaders(headers: IncomingHttpHeaders) {
  const nextHeaders: Record<string, string> = {}

  for (const [key, value] of Object.entries(headers)) {
    if (!value || skippedProxyHeaders.has(key.toLowerCase())) {
      continue
    }

    nextHeaders[key] = Array.isArray(value) ? value.join(',') : value
  }

  return nextHeaders
}

function alerthubApiProxy() {
  return {
    name: 'alerthub-api-proxy',
    configureServer(server: import('vite').ViteDevServer) {
      server.middlewares.use(async (request, response, next) => {
        if (!request.url?.startsWith('/api')) {
          next()
          return
        }

        try {
          const method = request.method ?? 'GET'
          const chunks: Buffer[] = []

          for await (const chunk of request) {
            chunks.push(Buffer.isBuffer(chunk) ? chunk : Buffer.from(chunk))
          }

          const upstream = await fetch(`${gatewayTarget}${request.url}`, {
            method,
            headers: toFetchHeaders(request.headers),
            body:
              method === 'GET' || method === 'HEAD' || chunks.length === 0
                ? undefined
                : Buffer.concat(chunks),
          })

          response.statusCode = upstream.status
          upstream.headers.forEach((value, key) => {
            if (
              !['content-encoding', 'content-length', 'transfer-encoding'].includes(
                key.toLowerCase(),
              )
            ) {
              response.setHeader(key, value)
            }
          })

          response.end(Buffer.from(await upstream.arrayBuffer()))
        } catch (error) {
          const cause =
            error instanceof Error && 'cause' in error
              ? String(error.cause)
              : undefined
          response.statusCode = 502
          response.setHeader('content-type', 'application/json')
          response.end(
            JSON.stringify({
              error: 'Gateway proxy failed',
              message: error instanceof Error ? error.message : 'Unknown error',
              cause,
            }),
          )
        }
      })
    },
  }
}

export default defineConfig({
  plugins: [react(), alerthubApiProxy()],
  server: {
    port: 5173,
  },
})
