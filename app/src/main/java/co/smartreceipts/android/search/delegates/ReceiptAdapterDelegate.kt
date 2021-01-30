package co.smartreceipts.android.search.delegates

import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import co.smartreceipts.android.R
import co.smartreceipts.android.databinding.ItemReceiptCardBinding
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.core.sync.provider.SyncProvider
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.squareup.picasso.Picasso

fun receiptAdapterDelegate(itemClickedListener: (Receipt) -> Unit, syncProvider: SyncProvider) =
    adapterDelegateViewBinding<Receipt, Any, ItemReceiptCardBinding>(
        { layoutInflater, root -> ItemReceiptCardBinding.inflate(layoutInflater, root, false) }
    ) {

        itemView.setOnClickListener { itemClickedListener(item) }

        bind {

            binding.apply {
                cardMenu.visibility = View.GONE

                title.text = item.name
                price.text = item.price.currencyFormattedPrice

                cardCategory.visibility = View.VISIBLE
                cardCategory.text = item.category.name
            }

            if (item.hasPDF()) {
                setIcon(binding.cardImage, R.drawable.ic_file_black_24dp)
            } else if (item.hasImage() && item.file != null) {
                binding.cardImage.setPadding(0, 0, 0, 0)
                Picasso.get()
                    .load(item.file!!)
                    .fit()
                    .centerCrop()
                    .into(binding.cardImage)
            } else {
                setIcon(binding.cardImage, R.drawable.ic_receipt_white_24dp)
            }

            // TODO: 28.01.2021 why this logic is not same with CardAdapter?
            if (syncProvider == SyncProvider.GoogleDrive) {
                when {
                    item.syncState.isSynced(SyncProvider.GoogleDrive) -> binding.cardSyncState.setImageResource(R.drawable.ic_cloud_done_24dp)
                    else -> binding.cardSyncState.setImageResource(R.drawable.ic_cloud_queue_24dp)
                }
            }
        }
    }

private fun setIcon(view: ImageView, @DrawableRes drawableRes: Int) {
    val context = view.context
    val drawable = ResourcesCompat.getDrawable(context.resources, drawableRes, context.getTheme())
    if (drawable != null) {
        drawable.mutate() // hack to prevent fab icon tinting (fab has drawable with the same src)
        DrawableCompat.setTint(drawable, ResourcesCompat.getColor(context.resources, R.color.card_image_tint, null))
        val pixelPadding = context.resources.getDimensionPixelOffset(R.dimen.card_image_padding)
        view.setImageDrawable(drawable)
        view.setPadding(pixelPadding, pixelPadding, pixelPadding, pixelPadding)
    }
}