---
paths:
  - "frontend/**"
---
# Frontend (Angular 19 + PrimeNG 19, Aura theme)

- Layout:
  - `src/app/core/{models,services,interceptors}`: models mirror the backend DTOs / `docs/api/openapi.yaml`, one API service per resource.
  - `src/app/features/{portfolio,positions,import}/`: standalone components.
  - Routes live in `app.routes.ts`.
- Standalone components with `inject()` and signals for state. No NgModules and no global store.
- Dev server on port 4200. `proxy.conf.json` forwards `/api` to `localhost:8080`.
- `error.interceptor.ts` maps backend `ApiError` and logs the trace id.
- Price-dependent values can be null. Render them as a dash, never as 0.
- **No hardcoded domain data** (brokers, accounts) in the UI.
- Quality gate: `npm run lint`, `npm run format:check`, `npm run build`. Tests are Karma/Jasmine, but CI doesn't run them yet.
