---
paths:
  - "**/*.java"
---
# Java coding style

- **Records:** one record per file. No nested records. Only the canonical constructor, with no convenience constructors.
- **Nullability:** annotate record components with `@NonNull` / `@Nullable`. Don't add comments that repeat an annotation.
- **Null checks:** name the parameter in the message: `Objects.requireNonNull(symbol, "symbol cannot be null")`.
- **Never return null.** Use `Optional<T>` for values that may be absent.
- **Naming:** simple names (`getCurrentPrice()`, not `getCurrentPriceOptional()`). Don't override record accessors just to return the field.
- **Separate types for separate concepts:** `Ticker` and `Isin`, not one `InstrumentSymbol` that handles both.
- **No version references** (v0.1, v0.2) in code or comments.
- **Avoid duplicate calculations:** compute once and pass the value into private helpers (e.g. `calculateWeightedAverageCostBasis(totalQuantity)`).
- **Constructor injection only.** No field `@Autowired`.
