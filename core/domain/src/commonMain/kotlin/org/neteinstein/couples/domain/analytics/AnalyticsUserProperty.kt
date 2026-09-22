package org.neteinstein.couples.domain.analytics

/**
 * User-scoped dimensions every event can be segmented by in the Firebase console (Audiences,
 * and the "Compare" selector on any report). Unlike [AnalyticsEvent] params - which describe one
 * interaction - these describe the *user*, so they stick until changed and answer questions like
 * "do dark-theme users open the app more often?" or "how many users ever turn on parents mode?".
 *
 * [key] must satisfy Firebase's user-property rules: <= 24 characters, alphanumeric/underscore,
 * starting with a letter, and not using the reserved `firebase_`/`google_`/`ga_` prefixes.
 *
 * Every property here is a low-cardinality app setting. Firebase caps a project at 25 custom user
 * properties, and each one must be registered in the console before it shows up as a dimension -
 * see the PR description's manual steps.
 */
enum class AnalyticsUserProperty(
    val key: String,
) {
    /** The language the question content is being served in (`en`, `pt`, `es`, `fr`, `de`). */
    AppLanguage("app_language"),

    /** `light`, `dark` or `system` - the persisted [org.neteinstein.couples.domain.model.ThemeMode]. */
    ThemeMode("theme_mode"),

    /** Whether the "Couple Questions For Parents" set is included (`true`/`false`). */
    ParentsMode("parents_mode"),

    /** `android`, `ios` or `web` - see [AnalyticsTracker.platform]. */
    Platform("platform"),
}
