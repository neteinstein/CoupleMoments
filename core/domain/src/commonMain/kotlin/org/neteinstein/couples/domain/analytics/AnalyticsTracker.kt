package org.neteinstein.couples.domain.analytics

/**
 * Platform-agnostic analytics sink. `core:domain` declares it (like every other repository
 * interface here) so ViewModels and use cases can report without knowing which SDK - if any -
 * is behind it; `core:data`'s `platformDataModule` binds the implementation per target:
 *
 * | Target | Implementation | Notes |
 * |---|---|---|
 * | Android | `FirebaseAnalyticsTracker` | Firebase Analytics, configured from `androidApp/google-services.json` |
 * | Web | `FirebaseWebAnalyticsTracker` | Firebase JS SDK, loaded by the generated `firebase-init.js` |
 * | iOS | `NoOpAnalyticsTracker` | no Firebase iOS SDK in the build yet - see AGENTS.md |
 *
 * Every implementation must be **fail-soft**: analytics is never load-bearing, so a missing or
 * misconfigured SDK degrades to doing nothing rather than throwing into a caller. That is also
 * what makes a local build with no `google-services.json` (or a Web build with no Firebase
 * secrets configured) behave exactly like a configured one from the app's point of view.
 *
 * Nothing passed through this interface may carry user-authored or personally identifying
 * content: only the enumerated [AnalyticsEvent]/[AnalyticsUserProperty] values below, which are
 * all low-cardinality categories, plus the random device-scoped id from
 * [org.neteinstein.couples.domain.repository.AnalyticsUserIdRepository]. Question *text* in
 * particular must never be logged.
 */
interface AnalyticsTracker {
    /** Short, stable id of the platform this tracker runs on: `android`, `ios` or `web`. */
    val platform: String

    /**
     * Turns collection on or off at the SDK level, so an opted-out user stops being counted at
     * all rather than merely having this app stop calling [logEvent]. Applied on every app start
     * from the persisted choice (see
     * [org.neteinstein.couples.domain.usecase.InitializeAnalyticsUseCase]) and again whenever the
     * Settings toggle flips.
     */
    fun setCollectionEnabled(enabled: Boolean)

    /**
     * Associates subsequent events with [userId] - the random, device-scoped id from
     * [org.neteinstein.couples.domain.repository.AnalyticsUserIdRepository], never an account or
     * device identifier. `null` clears it.
     */
    fun setUserId(userId: String?)

    fun setUserProperty(
        property: AnalyticsUserProperty,
        value: String?,
    )

    /** Reports a screen the user just landed on, as Firebase's own `screen_view` event. */
    fun logScreenView(screen: AnalyticsScreen)

    fun logEvent(event: AnalyticsEvent)
}
