## 2026-07-04 - [Combine Dialer and Recents Screen Filtering]
**Learning:** In Android Compose, filtering a combined list of Recents and Contacts purely locally based on exact sub-string matches is fast but must deduplicate efficiently based on phone numbers to avoid visual clutter. Furthermore, using a `FloatingActionButton` pattern to dynamically toggle the keypad provides excellent screen economy for scrolling through large contact lists.
**Action:** Always implement a derived state `remember(input)` flow when handling search strings so recomposition dynamically drives the UI filtering cleanly without background coroutines for small-medium lists.

## 2023-10-27 - Android Foreground Notification Status Bar Pill Colors
**Learning:** In Android, creating a colorized status bar indicator (the colored pill behind the icon) for an ongoing background task (like a call) requires more than just the `setColor()` method. It explicitly requires setting `.setColorized(true)` and `.setOngoing(true)` on the `NotificationCompat.Builder`. Without these, the system ignores the requested color for the status bar presentation and uses the default system notification tint.
**Action:** When implementing custom background colors for ongoing system notifications, ensure `setColorized(true)` and `setOngoing(true)` are explicitly enabled on the builder.

## 2023-10-27 - Grouped List Items UX
**Learning:** For a recent calls list, displaying an individual row for every single call log rapidly clutters the UI and requires excessive scrolling. Grouping calls by contact/number and presenting a single entry with a summarized call count significantly improves scannability. Furthermore, using a trailing "Info" icon (`IconButton` with `Icons.Outlined.Info`) establishes a clear, standard pattern for users to navigate to detailed historical context without overwhelming the primary dialing interaction.
**Action:** When designing history/log-based lists, prioritize data grouping (e.g., by entity or time block) and provide secondary discovery mechanisms (like detail screens) rather than flattening all data into the root view.

## 2026-07-14 - Navigation Bar Resizing & Layout Glitches
**Learning:** Using `AnimatedVisibility` for conditionally showing text inside a navigation bar item that uses `Modifier.weight(1f)` causes layout jumps. When the text appears/disappears, it alters the size of the Row's internal content within a fixed-weight constraint.
**Action:** When a design requires a dynamic expansion (e.g., active tabs are wider to accommodate text, inactive tabs just show icons), animate the `Modifier.weight()` itself (using `animateFloatAsState`) instead of solely relying on `AnimatedVisibility` for the child elements. This creates a smooth resizing effect of the container that fluidly repulses sibling elements rather than causing instant clipping or janky transitions.

## 2026-07-14 - Accessibility Image Placeholders
**Learning:** Setting `contentDescription = null` for user profile/avatar icons is inaccessible. Even generic placeholders (like a default `Person` icon) convey important information to screen reader users (that this area represents a user profile/avatar).
**Action:** Always provide descriptive `contentDescription` text (e.g., "Contact avatar") for decorative or fallback icons used in place of user imagery.
