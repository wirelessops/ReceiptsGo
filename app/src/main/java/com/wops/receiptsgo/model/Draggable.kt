package com.wops.receiptsgo.model

interface Draggable<T> : Comparable<T> {

    /**
     * Custom order id from the database
     */
    val customOrderId: Long
}
