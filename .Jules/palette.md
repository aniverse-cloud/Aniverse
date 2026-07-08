## 2024-05-18 - Jetpack Compose Icon Accessibility
**Learning:** Using `Modifier.clickable` directly on an `Icon` in Jetpack Compose does not provide the standard 48dp minimum touch target size or the default circular ripple effect, which negatively impacts accessibility and user interaction feedback.
**Action:** Always wrap interactive `Icon` elements with `IconButton` to ensure standard touch target sizing and built-in ripple effects for an optimal and accessible UX.
