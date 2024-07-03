@file:Suppress("unused", "FunctionName", "PrivatePropertyName")

package com.github.yuriisurzhykov.kevent.activeobject.scopes

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

/**
 *  Creates and returns a coroutine dispatcher backed by a single-threaded executor.
 *  This is used to ensure that coroutines within an Active Object are executed sequentially
 *  on a dedicated thread.
 *  @return [CoroutineDispatcher] from Executor
 * */
private fun AoCoroutineDispatcher(): CoroutineDispatcher =
    Executors.newSingleThreadExecutor().asCoroutineDispatcher()

/**
 *  Provides a [CoroutineContext] for Active Objects using the dispatcher returned by [AoCoroutineDispatcher].
 *  @return A [CoroutineContext] that uses a single-threaded executor.
 */
fun AoCoroutineContext(): CoroutineContext = AoCoroutineDispatcher()
