package org.neteinstein.couples.data.local

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Kotlin/Wasm has no `Dispatchers.IO` - the browser runs one JS event loop, so `Default` is the
 * only dispatcher there is, and storage reads on this target are synchronous `localStorage` calls
 * anyway (see the Settings-backed DAOs in this source set).
 */
actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
