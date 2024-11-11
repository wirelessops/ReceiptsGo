package com.wops.receiptsgo.search.delegates

import android.graphics.Typeface
import com.wops.receiptsgo.databinding.ItemDefaultContentBinding
import com.wops.receiptsgo.model.Distance
import com.wops.receiptsgo.sync.BackupProvidersManager
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding

fun distanceAdapterDelegate(
    itemClickedListener: (Distance) -> Unit,
    backupProvidersManager: BackupProvidersManager
) =
    adapterDelegateViewBinding<Distance, Any, ItemDefaultContentBinding>({ layoutInflater, parent ->
        ItemDefaultContentBinding.inflate(layoutInflater, parent, false)
    }) {

        itemView.setOnClickListener { itemClickedListener(item) }

        bind {
            binding.textPrice.text = item.price.currencyFormattedPrice
            binding.textName.text = item.decimalFormattedDistance

            val location: String = item.location
            binding.textDetails.text = if (location.isNotEmpty()) location else item.comment

            SyncStateViewHelper.setSyncStateImage(binding.imageSyncState, item, backupProvidersManager)
        }


    }

