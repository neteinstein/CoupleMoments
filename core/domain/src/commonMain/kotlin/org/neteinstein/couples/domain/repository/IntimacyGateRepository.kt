package org.neteinstein.couples.domain.repository

/**
 * Whether the user has already passed the one-time 18+ content notice shown before the Intimacy
 * category (romantic/sexual conversation starters) is first opened. Persisted so the notice
 * doesn't reappear on every app launch once acknowledged.
 */
interface IntimacyGateRepository {
    suspend fun hasAcknowledged(): Boolean

    suspend fun setAcknowledged()
}
