package co.smartreceipts.android.search.delegates

import co.smartreceipts.android.R
import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.model.Trip
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateLayoutContainer
import kotlinx.android.synthetic.main.simple_card.*


fun tripAdapterDelegate(itemClickedListener: (Trip) -> Unit, dateFormatter: DateFormatter) =
    adapterDelegateLayoutContainer<Trip, Any>(R.layout.simple_card) {

        itemView.setOnClickListener { itemClickedListener(item) }

        bind {
            price.text = item.price.currencyFormattedPrice
            title.text = item.name

            val start = dateFormatter.getFormattedDate(item.startDisplayableDate)
            val end = dateFormatter.getFormattedDate(item.endDisplayableDate)
            date.text = itemView.context.getString(R.string.trip_adapter_list_item_to, start, end)
        }

    }
