# ADR-019: Frontend Project Structure

## Status
Accepted

## Context

The Angular frontend needs a clear, scalable project structure that:
- Separates concerns (core, features, shared)
- Enables feature-based development
- Supports lazy loading
- Aligns with Angular best practices

## Decision

### Directory Structure

```
frontend/
├── src/
│   ├── app/
│   │   ├── core/                    # Singleton services, interceptors, guards
│   │   │   ├── interceptors/
│   │   │   │   └── error.interceptor.ts
│   │   │   ├── models/              # TypeScript interfaces/types
│   │   │   │   ├── account.model.ts
│   │   │   │   ├── error.model.ts
│   │   │   │   ├── money.model.ts
│   │   │   │   ├── portfolio.model.ts
│   │   │   │   ├── position.model.ts
│   │   │   │   └── index.ts         # Barrel export
│   │   │   └── services/            # API services
│   │   │       ├── account.service.ts
│   │   │       ├── api.config.ts
│   │   │       ├── portfolio.service.ts
│   │   │       ├── position.service.ts
│   │   │       └── index.ts         # Barrel export
│   │   ├── features/                # Feature modules (lazy loaded)
│   │   │   ├── portfolio/
│   │   │   │   └── portfolio-view/
│   │   │   │       ├── portfolio-view.component.ts
│   │   │   │       ├── portfolio-view.component.html
│   │   │   │       └── portfolio-view.component.scss
│   │   │   └── positions/
│   │   │       ├── position-details/
│   │   │       │   └── ...
│   │   │       └── position-entry/
│   │   │           └── ...
│   │   ├── shared/                  # Shared components, pipes, directives (future)
│   │   ├── app.component.ts         # Root component
│   │   ├── app.config.ts            # Application configuration
│   │   └── app.routes.ts            # Route definitions
│   ├── styles.scss                  # Global styles
│   ├── index.html                   # HTML entry point
│   └── main.ts                      # Bootstrap
├── angular.json                     # Angular CLI configuration
├── package.json                     # Dependencies
├── proxy.conf.json                  # Dev server proxy configuration
├── tsconfig.json                    # TypeScript configuration
└── tsconfig.app.json                # App-specific TS config
```

### Core Module (`/core`)

Contains singleton services and application-wide concerns:

- **services/**: API communication services (one per backend resource)
- **models/**: TypeScript interfaces matching API DTOs
- **interceptors/**: HTTP interceptors (error handling, auth)
- **guards/**: Route guards (future: auth guards)

**Import Rule:** Core imports nothing from features or shared.

### Features Module (`/features`)

Contains feature-specific components organized by domain:

- **portfolio/**: Portfolio viewing functionality
- **positions/**: Position viewing, creation, editing

**Structure per feature:**
- Each feature has its own folder
- Components follow Angular naming: `*.component.ts`, `*.component.html`, `*.component.scss`
- Features are lazy-loaded via routes

**Import Rule:** Features can import from core and shared, but not from other features.

### Shared Module (`/shared`) - Future

Will contain reusable components, pipes, and directives:

- **components/**: Reusable UI components
- **pipes/**: Custom pipes (formatters)
- **directives/**: Custom directives

**Import Rule:** Shared imports only from core (models), never from features.

### Barrel Exports

Index files (`index.ts`) provide clean imports:

```typescript
// frontend/src/app/core/models/index.ts
export * from './money.model';
export * from './portfolio.model';
export * from './position.model';
export * from './account.model';
export * from './error.model';

// Usage in components:
import { PortfolioSummary, PositionSummary, ApiError } from '../../../core/models';
```

## Consequences

### Positive

1. **Clear Boundaries**: Each folder has defined responsibility
2. **Scalability**: Easy to add new features
3. **Lazy Loading**: Features loaded on demand
4. **Team Collaboration**: Different teams can work on different features
5. **Testing**: Clear module boundaries simplify testing

### Negative

1. **Deep Imports**: Feature components have deep relative imports to core
2. **Boilerplate**: Each feature requires similar folder structure
3. **Shared State**: Cross-feature state requires careful design

### Mitigation

- Use TypeScript path aliases for cleaner imports (future)
- Create generators/schematics for consistent feature scaffolding
- Consider NgRx or similar for complex cross-feature state (v0.3+)

## Related Decisions

- [ADR-018: Angular 19 Frontend Framework](ADR-018-angular-19-frontend-framework.md)
- [ADR-004: Package Structure](ADR-004-package-structure.md) - Backend structure inspiration
