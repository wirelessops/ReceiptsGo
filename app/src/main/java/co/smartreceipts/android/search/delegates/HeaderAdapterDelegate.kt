package co.smartreceipts.android.search.delegates

import co.smartreceipts.android.R
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateLayoutContainer
import kotlinx.android.synthetic.main.item_header.*

fun headerAdapterDelegate() = adapterDelegateLayoutContainer<HeaderItem, Any>(R.layout.item_header) {

    bind {
        card_header.text = item.header
    }
}

data class HeaderItem(val header: String)