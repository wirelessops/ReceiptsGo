package co.smartreceipts.android.search.delegates

import co.smartreceipts.android.R
import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.model.Trip
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateLayoutContainer
import kotlinx.android.synthetic.main.item_trip_or_distance_card.*

fun tripAdapterDelegate(itemClickedListener: (Trip) -> Unit, dateFormatter: DateFormatter) =
    adapterDelegateLayoutContainer<Trip, Any>(R.layout.item_trip_or_distance_card) {

        itemView.setOnClickListener { itemClickedListener(item) }

        bind {
            text_price.text = item.price.currencyFormattedPrice
            text_name.text = item.name

            val start = dateFormatter.getFormattedDate(item.startDisplayableDate)
            val end = dateFormatter.getFormattedDate(item.endDisplayableDate)
            text_details.text = itemView.context.getString(R.string.trip_adapter_list_item_to, start, end)
        }

    }
