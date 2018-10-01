package co.smartreceipts.android.model

interface Draggable<T> : Comparable<T> {

    /**
     * Custom order id from the database
     */
    val customOrderId: Long
}
