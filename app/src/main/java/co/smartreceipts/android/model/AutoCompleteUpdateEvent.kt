package co.smartreceipts.android.model

import co.smartreceipts.android.autocomplete.AutoCompleteField
import co.smartreceipts.android.autocomplete.AutoCompleteResult

data class AutoCompleteUpdateEvent<Type>(
    val item: AutoCompleteResult<Type>,
    val type: AutoCompleteField,
    val position: Int
)
