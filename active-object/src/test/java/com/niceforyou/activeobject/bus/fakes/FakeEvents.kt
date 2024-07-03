package com.niceforyou.activeobject.bus.fakes

import com.github.yuriisurzhykov.kevent.events.Event
import com.github.yuriisurzhykov.kevent.events.EventNotValidException
import com.github.yuriisurzhykov.kevent.events.api.EventManager

class FakeValidatableEvent : Event.Sticky() {
    var exception: EventNotValidException? = null
    override suspend fun validate(eventManager: EventManager): EventNotValidException? = exception
}

internal class FakePersistableSticky : Event.Sticky(), Event.Persistable
internal class FakeStickyCollection(
    override val key: String
) : Event.StickyCollection<String>()