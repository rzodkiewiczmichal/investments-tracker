# ADR-018: Angular 19 Frontend Framework

## Status
Accepted

## Context

The Investment Tracker application requires a frontend application to provide a user interface for:
- Viewing portfolio summary and positions (FR-001 to FR-014)
- Manually entering positions (FR-041 to FR-046)
- Future features like imports, price management, reconciliation

### Requirements

1. **Type Safety**: Strong typing to match Java backend rigor
2. **Enterprise Ready**: Suitable for business application development
3. **API Integration**: Easy consumption of REST APIs (ADR-009)
4. **Long-term Support**: Framework with stable release cycle
5. **Developer Experience**: Good tooling and documentation

### Options Considered

1. **Angular 19** - Google's enterprise framework
2. **React 18** - Facebook's component library
3. **Vue 3** - Progressive framework
4. **Svelte 5** - Compiler-based framework

## Decision

Use **Angular 19** with the following key features:

### 1. Standalone Components (No NgModules)

All components are standalone, eliminating the need for NgModules:

```typescript
@Component({
  selector: 'app-portfolio-view',
  standalone: true,
  imports: [CommonModule, CardModule, TableModule],
  templateUrl: './portfolio-view.component.html',
  styleUrl: './portfolio-view.component.scss'
})
export class PortfolioViewComponent { }
```

**Rationale:**
- Simpler mental model
- Better tree-shaking
- Easier testing
- Modern Angular best practice

### 2. Signals for Reactive State

Use Angular Signals instead of RxJS BehaviorSubjects for component state:

```typescript
export class PortfolioViewComponent {
  portfolio = signal<PortfolioSummary | null>(null);
  positions = signal<PositionSummary[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
}
```

**Rationale:**
- Simpler API than RxJS for local state
- Better change detection integration
- Less boilerplate
- Angular's recommended approach

### 3. Control Flow Syntax

Use new control flow syntax (`@if`, `@for`, `@else`) instead of structural directives:

```html
@if (loading()) {
  <p-skeleton />
} @else if (error()) {
  <p-message severity="error" [text]="error()!" />
} @else {
  <p-table [value]="positions()" />
}
```

**Rationale:**
- Better performance (no need for NgIf/NgFor directives)
- Cleaner template syntax
- Built-in type narrowing

### 4. Functional Interceptors

Use functional HTTP interceptors instead of class-based:

```typescript
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Error handling
    })
  );
};
```

**Rationale:**
- Simpler than class-based interceptors
- Easier to test
- Modern Angular pattern

### 5. Lazy Loading with loadComponent

Routes use dynamic imports for lazy loading:

```typescript
export const routes: Routes = [
  {
    path: 'portfolio',
    loadComponent: () => import('./features/portfolio/portfolio-view/portfolio-view.component')
      .then(m => m.PortfolioViewComponent)
  }
];
```

**Rationale:**
- Reduces initial bundle size
- Improves startup performance
- Each feature loaded on demand

## Consequences

### Positive

1. **Type Safety**: TypeScript integration with strict mode
2. **Tooling**: Angular CLI provides excellent DX
3. **Testing**: Built-in testing utilities
4. **Performance**: Signals enable fine-grained reactivity
5. **Future Proof**: Using latest Angular patterns
6. **Enterprise Support**: Long-term support from Google

### Negative

1. **Learning Curve**: Angular has steeper learning curve than Vue/Svelte
2. **Bundle Size**: Larger than Svelte/Vue (mitigated by lazy loading)
3. **Complexity**: More boilerplate than simpler frameworks

### Mitigation

- Use lazy loading to minimize initial bundle
- Follow Angular style guide for consistency
- Use strict TypeScript settings to catch errors early

## Related Decisions

- [ADR-009: REST API Structure](ADR-009-rest-api-structure.md) - API contract for frontend
- [ADR-019: Frontend Project Structure](ADR-019-frontend-project-structure.md) - Folder organization
- [ADR-020: PrimeNG UI Framework](ADR-020-primeng-ui-framework.md) - UI component library

## References

- [Angular 19 Documentation](https://angular.dev/)
- [Angular Signals](https://angular.dev/guide/signals)
- [Standalone Components](https://angular.dev/guide/components/importing)
