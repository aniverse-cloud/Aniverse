## 2026-07-04 - [Combine Dialer and Recents Screen Filtering]
**Learning:** In Android Compose, filtering a combined list of Recents and Contacts purely locally based on exact sub-string matches is fast but must deduplicate efficiently based on phone numbers to avoid visual clutter. Furthermore, using a `FloatingActionButton` pattern to dynamically toggle the keypad provides excellent screen economy for scrolling through large contact lists.
**Action:** Always implement a derived state `remember(input)` flow when handling search strings so recomposition dynamically drives the UI filtering cleanly without background coroutines for small-medium lists.

## 2023-10-27 - Android Foreground Notification Status Bar Pill Colors
**Learning:** In Android, creating a colorized status bar indicator (the colored pill behind the icon) for an ongoing background task (like a call) requires more than just the `setColor()` method. It explicitly requires setting `.setColorized(true)` and `.setOngoing(true)` on the `NotificationCompat.Builder`. Without these, the system ignores the requested color for the status bar presentation and uses the default system notification tint.
**Action:** When implementing custom background colors for ongoing system notifications, ensure `setColorized(true)` and `setOngoing(true)` are explicitly enabled on the builder.
