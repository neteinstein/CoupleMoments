package org.neteinstein.couples.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

/**
 * SQLDelight's Kotlin/Native driver, backed by the same generated [CoupleMomentsDatabase.Schema]
 * Android uses - so iOS gets genuine on-device persistence, not an in-memory stand-in.
 *
 * Uses the same [DB_NAME] as the Android driver: these are separate per-platform sandboxes, so
 * there is no file to collide with, and keeping the name identical makes the two easy to compare
 * when debugging.
 */
class DriverFactory {
    fun createDriver(): SqlDriver = NativeSqliteDriver(CoupleMomentsDatabase.Schema, DB_NAME)

    private companion object {
        const val DB_NAME = "couple_moments_sqldelight.db"
    }
}
