package co.smartreceipts.android.search.delegates

import co.smartreceipts.android.R
import co.smartreceipts.android.databinding.ItemTripOrDistanceCardBinding
import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.model.Trip
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding

fun tripAdapterDelegate(itemClickedListener: (Trip) -> Unit, dateFormatter: DateFormatter) =
    adapterDelegateViewBinding<Trip, Any, ItemTripOrDistanceCardBinding>({ layoutInflater, root ->
        ItemTripOrDistanceCardBinding.inflate(layoutInflater, root, false)
    }) {

        itemView.setOnClickListener { itemClickedListener(item) }

        bind {
            binding.textPrice.text = item.price.currencyFormattedPrice
            binding.textName.text = item.name

            val start = dateFormatter.getFormattedDate(item.startDisplayableDate)
            val end = dateFormatter.getFormattedDate(item.endDisplayableDate)
            binding.textDetails.text = itemView.context.getString(R.string.trip_adapter_list_item_to, start, end)
        }

    }
