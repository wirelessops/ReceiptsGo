package com.wops.receiptsgo.trips.navigation

import com.wops.receiptsgo.model.Trip

/**
 * Defines how we can route to view the receipts contained in a specific trip
 */
interface ViewReceiptsInTripRouter {

    fun routeToViewReceipts(trip: Trip)

}