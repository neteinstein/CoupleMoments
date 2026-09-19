package org.neteinstein.couples.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [SeedMetadataDao] backed by SQLDelight's generated `SeedMetadataQueries` (from
 * `SeedMetadata.sq`), replacing the previous Room `@Dao` - see [CardDaoImpl]'s doc comment for the
 * dispatcher-wrapping rationale, which applies here identically.
 */
class SeedMetadataDaoImpl(
    private val queries: SeedMetadataQueries,
) : SeedMetadataDao {
    override suspend fun getVersion(): Int? =
        withContext(Dispatchers.IO) {
            queries.selectVersion().executeAsOneOrNull()?.toInt()
        }

    override suspend fun setVersion(metadata: SeedMetadataEntity) =
        withContext(Dispatchers.IO) {
            queries.setVersion(id = metadata.id.toLong(), version = metadata.version.toLong())
        }
}
