package com.niceforyou.activeobject.communication

import com.niceforyou.activeobject.TestStickyEvent
import com.github.yuriisurzhykov.kevent.activeobject.communication.operation.OperateSticky
import com.github.yuriisurzhykov.kevent.events.Event
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import kotlin.reflect.KClass

class OperateStickyTest {

    @Test
    fun `check emit sticky store event in map by class`() = runTest {
        val useCase = OperateSticky.Base()

        val map = mutableMapOf<KClass<out Event>, Event>()
        val event = TestStickyEvent()
        useCase.emit(map, event)

        Assert.assertNotNull(map[TestStickyEvent::class])
        Assert.assertEquals(event, map[TestStickyEvent::class])
    }

    @Test
    fun `check replaces sticky event in map by class`() = runTest {
        val useCase = OperateSticky.Base()
        val map = mutableMapOf<KClass<out Event>, Event>()

        val event = TestStickyEvent()
        useCase.emit(map, event)
        Assert.assertNotNull(map[TestStickyEvent::class])
        Assert.assertEquals(event, map[TestStickyEvent::class])

        val replaceEvent = TestStickyEvent(12)
        useCase.emit(map, replaceEvent)
        Assert.assertEquals(replaceEvent, map[TestStickyEvent::class])
    }

    @Test
    fun `check deletes sticky from map by class`() = runTest {
        val useCase = OperateSticky.Base()
        val map = mutableMapOf<KClass<out Event>, Event>()

        map[TestStickyEvent::class] = TestStickyEvent()
        Assert.assertNotNull(map[TestStickyEvent::class])
        useCase.delete(map, TestStickyEvent::class)

        Assert.assertNull(map[TestStickyEvent::class])
    }

    @Test
    fun `check returns sticky event if exists`() = runTest {
        val useCase = OperateSticky.Base()
        val map = mutableMapOf<KClass<out Event>, Event>()

        val expected = TestStickyEvent()
        map[TestStickyEvent::class] = expected
        val actual = useCase.read(map, TestStickyEvent::class)

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `check returns null if sticky event not exist`() = runTest {
        val useCase = OperateSticky.Base()
        val map = mutableMapOf<KClass<out Event>, Event>()

        val actual = useCase.read(map, TestStickyEvent::class)
        Assert.assertNull(actual)
    }
}