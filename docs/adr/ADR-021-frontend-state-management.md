# ADR-021: Frontend State Management

## Status
Accepted

## Context

The Angular frontend needs a strategy for managing:
- Component-level state (loading, error, data)
- Form state (values, validation)
- API response data

### Requirements

1. **Simplicity**: MVP doesn't need complex state management
2. **Reactivity**: UI should reflect state changes immediately
3. **Type Safety**: State should be strongly typed
4. **Testability**: State management should be easy to test

### Options Considered

1. **Angular Signals** - Built-in fine-grained reactivity
2. **RxJS BehaviorSubject** - Traditional Angular reactive pattern
3. **NgRx** - Redux-style state management
4. **NGXS** - Simplified Redux pattern
5. **Akita** - Entity-based state management

## Decision

Use **Angular Signals** for component state and **Reactive Forms** for form state.

### Component State with Signals

```typescript
export class PortfolioViewComponent {
  // Primitive state
  loading = signal(true);
  error = signal<string | null>(null);

  // Object state
  portfolio = signal<PortfolioSummary | null>(null);

  // Array state
  positions = signal<PositionSummary[]>([]);

  // Update state
  loadData(): void {
    this.loading.set(true);
    this.portfolioService.getPortfolioSummary().subscribe({
      next: (data) => {
        this.portfolio.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.message);
        this.loading.set(false);
      }
    });
  }
}
```

### Template Usage

Signals are called as functions in templates:

```html
@if (loading()) {
  <p-skeleton />
} @else if (error()) {
  <p-message severity="error" [text]="error()!" />
} @else {
  <div>{{ portfolio()!.totalCurrentValue.amount }}</div>
}
```

### Form State with Reactive Forms

```typescript
export class PositionEntryComponent {
  private readonly fb = inject(FormBuilder);

  form = this.fb.group({
    instrumentName: ['', Validators.required],
    instrumentSymbol: ['', Validators.required],
    quantity: [null as number | null, [Validators.required, Validators.min(0.00000001)]],
    averageCost: [null as number | null, [Validators.required, Validators.min(0.0001)]]
  });

  onSubmit(): void {
    if (this.form.valid) {
      const command = this.form.value as AddPositionCommand;
      // Submit to API
    }
  }
}
```

## Rationale

### Why Signals over BehaviorSubject

| Aspect | Signals | BehaviorSubject |
|--------|---------|-----------------|
| Syntax | `count()` | `count$ \| async` |
| Change Detection | Fine-grained | Zone-based |
| Boilerplate | Minimal | More setup |
| Memory Management | Automatic | Manual unsubscribe |
| Template Usage | Direct call | Async pipe |

### Why Not NgRx/NGXS

1. **MVP Scope**: Only 3 views, no complex state sharing
2. **Complexity**: Redux patterns add significant boilerplate
3. **Learning Curve**: Team unfamiliar with Redux concepts
4. **Performance**: Not needed for current scale
5. **Future Option**: Can migrate to NgRx if state grows complex

### State Location Strategy

| State Type | Location | Tool |
|------------|----------|------|
| UI State (loading, errors) | Component | Signals |
| View Data | Component | Signals |
| Form Data | Component | Reactive Forms |
| Global State (user, theme) | Service | Signals (future) |

## Consequences

### Positive

1. **Simple Mental Model**: State is just function calls
2. **Fine-grained Updates**: Only affected DOM updates
3. **No Memory Leaks**: Signals auto-cleanup
4. **Type Safe**: Full TypeScript support
5. **Modern Angular**: Aligns with Angular's direction

### Negative

1. **Limited State Sharing**: No built-in cross-component state
2. **No DevTools**: No Redux-style time-travel debugging
3. **Learning**: Different from traditional RxJS patterns

### Mitigation

1. **Service State**: For shared state, use signals in services
2. **Future Migration**: Structure allows easy migration to NgRx if needed
3. **Documentation**: Document signal patterns for team

## Implementation Guidelines

### Signal Naming Conventions

```typescript
// Use descriptive names without $ suffix
loading = signal(false);        // Not: loading$
error = signal<string | null>(null);
portfolio = signal<Portfolio | null>(null);
positions = signal<Position[]>([]);
```

### Computed Values

Use `computed()` for derived state:

```typescript
totalValue = computed(() =>
  this.positions().reduce((sum, p) => sum + p.currentValue.amount, 0)
);
```

### Effect for Side Effects

Use `effect()` sparingly for side effects:

```typescript
constructor() {
  effect(() => {
    if (this.error()) {
      console.error('Error occurred:', this.error());
    }
  });
}
```

## Related Decisions

- [ADR-018: Angular 19 Frontend Framework](ADR-018-angular-19-frontend-framework.md)
- [ADR-010: Error Handling Strategy](ADR-010-error-handling-strategy.md) - Error state patterns
