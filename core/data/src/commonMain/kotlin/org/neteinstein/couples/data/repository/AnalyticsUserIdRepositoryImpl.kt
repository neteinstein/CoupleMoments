package org.neteinstein.couples.data.repository

import com.russhwolf.settings.Settings
import kotlinx.coroutines.withContext
import org.neteinstein.couples.data.local.ioDispatcher
import org.neteinstein.couples.domain.repository.AnalyticsUserIdRepository
import kotlin.random.Random

/**
 * Generates a random 128-bit id on first call and keeps it in [settings] for the life of the
 * install.
 *
 * Built from [Random.Default] rather than from anything the device already knows about itself -
 * no ANDROID_ID, no advertising id, no `identifierForVendor` - so it is a label for this install's
 * *event stream* and nothing else. [Random.Default] is seeded from the platform's own entropy on
 * every target here, and a collision across installs would need a 2^64-install birthday bound, so
 * no stronger source is warranted for what this identifies.
 *
 * Deliberately not `kotlin.uuid.Uuid`: that API still carries an opt-in marker on the Kotlin
 * version this project pins, and the format is not load-bearing - Firebase treats `user_id` as an
 * opaque string.
 */
class AnalyticsUserIdRepositoryImpl(
    private val settings: Settings,
) : AnalyticsUserIdRepository {
    override suspend fun getOrCreate(): String =
        withContext(ioDispatcher) {
            settings.getStringOrNull(KEY_USER_ID)
                ?: randomId().also { settings.putString(KEY_USER_ID, it) }
        }

    private fun randomId(): String =
        buildString(ID_HEX_LENGTH) {
            repeat(ID_HEX_LENGTH) { append(HEX_DIGITS[Random.nextInt(HEX_DIGITS.length)]) }
        }

    private companion object {
        const val KEY_USER_ID = "analytics_user_id"

        /** 32 hex characters = 128 bits, the same width a UUID carries. */
        const val ID_HEX_LENGTH = 32
        const val HEX_DIGITS = "0123456789abcdef"
    }
}
