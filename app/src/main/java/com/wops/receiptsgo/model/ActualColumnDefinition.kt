package com.wops.receiptsgo.model

import androidx.annotation.StringRes

interface ActualColumnDefinition {

    val columnType: Int

    @get:StringRes
    val columnHeaderId: Int
}
