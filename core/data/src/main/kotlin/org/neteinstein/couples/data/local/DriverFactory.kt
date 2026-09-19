package org.neteinstein.couples.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Creates the [SqlDriver] backing [CoupleMomentsDatabase] (SQLDelight's generated database class
 * - see `core/data/build.gradle.kts`'s `sqldelight { databases { ... } }` block).
 *
 * Plain Android class rather than an `expect`/`actual` pair, matching this module's other KMP
 * migration groundwork (`core:data` itself is still `com.android.library`, not yet KMP) - this
 * becomes the `androidMain actual` once the module converts and iOS/wasmJs targets each need
 * their own driver.
 *
 * [DB_NAME] is deliberately NOT the pre-existing Room database's file name ("couple_moments.db",
 * see git history) - opening the same SQLite file with a different persistence library risks a
 * schema/`user_version`-tracking mismatch this environment has no way to verify against a real
 * device. Using a new file name means an upgrading install simply starts with no `cards`/
 * `seed_metadata` rows, which [org.neteinstein.couples.data.repository.QuestionRepositoryImpl]
 * already treats as an ordinary first-launch seed (see its `ensureSeeded()`) - the only
 * user-visible effect is previously-hidden/answered cards becoming visible again once, which is
 * far safer than a corrupted or unreadable database. Revisit if/when this migration is validated
 * against a real upgrading device.
 */
class DriverFactory(
    private val context: Context,
) {
    fun createDriver(): SqlDriver = AndroidSqliteDriver(CoupleMomentsDatabase.Schema, context, DB_NAME)

    private companion object {
        const val DB_NAME = "couple_moments_sqldelight.db"
    }
}
