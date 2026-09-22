package org.neteinstein.couples.domain.analytics

import org.neteinstein.couples.domain.model.AppLanguage
import org.neteinstein.couples.domain.model.QuestionAudience
import org.neteinstein.couples.domain.model.QuestionCategory
import org.neteinstein.couples.domain.model.ThemeMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * These assertions are about the *wire format*, not about behaviour: Firebase can never merge two
 * differently-named rows after the fact, and it silently drops anything over its length limits. So
 * the event names, the parameter values and their lengths are pinned here - a rename that looks
 * harmless in Kotlin fails this test instead of quietly splitting a metric in the console.
 */
class AnalyticsEventTest {
    @Test
    fun `category values are the stable storage-style keys, not enum or class names`() {
        assertEquals(
            "ice_breakers",
            AnalyticsEvent.CategorySelected(QuestionCategory.IceBreakers).params[AnalyticsEvent.PARAM_CATEGORY],
        )
        assertEquals(
            "future_dreams",
            AnalyticsEvent.CategorySelected(QuestionCategory.FutureDreams).params[AnalyticsEvent.PARAM_CATEGORY],
        )
    }

    @Test
    fun `no category filter is reported as a value rather than an absent parameter`() {
        val params = AnalyticsEvent.CategorySelected(null).params

        assertEquals(AnalyticsEvent.CATEGORY_ALL, params[AnalyticsEvent.PARAM_CATEGORY])
    }

    @Test
    fun `following the OS language is reported as automatic rather than an absent parameter`() {
        assertEquals(
            AnalyticsEvent.LANGUAGE_AUTOMATIC,
            AnalyticsEvent.LanguageChanged(null).params[AnalyticsEvent.PARAM_LANGUAGE],
        )
        assertEquals("pt", AnalyticsEvent.LanguageChanged(AppLanguage.PORTUGUESE).params[AnalyticsEvent.PARAM_LANGUAGE])
    }

    @Test
    fun `question_viewed carries the three dimensions it is meant to be sliced by`() {
        val params =
            AnalyticsEvent
                .QuestionViewed(
                    category = QuestionCategory.Intimacy,
                    audience = QuestionAudience.WithKids,
                    languageCode = "de",
                ).params

        assertEquals("intimacy", params[AnalyticsEvent.PARAM_CATEGORY])
        assertEquals("with_kids", params[AnalyticsEvent.PARAM_AUDIENCE])
        assertEquals("de", params[AnalyticsEvent.PARAM_LANGUAGE])
    }

    @Test
    fun `theme values are lowercase and stable`() {
        assertEquals("system", AnalyticsEvent.ThemeChanged(ThemeMode.System).params[AnalyticsEvent.PARAM_THEME])
    }

    /** Raw counts would give every user their own row in the parameter report - see CardsReset. */
    @Test
    fun `reset counts are bucketed instead of reported exactly`() {
        assertEquals("0", AnalyticsEvent.bucket(0))
        assertEquals("1-10", AnalyticsEvent.bucket(1))
        assertEquals("1-10", AnalyticsEvent.bucket(10))
        assertEquals("11-50", AnalyticsEvent.bucket(11))
        assertEquals("51-200", AnalyticsEvent.bucket(200))
        assertEquals("200+", AnalyticsEvent.bucket(201))
        assertEquals("0", AnalyticsEvent.bucket(-1))
    }

    @Test
    fun `every event name and parameter stays inside Firebase's limits`() {
        val allEvents =
            listOf(
                AnalyticsEvent.QuestionViewed(QuestionCategory.DailyLife, QuestionAudience.Both, "en"),
                AnalyticsEvent.QuestionHidden(QuestionCategory.Memories),
                AnalyticsEvent.CategorySelected(QuestionCategory.Values),
                AnalyticsEvent.ViewModeChanged(gridView = true),
                AnalyticsEvent.IntimacyGateShown,
                AnalyticsEvent.IntimacyGateResolved(accepted = true),
                AnalyticsEvent.GameQuestionViewed("fr"),
                AnalyticsEvent.TabSelected(AnalyticsScreen.Game),
                AnalyticsEvent.ThemeChanged(ThemeMode.Dark),
                AnalyticsEvent.LanguageChanged(AppLanguage.SPANISH),
                AnalyticsEvent.ParentsModeToggled(enabled = true),
                AnalyticsEvent.CardsReset(42),
                AnalyticsEvent.AnalyticsToggled(enabled = false),
                AnalyticsEvent.UpdateCheckCompleted(AnalyticsEvent.UPDATE_RESULT_FAILED),
                AnalyticsEvent.UpdateInstallStarted,
                AnalyticsEvent.InstallBannerClicked,
            )

        allEvents.forEach { event ->
            assertTrue(event.name.length <= MAX_EVENT_NAME_LENGTH, "event name too long: ${event.name}")
            assertTrue(event.name.matches(NAME_PATTERN), "event name not Firebase-legal: ${event.name}")
            RESERVED_PREFIXES.forEach { prefix ->
                assertTrue(!event.name.startsWith(prefix), "event name uses a reserved prefix: ${event.name}")
            }
            assertTrue(event.params.size <= MAX_PARAMS, "too many parameters on ${event.name}")
            event.params.forEach { (key, value) ->
                assertTrue(key.length <= MAX_PARAM_NAME_LENGTH, "parameter name too long: $key")
                assertTrue(key.matches(NAME_PATTERN), "parameter name not Firebase-legal: $key")
                assertTrue(value.length <= MAX_PARAM_VALUE_LENGTH, "parameter value too long: $key=$value")
            }
        }
    }

    @Test
    fun `every user property name stays inside Firebase's limits`() {
        AnalyticsUserProperty.entries.forEach { property ->
            assertTrue(property.key.length <= MAX_USER_PROPERTY_NAME_LENGTH, "too long: ${property.key}")
            assertTrue(property.key.matches(NAME_PATTERN), "not Firebase-legal: ${property.key}")
            RESERVED_PREFIXES.forEach { prefix ->
                assertTrue(!property.key.startsWith(prefix), "uses a reserved prefix: ${property.key}")
            }
        }
    }

    @Test
    fun `screen names are distinct so the console does not merge two screens into one row`() {
        val names = AnalyticsScreen.entries.map { it.screenName }

        assertEquals(names.size, names.toSet().size)
    }

    private companion object {
        const val MAX_EVENT_NAME_LENGTH = 40
        const val MAX_PARAM_NAME_LENGTH = 40
        const val MAX_PARAM_VALUE_LENGTH = 100
        const val MAX_USER_PROPERTY_NAME_LENGTH = 24
        const val MAX_PARAMS = 25

        val NAME_PATTERN = Regex("^[A-Za-z][A-Za-z0-9_]*$")
        val RESERVED_PREFIXES = listOf("firebase_", "google_", "ga_")
    }
}
