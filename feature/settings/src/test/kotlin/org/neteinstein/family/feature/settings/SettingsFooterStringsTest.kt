package org.neteinstein.family.feature.settings

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The LoopGain footer turns the two names injected into `settings_loopgain_footer` into links by
 * locating them in the formatted string (see `SettingsScreen`'s `addUrlLink`). A translation that
 * drops a placeholder therefore renders as plain text with no link at all - silently, and only in
 * that one locale. These tests guard the placeholders across every locale we ship.
 */
class SettingsFooterStringsTest {

    private val resDir = listOf("src/main/res", "feature/settings/src/main/res")
        .map(::File)
        .first { it.isDirectory }

    private val footerRegex = Regex("""<string name="settings_loopgain_footer">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)

    @Test
    fun `every locale declares the loopgain footer`() {
        val localesWithoutFooter = localeDirs().filter { footerIn(it) == null }.map { it.name }

        assertTrue("Missing settings_loopgain_footer in: $localesWithoutFooter", localesWithoutFooter.isEmpty())
    }

    @Test
    fun `every loopgain footer keeps both link placeholders`() {
        val broken = localeDirs().filter { dir ->
            val footer = footerIn(dir) ?: return@filter true
            !footer.contains("%1\$s") || !footer.contains("%2\$s")
        }.map { it.name }

        assertTrue("Footer is missing %1\$s and/or %2\$s in: $broken", broken.isEmpty())
    }

    private fun localeDirs(): List<File> =
        resDir.listFiles { file -> file.isDirectory && file.name.startsWith("values") }
            .orEmpty()
            .filter { File(it, "strings.xml").isFile }
            .sortedBy { it.name }

    private fun footerIn(localeDir: File): String? =
        footerRegex.find(File(localeDir, "strings.xml").readText())?.groupValues?.get(1)
}
