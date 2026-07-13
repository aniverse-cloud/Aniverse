
## 2024-07-13 - Jetpack Compose Touch Targets and Ripple Shapes
**Learning:** Using `Modifier.clickable` directly on an `Icon` component bypasses the standard 48dp minimum touch target area required for good accessibility, and it defaults to a rectangular ripple which looks unpolished on standalone icons. Furthermore, when using custom container shapes (like an 80dp circular dialpad button), `Modifier.clickable` will bleed its ripple as a square unless it is explicitly constrained.
**Action:** Always prefer using `IconButton` wrappers over `Modifier.clickable` on raw `Icon`s. When using custom tap areas that aren't rectangular, always prepend `Modifier.clip(Shape)` (e.g., `.clip(CircleShape)`) *before* `Modifier.clickable` in the modifier chain to properly mask the ripple effect.
