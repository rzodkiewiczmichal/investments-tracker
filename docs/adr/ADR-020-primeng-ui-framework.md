# ADR-020: PrimeNG UI Framework

## Status
Accepted

## Context

The Investment Tracker frontend requires a UI component library that:
- Provides professional-looking components
- Has good Angular integration
- Offers data-heavy components (tables, forms)
- Supports theming and customization
- Is actively maintained

### Options Considered

1. **Angular Material** - Google's official Material Design components
2. **PrimeNG** - PrimeTek's comprehensive component suite
3. **Taiga UI** - Modern, customizable components
4. **NG-ZORRO** - Ant Design for Angular
5. **Tailwind + DaisyUI** - Utility-first CSS with component layer

## Decision

Use **PrimeNG v19** with the **Aura theme**.

### Configuration

```typescript
// app.config.ts
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeng/themes/aura';

export const appConfig: ApplicationConfig = {
  providers: [
    provideAnimationsAsync(),
    providePrimeNG({
      theme: {
        preset: Aura,
        options: {
          darkModeSelector: '.dark-mode'
        }
      }
    })
  ]
};
```

### Components Used

| Category | Components | Usage |
|----------|------------|-------|
| Layout | Card | Containers, sections |
| Data | Table | Position list with sorting, pagination |
| Form | InputText, InputNumber, Select | Position entry form |
| Feedback | Message, Skeleton | Errors, loading states |
| Navigation | Menubar, Button | App navigation, actions |
| Display | Tag, Divider | Instrument types, separators |

### Styling Approach

1. **Theme Variables**: Use PrimeNG CSS variables for consistency:
   ```scss
   color: var(--text-color);
   background: var(--surface-ground);
   border-color: var(--surface-200);
   ```

2. **Utility Classes**: Global utility classes in `styles.scss`:
   ```scss
   .text-right { text-align: right; }
   .positive { color: var(--green-500) !important; }
   .negative { color: var(--red-500) !important; }
   ```

3. **Component Scoped**: Feature-specific styles in component SCSS files.

## Rationale

### Why PrimeNG over alternatives:

| Criteria | PrimeNG | Material | Taiga UI | NG-ZORRO |
|----------|---------|----------|----------|----------|
| Component Count | 90+ | 40+ | 100+ | 70+ |
| Data Table | Excellent | Basic | Good | Excellent |
| Forms | Excellent | Good | Excellent | Good |
| Theming | Excellent | Limited | Excellent | Good |
| Angular 19 | Yes | Yes | Partial | Yes |
| Enterprise Ready | Yes | Yes | Yes | Yes |

**Key Advantages of PrimeNG:**

1. **Rich Data Table**: Built-in sorting, filtering, pagination, row selection
2. **Form Components**: Comprehensive input types with validation support
3. **Theme System**: Aura theme is modern and professional
4. **Documentation**: Extensive with live examples
5. **Long-term Support**: Active development, frequent releases

## Consequences

### Positive

1. **Rapid Development**: Pre-built components reduce development time
2. **Consistent UI**: All components follow same design language
3. **Accessibility**: Components have ARIA support built-in
4. **Responsive**: Mobile-friendly by default
5. **Customizable**: Theme variables allow easy branding

### Negative

1. **Bundle Size**: Adds ~150-200KB to initial bundle
2. **Version Coupling**: Must match Angular major version
3. **Learning Curve**: PrimeNG-specific APIs to learn
4. **Over-styling**: May need to override default styles

### Mitigation

1. **Tree Shaking**: Import only used components
2. **Version Management**: Pin primeng version to match Angular
3. **Documentation**: Refer to PrimeNG docs for component APIs
4. **CSS Variables**: Use theme variables instead of hardcoded values

## Implementation Notes

### Import Pattern

Import only required modules per component:

```typescript
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';

@Component({
  imports: [CardModule, TableModule, ButtonModule],
  // ...
})
```

### Required Packages

```json
{
  "dependencies": {
    "primeng": "^19.1.4",
    "primeicons": "^7.0.0",
    "@primeng/themes": "^19.1.4"
  }
}
```

### Required Styles

```scss
// styles.scss
@import 'primeicons/primeicons.css';
```

## Related Decisions

- [ADR-018: Angular 19 Frontend Framework](ADR-018-angular-19-frontend-framework.md)
- [ADR-019: Frontend Project Structure](ADR-019-frontend-project-structure.md)

## References

- [PrimeNG Documentation](https://primeng.org/)
- [Aura Theme](https://primeng.org/theming)
- [PrimeIcons](https://primeng.org/icons)
