package org.neteinstein.couples.domain.analytics

import org.neteinstein.couples.domain.model.AppLanguage
import org.neteinstein.couples.domain.model.QuestionAudience
import org.neteinstein.couples.domain.model.QuestionCategory
import org.neteinstein.couples.domain.model.ThemeMode

/**
 * The closed set of events this app reports. Sealed rather than a free-form
 * `logEvent(String, Map)` on [AnalyticsTracker] for two reasons: Firebase silently drops anything
 * over its limits (40 characters per event name, 40 per parameter name, 100 per parameter *value*,
 * 25 parameters per event, 500 distinct event names per project), and an event whose name or
 * parameters drift between call sites produces two unrelated rows in the console that can never be
 * merged retroactively. Constructing them here means every call site spells them the same way, and
 * the names below can be checked once against those limits rather than at every call site.
 *
 * Names deliberately avoid Firebase's reserved ones (`app_open`, `screen_view`, `session_start`,
 * `first_open`, ...), which the SDK logs by itself; [AnalyticsTracker.logScreenView] is the one
 * place a reserved name is used, and it goes through the SDK's own constant.
 *
 * Parameter values are drawn from the explicit `analyticsValue` mappings at the bottom of this
 * file rather than from `Enum.name`/`simpleName`: the release build is obfuscated with R8 in full
 * mode (see `androidApp/proguard-rules.pro`), which is free to rewrite both, and an event
 * dimension whose values change between two releases is worse than no dimension at all. They also
 * never carry question text or anything a user typed - see [AnalyticsTracker]'s kdoc.
 */
sealed class AnalyticsEvent(
    val name: String,
    val params: Map<String, String> = emptyMap(),
) {
    /**
     * A question card became the visible one in the deck, whether by swipe, arrow key or the grid.
     * The core engagement metric: cards-per-session and which categories actually get browsed.
     */
    class QuestionViewed(
        category: QuestionCategory?,
        audience: QuestionAudience,
        languageCode: String,
    ) : AnalyticsEvent(
            name = "question_viewed",
            params =
                mapOf(
                    PARAM_CATEGORY to category.analyticsValue(),
                    PARAM_AUDIENCE to audience.analyticsValue(),
                    PARAM_LANGUAGE to languageCode,
                ),
        )

    /** The user swiped a card down, hiding it from rotation until "Reset Cards". */
    class QuestionHidden(
        category: QuestionCategory?,
    ) : AnalyticsEvent(
            name = "question_hidden",
            params = mapOf(PARAM_CATEGORY to category.analyticsValue()),
        )

    /** A category filter was applied on Home (`all` when the filter was cleared). */
    class CategorySelected(
        category: QuestionCategory?,
    ) : AnalyticsEvent(
            name = "category_selected",
            params = mapOf(PARAM_CATEGORY to category.analyticsValue()),
        )

    /** Home's deck/grid toggle. Tells us whether the swipe deck or the overview is the real UI. */
    class ViewModeChanged(
        gridView: Boolean,
    ) : AnalyticsEvent(
            name = "view_mode_changed",
            params = mapOf(PARAM_MODE to if (gridView) "grid" else "deck"),
        )

    /** The one-time 18+ notice was shown before entering the Intimacy category. */
    data object IntimacyGateShown : AnalyticsEvent(name = "intimacy_gate_shown")

    /** How that notice ended - the drop-off rate here is the point of tracking it. */
    class IntimacyGateResolved(
        accepted: Boolean,
    ) : AnalyticsEvent(
            name = "intimacy_gate_resolved",
            params = mapOf(PARAM_ACCEPTED to accepted.toString()),
        )

    /** A card in the Game tab became visible. Separate from [QuestionViewed]: different content set. */
    class GameQuestionViewed(
        languageCode: String,
    ) : AnalyticsEvent(
            name = "game_question_viewed",
            params = mapOf(PARAM_LANGUAGE to languageCode),
        )

    /** The bottom-bar tab the user switched to (`questions` / `game`). */
    class TabSelected(
        screen: AnalyticsScreen,
    ) : AnalyticsEvent(
            name = "tab_selected",
            params = mapOf(PARAM_TAB to screen.screenName),
        )

    class ThemeChanged(
        themeMode: ThemeMode,
    ) : AnalyticsEvent(
            name = "theme_changed",
            params = mapOf(PARAM_THEME to themeMode.analyticsValue()),
        )

    /** `null` [language] means "follow the OS/browser locale" - reported as `automatic`. */
    class LanguageChanged(
        language: AppLanguage?,
    ) : AnalyticsEvent(
            name = "language_changed",
            params = mapOf(PARAM_LANGUAGE to (language?.code ?: LANGUAGE_AUTOMATIC)),
        )

    class ParentsModeToggled(
        enabled: Boolean,
    ) : AnalyticsEvent(
            name = "parents_mode_toggled",
            params = mapOf(PARAM_ENABLED to enabled.toString()),
        )

    /**
     * "Reset Cards" completed. The hidden-card count is bucketed rather than exact: Firebase
     * treats every distinct string value as its own row, so raw counts would produce thousands of
     * one-user rows in the parameter report.
     */
    class CardsReset(
        hiddenCount: Int,
    ) : AnalyticsEvent(
            name = "cards_reset",
            params = mapOf(PARAM_HIDDEN_BUCKET to bucket(hiddenCount)),
        )

    /**
     * The analytics opt-out in Settings was flipped. Reported for both directions, but the
     * opt-*out* case only reaches the SDK because it is logged before collection is disabled -
     * see `SettingsViewModel.onAnalyticsEnabledToggled`.
     */
    class AnalyticsToggled(
        enabled: Boolean,
    ) : AnalyticsEvent(
            name = "analytics_toggled",
            params = mapOf(PARAM_ENABLED to enabled.toString()),
        )

    /** GitHub-flavor self-update check outcome - one of the `UPDATE_RESULT_*` constants below. */
    class UpdateCheckCompleted(
        result: String,
    ) : AnalyticsEvent(
            name = "update_check_completed",
            params = mapOf(PARAM_RESULT to result),
        )

    /** The user confirmed an update; the APK download/install handoff started. */
    data object UpdateInstallStarted : AnalyticsEvent(name = "update_install_started")

    /** Web only: the "Install the Android app" banner was acted on. */
    data object InstallBannerClicked : AnalyticsEvent(name = "install_banner_clicked")

    companion object {
        const val PARAM_CATEGORY = "category"
        const val PARAM_AUDIENCE = "audience"
        const val PARAM_LANGUAGE = "language"
        const val PARAM_MODE = "mode"
        const val PARAM_ACCEPTED = "accepted"
        const val PARAM_TAB = "tab"
        const val PARAM_THEME = "theme"
        const val PARAM_ENABLED = "enabled"
        const val PARAM_RESULT = "result"
        const val PARAM_HIDDEN_BUCKET = "hidden_bucket"

        /** Reported when no category filter is applied, so the parameter is never absent. */
        const val CATEGORY_ALL = "all"
        const val LANGUAGE_AUTOMATIC = "automatic"

        const val UPDATE_RESULT_UP_TO_DATE = "up_to_date"
        const val UPDATE_RESULT_AVAILABLE = "update_available"
        const val UPDATE_RESULT_FAILED = "failed"

        /**
         * Collapses a count into one of a handful of ranges. Keeps the parameter's cardinality
         * bounded (see [CardsReset]) while still separating "reset after a couple of cards" from
         * "reset after working through the whole deck".
         */
        internal fun bucket(count: Int): String =
            when {
                count <= 0 -> "0"
                count <= 10 -> "1-10"
                count <= 50 -> "11-50"
                count <= 200 -> "51-200"
                else -> "200+"
            }
    }
}

/**
 * Stable wire names for the domain types used as event parameters, mirroring
 * `core:data`'s `CardMapper` storage keys. Written out by hand rather than derived from
 * `Enum.name`/`this::class.simpleName` because R8 may rename either - see this file's kdoc.
 */
internal fun QuestionCategory?.analyticsValue(): String =
    when (this) {
        null -> AnalyticsEvent.CATEGORY_ALL
        QuestionCategory.IceBreakers -> "ice_breakers"
        QuestionCategory.Memories -> "memories"
        QuestionCategory.Values -> "values"
        QuestionCategory.FutureDreams -> "future_dreams"
        QuestionCategory.DailyLife -> "daily_life"
        QuestionCategory.Intimacy -> "intimacy"
    }

internal fun QuestionAudience.analyticsValue(): String =
    when (this) {
        QuestionAudience.WithoutKids -> "without_kids"
        QuestionAudience.WithKids -> "with_kids"
        QuestionAudience.Both -> "both"
    }

internal fun ThemeMode.analyticsValue(): String =
    when (this) {
        ThemeMode.Light -> "light"
        ThemeMode.Dark -> "dark"
        ThemeMode.System -> "system"
    }
