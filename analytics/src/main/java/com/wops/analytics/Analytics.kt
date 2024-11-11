package com.wops.analytics

import com.wops.analytics.events.Event

/**
 * A default contract which can be used for logging events
 */
interface Analytics {

    /**
     * Records a specific event
     *
     * @param event the [Event] to record
     */
    fun record(event: Event)
}
