package co.smartreceipts.android.search.delegates

import co.smartreceipts.android.databinding.ItemRoundHeaderBinding
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding

fun headerAdapterDelegate() = adapterDelegateViewBinding<HeaderItem, Any, ItemRoundHeaderBinding>(
    { layoutInflater, root -> ItemRoundHeaderBinding.inflate(layoutInflater, root, false) }
) {

    bind {
        binding.header.text = item.header
    }
}

data class HeaderItem(val header: String)