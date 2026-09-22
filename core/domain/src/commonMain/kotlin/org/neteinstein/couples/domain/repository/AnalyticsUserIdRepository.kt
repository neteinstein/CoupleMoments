package org.neteinstein.couples.domain.repository

/**
 * Supplies the id reported as Firebase's `user_id`, which is what turns per-event rows into
 * per-*user* metrics (active users, retention cohorts, "cards viewed per user").
 *
 * The id is generated randomly on first use and stored locally, so it identifies an *install*,
 * not a person: it is not an account id, not an advertising id, and not derived from any device
 * identifier, so it cannot be correlated with anything outside this app. Clearing app data or
 * reinstalling produces a new one, which is the intended trade-off - see PRIVACY.md.
 */
interface AnalyticsUserIdRepository {
    /** Returns the stored id, generating and persisting one on first call. */
    suspend fun getOrCreate(): String
}
