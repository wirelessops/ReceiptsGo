package co.smartreceipts.android.model

import androidx.annotation.StringRes

interface ActualColumnDefinition {

    val columnType: Int

    @get:StringRes
    val columnHeaderId: Int
}
