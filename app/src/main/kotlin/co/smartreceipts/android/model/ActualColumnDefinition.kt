package co.smartreceipts.android.model

import android.support.annotation.StringRes

interface ActualColumnDefinition {

    val columnType: Int

    @get:StringRes
    val columnHeaderId: Int
}
