## 2024-07-11 - Jetpack Compose Touch Targets
**Learning:** Using `Icon(modifier = Modifier.clickable)` in Jetpack Compose creates custom touch targets that might not meet standard minimums (e.g., 48dp) and may have incorrectly shaped or sized ripples (unless explicitly configured). This is a common accessibility anti-pattern.
**Action:** Always prefer using `IconButton(onClick = { ... }) { Icon(...) }` instead of making an Icon directly clickable. `IconButton` automatically provides a proper 48dp minimum touch target and applies the correct, circular ripple effect.
