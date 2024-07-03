package com.github.yuriisurzhykov.kevent.events.api.sticky

/**
 * The `StickyApi` interface represents an API for interacting with sticky events in the event system.
 * It combines the functionalities of the `DeleteSticky`, `ReadSticky`, and `PublishEvent` interfaces.
 */
interface StickyApi : DeleteSticky, ReadSticky