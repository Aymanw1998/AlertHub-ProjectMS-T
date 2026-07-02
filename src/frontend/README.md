# Alert Hub Frontend

React + TypeScript + Vite UI for the Alert Hub microservices project.

## Run locally

Start the backend Docker Compose first, then run:

```powershell
npm install
npm run dev
```

In development the UI calls `/api/*`; Vite forwards those calls to GatewayMS at `http://127.0.0.1:1007`.

Default demo login:

```text
admin / admin
```

## API integration

The UI uses the Gateway routes exposed by the backend:

- `POST /api/auth/signin`
- `POST /api/auth/signup`
- `GET /api/loader/get-all`
- `GET /api/loader/scan`
- `GET|POST|PUT|DELETE /api/metric/*`
- `GET|POST|PUT|DELETE /api/action/*`
- `GET /api/processor/get-all-data-loader`
- `GET /api/evaluation/*`
- `GET /api/logger/get-all`
- `GET|POST|PATCH|DELETE /api/user/*`
- `GET /api/role/get-all`

For production hosting, set `VITE_API_BASE_URL` if the frontend is not served behind the same Gateway origin. If it is served behind the same origin, the default relative `/api` paths are used.
