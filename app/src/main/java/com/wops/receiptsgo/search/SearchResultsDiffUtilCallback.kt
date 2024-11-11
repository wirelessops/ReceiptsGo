package com.wops.receiptsgo.search

import androidx.recyclerview.widget.DiffUtil
import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.model.Trip
import com.wops.receiptsgo.search.viewholders.HeaderItem

class SearchResultsDiffUtilCallback : DiffUtil.ItemCallback<Any>() {

    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        if (oldItem::class != newItem::class) return false

        return when (oldItem) {
            is HeaderItem -> oldItem.header == (newItem as HeaderItem).header
            is Receipt -> oldItem.id == (newItem as Receipt).id
            is Trip -> oldItem.id == (newItem as Trip).id
            else -> throw IllegalStateException("Search results must contain only Headers, Receipts and Trips")
        }
    }

    // no need to compare content of the searched items because it's not supposed to be changed during searching
    override fun areContentsTheSame(oldItem: Any, newItem: Any) = true
}
