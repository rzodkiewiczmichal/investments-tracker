# Import Formats - TODO
**Version:** 1.0
**Date:** 2025-09-16
**Status:** Awaiting broker export samples

## Context
Each broker provides data in a different export format. We need to support multiple import formats to handle all 6 accounts.

## Known Accounts Requiring Import Support

1. **Regular Brokerage Accounts** (multiple brokers)
   - Format: TBD
   - Sample file: Not yet provided
   - Scenario name: TBD based on broker name

2. **IKE Account** (Indywidualne Konto Emerytalne)
   - Format: TBD
   - Sample file: Not yet provided
   - Scenario name: "Import IKE account positions"

3. **IKZE Account** (Indywidualne Konto Zabezpieczenia Emerytalnego)
   - Format: TBD
   - Sample file: Not yet provided
   - Scenario name: "Import IKZE account positions"

4. **Polish Government Bonds Account**
   - Format: TBD (likely different structure than stock/ETF accounts)
   - Sample file: Not yet provided
   - Scenario name: "Import government bonds positions"

## Next Steps

1. **Collect sample export files** from each broker/account
2. **Analyze each format** for:
   - File type (CSV, Excel, PDF, etc.)
   - Column headers/structure
   - Data encoding (UTF-8, Windows-1250 for Polish characters)
   - Delimiter (comma, semicolon, tab)
   - Date format
   - Number format (decimal separator, thousand separator)
   - Currency representation

3. **Create specific Gherkin scenarios** for each format in `data-import.feature`

4. **Design import adapters** for each format in the implementation phase

## Format Analysis Template

When samples are provided, document each format:

```markdown
### [Broker Name] Format

**File Type:** CSV/Excel/Other
**Encoding:** UTF-8/Windows-1250/Other
**Delimiter:** comma/semicolon/tab
**Sample Header Row:**
```
[paste here]
```
**Mapping to Domain Model:**
- Instrument Name → Column X
- Quantity → Column Y
- Average Cost → Column Z (or needs calculation)
- Account → [How to identify]

**Special Considerations:**
- [Any quirks or special handling needed]
```

## Implementation Notes

- Consider creating a flexible import framework that can handle various formats
- Each broker gets its own import adapter/parser
- Common validation rules apply regardless of format
- User should be able to identify which broker format they're importing