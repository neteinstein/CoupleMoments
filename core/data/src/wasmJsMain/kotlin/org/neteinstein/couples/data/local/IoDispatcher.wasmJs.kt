package org.neteinstein.couples.data.local

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Kotlin/Wasm has no `Dispatchers.IO` - the browser runs one JS event loop, so `Default` is the
 * only dispatcher there is. Storage on this target never blocks it anyway: IndexedDB calls are
 * asynchronous (awaited Promises, see IndexedDb.kt) and `localStorage` reads are tiny.
 */
actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
