package org.neteinstein.couples.domain.analytics

/**
 * The screens reported through [AnalyticsTracker.logScreenView]. An enum rather than free-form
 * strings so the set stays closed: Firebase groups its whole engagement/retention reporting by
 * `screen_name`, and a typo or a route string leaking in (`home?category=Intimacy`) silently
 * splits one screen into several in every one of those reports.
 *
 * [Questions] and [Game] are the two tabs inside the single `home` navigation destination (see
 * `MainScreen`) - they are separate screens to the user, so they are separate screens here.
 */
enum class AnalyticsScreen(
    val screenName: String,
) {
    Splash("splash"),
    Questions("questions"),
    Game("game"),
    Settings("settings"),
}
