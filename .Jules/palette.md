## 2024-07-10 - Touch targets in Jetpack Compose
**Learning:** Using `Icon(modifier = Modifier.clickable { ... })` instead of `IconButton` is a common accessibility anti-pattern in Compose, as it bypasses the standard 48dp minimum touch target area and the default circular ripple state indicators.
**Action:** Always prefer using `IconButton(onClick = { ... }) { Icon(...) }` for standalone interactive icons to ensure adherence to standard touch targets and provide immediate visual feedback.
