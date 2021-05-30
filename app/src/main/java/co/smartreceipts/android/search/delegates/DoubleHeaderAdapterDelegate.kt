package co.smartreceipts.android.search.delegates

import co.smartreceipts.android.adapters.DistanceListItem
import co.smartreceipts.android.databinding.ItemHeaderDoubleBinding
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