package org.neteinstein.family.data.locale

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class LocaleProviderImplTest {
    private val originalDefaultLocale: Locale = Locale.getDefault()
    private val provider = LocaleProviderImpl()

    @After
    fun tearDown() {
        Locale.setDefault(originalDefaultLocale)
    }

    @Test
    fun `currentLanguageCode returns the process default locale's language`() {
        Locale.setDefault(Locale.forLanguageTag("pt"))

        assertEquals("pt", provider.currentLanguageCode())
    }

    @Test
    fun `currentLanguageCode reflects a change to the default locale`() {
        Locale.setDefault(Locale.forLanguageTag("en"))
        assertEquals("en", provider.currentLanguageCode())

        Locale.setDefault(Locale.forLanguageTag("es"))
        assertEquals("es", provider.currentLanguageCode())
    }

    @Test
    fun `currentLanguageCode strips region subtags down to the bare language code`() {
        Locale.setDefault(Locale.forLanguageTag("pt-BR"))

        assertEquals("pt", provider.currentLanguageCode())
    }
}
