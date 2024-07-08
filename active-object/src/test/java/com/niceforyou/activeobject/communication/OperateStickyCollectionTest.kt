package com.niceforyou.activeobject.communication

import com.niceforyou.activeobject.TestStickyCollection
import com.github.yuriisurzhykov.kevent.eventbus.communication.operation.OperateStickyCollection
import com.github.yuriisurzhykov.kevent.events.Event
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.reflect.KClass

class OperateStickyCollectionTest {

    @Test
    fun `check emit sticky collection event in map by class and key`() = runTest {
        val useCase = OperateStickyCollection.Collection()
        val map =
            HashMap<KClass<out Event>, HashMap<Any, Event>>()
        val nestedMap = HashMap<Any, Event>()
        map[TestStickyCollection::class] = nestedMap

        val event = TestStickyCollection(1)
        useCase.emit(map, event)

        assertNotNull(map[TestStickyCollection::class])

        // 1 is not an index, it's an integer key
        assertEquals(event, nestedMap[1])
    }

    @Test
    fun `check replaces sticky collection if exists in map`() = runTest {
        val useCase = OperateStickyCollection.Collection()
        val map =
            HashMap<KClass<out Event>, HashMap<Any, Event>>()
        val nestedMap = HashMap<Any, Event>()
        map[TestStickyCollection::class] = nestedMap

        val event = TestStickyCollection(1)
        useCase.emit(map, event)
        val replaceEvent = TestStickyCollection(2)
        useCase.emit(map, replaceEvent)

        // 2 is not an index, it's an integer key
        assertEquals(replaceEvent, nestedMap[2])
    }

    @Test
    fun `check deletes sticky collection if exists in map`() = runTest {
        val useCase = OperateStickyCollection.Collection()
        val map =
            HashMap<KClass<out Event>, HashMap<Any, Event>>()
        val nestedMap = HashMap<Any, Event>()
        map[TestStickyCollection::class] = nestedMap

        nestedMap[1] = TestStickyCollection(1)
        useCase.delete(map, TestStickyCollection::class, 1)

        // 1 is not an index, it's an integer key
        assertNull(nestedMap[1])
    }

    @Test
    fun `check don't delete sticky collection if key not matches`() = runTest {
        val useCase = OperateStickyCollection.Collection()
        val map =
            HashMap<KClass<out Event>, HashMap<Any, Event>>()
        val nestedMap = HashMap<Any, Event>()
        map[TestStickyCollection::class] = nestedMap

        nestedMap[1] = TestStickyCollection(1)
        useCase.delete(map, TestStickyCollection::class, 10)

        // 1 is not an index, it's an integer key
        assertNotNull(nestedMap[1])
    }

    @Test
    fun `check returns sticky collection event if exists in map`() = runTest {
        val useCase = OperateStickyCollection.Collection()
        val map =
            HashMap<KClass<out Event>, HashMap<Any, Event>>()
        val nestedMap = HashMap<Any, Event>()
        map[TestStickyCollection::class] = nestedMap

        val expected = TestStickyCollection(1)
        nestedMap[1] = expected
        val actual = useCase.read(map, TestStickyCollection::class, 1)

        assertEquals(expected, actual)
    }

    @Test
    fun `check returns null if submap exists but key not found`() = runTest {
        val useCase = OperateStickyCollection.Collection()
        val map =
            HashMap<KClass<out Event>, HashMap<Any, Event>>()
        val nestedMap = HashMap<Any, Event>()
        map[TestStickyCollection::class] = nestedMap

        val actual = useCase.read(map, TestStickyCollection::class, 1)

        assertNull(actual)
    }

    @Test
    fun `check returns null if submap not exists`() = runTest {
        val useCase = OperateStickyCollection.Collection()
        val map =
            HashMap<KClass<out Event>, HashMap<Any, Event>>()

        val actual = useCase.read(map, TestStickyCollection::class, 1)

        assertNull(actual)
    }
}