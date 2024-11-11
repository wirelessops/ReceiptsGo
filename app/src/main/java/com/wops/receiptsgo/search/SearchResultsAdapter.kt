package com.wops.receiptsgo.search

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wops.receiptsgo.date.DateFormatter
import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.model.Trip
import com.wops.receiptsgo.search.viewholders.HeaderItem
import com.wops.receiptsgo.search.viewholders.HeaderViewHolder
import com.wops.receiptsgo.search.viewholders.ReceiptViewHolder
import com.wops.receiptsgo.search.viewholders.TripViewHolder
import com.wops.core.sync.provider.SyncProvider
import com.squareup.picasso.Transformation

class SearchResultsAdapter(
    private val transformation: Transformation,
    private val dateFormatter: DateFormatter,
    private val syncProvider: SyncProvider,
    private val tripClickListener: (Trip) -> Unit,
    private val receiptClickListener: (Receipt) -> Unit,
) : ListAdapter<Any, RecyclerView.ViewHolder>(SearchResultsDiffUtilCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HeaderItem -> TYPE_HEADER
            is Receipt -> TYPE_RECEIPT
            else -> TYPE_TRIP
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder.from(parent)
            TYPE_RECEIPT -> ReceiptViewHolder.from(parent)
            else -> TripViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        when (holder) {
            is HeaderViewHolder -> {
                holder.bind(item as HeaderItem)
            }
            is ReceiptViewHolder -> {
                holder.bind(item as Receipt, syncProvider, transformation, receiptClickListener)
            }
            is TripViewHolder -> {
                holder.bind(item as Trip, tripClickListener, {}, dateFormatter)
            }
        }
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_RECEIPT = 1
        private const val TYPE_TRIP = 2
    }
}