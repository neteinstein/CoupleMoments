package org.neteinstein.couples.data.local

/**
 * Persistence-framework-agnostic contract for [SeedMetadataEntity] storage - see [CardDao]'s doc
 * comment for why this stays a separate interface from its SQLDelight-backed implementation,
 * [SeedMetadataDaoImpl].
 */
interface SeedMetadataDao {
    suspend fun getVersion(): Int?

    suspend fun setVersion(metadata: SeedMetadataEntity)
}
