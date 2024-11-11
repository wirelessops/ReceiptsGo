package com.wops.receiptsgo.search.delegates

import com.wops.receiptsgo.adapters.DistanceListItem
import com.wops.receiptsgo.databinding.ItemHeaderDoubleBinding
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding

fun doubleHeaderAdapterDelegate() = adapterDelegateViewBinding<DoubleHeaderItem, Any, ItemHeaderDoubleBinding>(
    { layoutInflater, root -> ItemHeaderDoubleBinding.inflate(layoutInflater, root, false) }
) {

    bind {
        binding.textLeft.text = item.headerLeft
        binding.textRight.text = item.headerRight
    }
}

data class DoubleHeaderItem(val headerLeft: String, val headerRight: String) : DistanceListItem