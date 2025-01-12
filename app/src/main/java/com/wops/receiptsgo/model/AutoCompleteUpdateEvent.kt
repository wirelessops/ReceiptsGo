package com.wops.receiptsgo.model

import com.wops.receiptsgo.autocomplete.AutoCompleteField
import com.wops.receiptsgo.autocomplete.AutoCompleteResult

data class AutoCompleteUpdateEvent<Type>(
    val item: AutoCompleteResult<Type>?,
    val type: AutoCompleteField,
    val position: Int
)
