package com.github.yuriisurzhykov.kevent.events.api.stickycollection

/**
 * The StickyCollectionApi interface represents an API for interacting with sticky event collections in the event system.
 * It combines the functionalities of the DeleteStickyByKey, ReceiveStickyByKey, and ReceiveStickyCollection interfaces.
 */
interface StickyCollectionApi :
    DeleteStickyCollection,
    ReadStickyCollection,
    ReceiveCollectionOfSticky