## 2024-07-14 - Improve dialpad key ripple effects
**Learning:** In Jetpack Compose, the default ripple effect bleeds as a rectangle if applied to a non-clipped size modifier on custom components. The `.clickable` modifier applies ripple, but without `.clip(CircleShape)` preceding it, the ripple extends beyond the circular intention.
**Action:** Always apply `.clip(Shape)` before `.clickable` for non-rectangular components. Also, prefer `IconButton` over `Modifier.clickable` on `Icon`s for standard touch targets.
