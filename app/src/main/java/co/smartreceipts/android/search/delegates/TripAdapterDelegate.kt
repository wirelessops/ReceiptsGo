package co.smartreceipts.android.search.delegates

import android.view.View
import co.smartreceipts.android.R
import co.smartreceipts.android.databinding.ItemTripOrDistanceCardBinding
import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.sync.BackupProvidersManager
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding

fun tripAdapterDelegate(itemClickedListener: (Trip) -> Unit, dateFormatter: DateFormatter, backupProvidersManager: BackupProvidersManager?) =
    adapterDelegateViewBinding<Trip, Any, ItemTripOrDistanceCardBinding>({ layoutInflater, root ->
        ItemTripOrDistanceCardBinding.inflate(layoutInflater, root, false)
    }) {

        // TODO: 28.01.2021 add nullable params and handle sync state images (from CardAdapter)
        // TODO: 28.01.2021 don't forget about selection marker

        itemView.setOnClickListener { itemClickedListener(item) }

        bind {
            binding.textPrice.text = item.price.currencyFormattedPrice
            binding.textName.text = item.name

            val start = dateFormatter.getFormattedDate(item.startDisplayableDate)
            val end = dateFormatter.getFormattedDate(item.endDisplayableDate)
            binding.textDetails.text = itemView.context.getString(R.string.trip_adapter_list_item_to, start, end)

            if (backupProvidersManager != null) {
                binding.imageSyncState.visibility = View.VISIBLE
                SyncStateViewHelper.setSyncStateImage(binding.imageSyncState, item, backupProvidersManager)
            } else {
                binding.imageSyncState.visibility = View.GONE
            }
        }

    }
