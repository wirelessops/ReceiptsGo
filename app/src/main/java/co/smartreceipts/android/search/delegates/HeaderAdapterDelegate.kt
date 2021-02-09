package co.smartreceipts.android.search.delegates

import co.smartreceipts.android.databinding.ItemHeaderBinding
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding

fun headerAdapterDelegate() = adapterDelegateViewBinding<HeaderItem, Any, ItemHeaderBinding>(
    { layoutInflater, root -> ItemHeaderBinding.inflate(layoutInflater, root, false) }
) {

    bind {
        binding.header.text = item.header
    }
}

data class HeaderItem(val header: String)