## 2026-07-07 - Jetpack Compose IconButton vs Icon+Clickable
**Learning:** In Jetpack Compose, wrapping an `Icon` with `Modifier.clickable` creates a rectangular touch target that can be too small for accessibility standards and has an inconsistent ripple effect.
**Action:** Always prefer using `IconButton` instead of `Icon(modifier = Modifier.clickable(...))` to automatically provide standard 48dp minimum touch targets and appropriate circular ripples for improved accessibility and UX.
