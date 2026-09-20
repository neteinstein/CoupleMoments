package org.neteinstein.couples.data.local

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * `Dispatchers.IO` is internal on Kotlin/Native in this coroutines version, so the closest public
 * equivalent is used instead: `Default` is a real multi-threaded pool there (unlike on Wasm), and
 * SQLDelight's native driver calls are short synchronous SQLite operations rather than the kind of
 * long blocking IO a dedicated pool exists to isolate.
 */
actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
