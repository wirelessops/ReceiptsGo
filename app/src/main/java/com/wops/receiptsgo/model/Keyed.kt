package com.wops.receiptsgo.model

import java.util.*

interface Keyed {
    /**
     * The primary key id for this item
     *
     * @return the items's autoincrement id
     */
    val id: Int

    /**
     * The UUID for this item
     *
     * @return the items's UUID
     */
    val uuid: UUID

    companion object {
        const val MISSING_ID: Int = -1

        val MISSING_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    }
}