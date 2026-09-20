package org.neteinstein.couples.data.local

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
