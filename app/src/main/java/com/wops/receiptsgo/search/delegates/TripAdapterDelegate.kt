package com.wops.receiptsgo.search.delegates

import android.graphics.Typeface
import android.view.View
import androidx.core.content.ContextCompat
import com.wops.receiptsgo.R
import com.wops.receiptsgo.databinding.ItemDefaultContentBinding
import com.wops.receiptsgo.date.DateFormatter
import com.wops.receiptsgo.model.Trip
import com.wops.receiptsgo.sync.BackupProvidersManager
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding

interface SelectedItemIdHolder {
    var selectedItemId: Int
}

fun tripAdapterDelegate(
    itemClickedListener: (Trip) -> Unit,
    itemLongClickedListener: (Trip) -> Unit,
    dateFormatter: DateFormatter,
    backupProvidersManager: BackupProvidersManager? = null,
    selectedItemIdHolder: SelectedItemIdHolder? = null
) =
    adapterDelegateViewBinding<Trip, Any, ItemDefaultContentBinding>({ layoutInflater, root ->
        ItemDefaultContentBinding.inflate(layoutInflater, root, false)
    }) {

        val colorSelected = ContextCompat.getColor(context, R.color.smart_receipts_colorPrimary)
        val colorDefault = ContextCompat.getColor(context, R.color.text_primary_color)

        itemView.setOnClickListener { itemClickedListener(item) }

        itemView.setOnLongClickListener {
            itemLongClickedListener(item)
            true
        }

        bind {

            val selectedItemId = selectedItemIdHolder?.selectedItemId ?: -1

            if (item.id == selectedItemId) {
                binding.imageSelectionMarker.visibility = View.VISIBLE
                binding.textName.setTextColor(colorSelected)
                binding.textPrice.setTextColor(colorSelected)
            } else {
                binding.imageSelectionMarker.visibility = View.GONE
                binding.textName.setTextColor(colorDefault)
                binding.textPrice.setTextColor(colorDefault)
            }

            binding.textPrice.text = item.price.currencyFormattedPrice
            binding.textName.text = item.name

            val start = dateFormatter.getFormattedDate(item.startDisplayableDate)
            val end = dateFormatter.getFormattedDate(item.endDisplayableDate)
            binding.textDetails.text = itemView.context.getString(R.string.trip_adapter_list_item_to, start, end)
            binding.textDetails.setTypeface(null, Typeface.NORMAL)
            binding.textDetails.setTextColor(ContextCompat.getColor(context, R.color.text_secondary_color))


            if (backupProvidersManager != null) {
                binding.imageSyncState.visibility = View.VISIBLE
                SyncStateViewHelper.setSyncStateImage(binding.imageSyncState, item, backupProvidersManager)
            } else {
                binding.imageSyncState.visibility = View.GONE
            }
        }

    }
