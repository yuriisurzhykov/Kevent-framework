package com.github.yuriisurzhykov.kevent.events.api

import com.github.yuriisurzhykov.kevent.events.api.sticky.StickyApi
import com.github.yuriisurzhykov.kevent.events.api.stickycollection.StickyCollectionApi

/**
 *  `EventManager` combines all [PublishEvent], [StickyApi] and [StickyCollectionApi]
 *  interface and provides API to get access to methods declared in these interfaces.
 *  This `EventManager` is an immutable entry point to work with different sticky events.
 * */
interface EventManager : PublishEvent, StickyApi, StickyCollectionApi