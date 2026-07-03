# Palette's UX Journal

## 2024-07-03 - [Fix Circular Button Ripple Bleed in Compose]
**Learning:** In Jetpack Compose, the default `clickable` modifier applies a rectangular ripple effect which bleeds outside of `CircleShape` containers if they are not explicitly clipped or if `clickable` is applied before `clip`. While `clip` works, for custom interactive elements, completely removing the default indication via `interactionSource = remember { MutableInteractionSource() }` and `indication = null` provides full control over the visual feedback and prevents ugly rectangular bleeds on circular UI elements like dialer keys.
**Action:** When creating custom circular buttons or keys in Compose, explicitly handle the `interactionSource` and `indication` to ensure visual polish and avoid default rectangular ripples bleeding outside the intended shape.
