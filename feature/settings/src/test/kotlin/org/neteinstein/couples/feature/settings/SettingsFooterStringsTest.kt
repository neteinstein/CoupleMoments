package org.neteinstein.couples.feature.settings

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The LoopGain footer and the "About" app title both turn a name injected into their format
 * string into a link by locating it in the formatted string (see `SettingsScreen`'s
 * `addUrlLink`). A translation that drops a placeholder therefore renders as plain text with no
 * link at all - silently, and only in that one locale. These tests guard the placeholders across
 * every locale we ship.
 */
class SettingsFooterStringsTest {
    private val resDir =
        listOf("src/main/res", "feature/settings/src/main/res")
            .map(::File)
            .first { it.isDirectory }

    @Test
    fun `every locale declares the loopgain footer`() {
        val localesWithoutFooter = localeDirs().filter { stringIn(it, "settings_loopgain_footer") == null }.map { it.name }

        assertTrue("Missing settings_loopgain_footer in: $localesWithoutFooter", localesWithoutFooter.isEmpty())
    }

    @Test
    fun `every loopgain footer keeps both link placeholders`() {
        val broken = brokenLocales("settings_loopgain_footer", "%1\$s", "%2\$s")

        assertTrue("Footer is missing %1\$s and/or %2\$s in: $broken", broken.isEmpty())
    }

    @Test
    fun `every locale declares the about title format`() {
        val localesWithoutTitle = localeDirs().filter { stringIn(it, "settings_app_title_format") == null }.map { it.name }

        assertTrue("Missing settings_app_title_format in: $localesWithoutTitle", localesWithoutTitle.isEmpty())
    }

    @Test
    fun `every about title format keeps both link placeholders`() {
        val broken = brokenLocales("settings_app_title_format", "%1\$s", "%2\$s")

        assertTrue("About title is missing %1\$s and/or %2\$s in: $broken", broken.isEmpty())
    }

    private fun brokenLocales(
        stringName: String,
        vararg requiredPlaceholders: String,
    ): List<String> =
        localeDirs()
            .filter { dir ->
                val value = stringIn(dir, stringName) ?: return@filter true
                requiredPlaceholders.any { it !in value }
            }.map { it.name }

    private fun localeDirs(): List<File> =
        resDir
            .listFiles { file -> file.isDirectory && file.name.startsWith("values") }
            .orEmpty()
            .filter { File(it, "strings.xml").isFile }
            .sortedBy { it.name }

    private fun stringIn(
        localeDir: File,
        stringName: String,
    ): String? =
        Regex("""<string name="$stringName">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
            .find(File(localeDir, "strings.xml").readText())
            ?.groupValues
            ?.get(1)
}
