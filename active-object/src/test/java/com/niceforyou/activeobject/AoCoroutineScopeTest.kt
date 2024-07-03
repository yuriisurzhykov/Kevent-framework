package com.niceforyou.activeobject

import com.github.yuriisurzhykov.kevent.activeobject.scopes.AoCoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.lang.Thread.sleep

@Suppress("BlockingMethodInNonBlockingContext")
class AoCoroutineScopeTest {

    @Test
    fun `AoCoroutineContext creates a new CoroutineContext`() = runBlocking {
        val context = AoCoroutineContext()
        assertNotNull(context)
    }

    @Test
    fun `CoroutineScope is executed on a single separate thread`() = runBlocking {
        val mainThreadId = Thread.currentThread().id
        val scope = CoroutineScope(AoCoroutineContext())

        var coroutineThreadId: Long = -1

        scope.launch {
            coroutineThreadId = Thread.currentThread().id
        }

        // Lets coroutine to finish execution
        sleep(100)

        assertNotEquals(mainThreadId, coroutineThreadId)
    }
}