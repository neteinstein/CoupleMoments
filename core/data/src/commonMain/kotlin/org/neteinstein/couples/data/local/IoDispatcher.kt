package org.neteinstein.couples.data.local

import kotlinx.coroutines.CoroutineDispatcher

/**
 * The dispatcher every blocking storage call is moved onto - SQLDelight's driver calls are
 * synchronous, the same way Room's suspend DAO methods dispatched internally.
 *
 * `Dispatchers.IO` exists on Android/JVM and Kotlin/Native but not on Kotlin/Wasm, where the
 * single-threaded event loop means there is no separate IO pool to move to in the first place -
 * hence expect/actual rather than a direct reference from commonMain.
 */
expect val ioDispatcher: CoroutineDispatcher
