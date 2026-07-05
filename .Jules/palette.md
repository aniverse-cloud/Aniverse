## 2026-07-05 - Replaced clickable Modifier with IconButton
**Learning:** In Jetpack Compose, wrapping an `Icon` with `modifier = Modifier.clickable` does not automatically apply a standardized touch target size (typically 48dp) and can sometimes result in incorrect ripple bounds compared to using `IconButton`.
**Action:** Always prefer using `IconButton(onClick = { ... }) { Icon(...) }` over `Icon(modifier = Modifier.clickable { ... })` for standard, icon-only buttons to ensure proper accessibility touch targets and visual feedback.
