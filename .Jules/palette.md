## 2024-07-09 - Accessible Touch Targets for Icons
**Learning:** Using `Modifier.clickable` directly on an `Icon` in Jetpack Compose does not automatically provide the standard 48dp minimum accessible touch target or a correct circular ripple.
**Action:** Always use `IconButton` (or `IconToggleButton`) wrapping an `Icon` for icon-only buttons to automatically handle minimum touch target size (48x48dp) and standard circular ripple indication out of the box.
