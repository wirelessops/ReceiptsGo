package co.smartreceipts.android.search.delegates

import android.graphics.Typeface
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import co.smartreceipts.android.R
import co.smartreceipts.android.databinding.ItemReceiptCardBinding
import co.smartreceipts.android.images.RoundedTransformation
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.core.sync.provider.SyncProvider
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.squareup.picasso.Picasso

/*
This adapter is used for the search results. Unfortunately can't use it for receipts list due to drag&drop feature
 */

fun receiptAdapterDelegate(itemClickedListener: (Receipt) -> Unit, syncProvider: SyncProvider) =
    adapterDelegateViewBinding<Receipt, Any, ItemReceiptCardBinding>(
        { layoutInflater, root -> ItemReceiptCardBinding.inflate(layoutInflater, root, false) }
    ) {

        itemView.setOnClickListener { itemClickedListener(item) }

        val transformation = RoundedTransformation()

        bind {

            binding.content.apply {

                textName.text = item.name
                textPrice.text = item.price.currencyFormattedPrice

                textDetails.text = item.category.name
            }

            if (item.hasPDF()) {
                setIcon(binding.receiptImage, R.drawable.ic_file_black_24dp)
            } else if (item.hasImage() && item.file != null) {
                binding.receiptImage.setPadding(0, 0, 0, 0)
                Picasso.get()
                    .load(item.file!!)
                    .fit()
                    .centerCrop()
                    .transform(transformation)
                    .into(binding.receiptImage)
            } else {
                setIcon(binding.receiptImage, R.drawable.ic_receipt_white_24dp)
            }

            binding.content.imageSyncState.setOnClickListener(null)

            if (syncProvider == SyncProvider.GoogleDrive) {
                when {
                    item.syncState.isSynced(SyncProvider.GoogleDrive) -> binding.content.imageSyncState.setImageResource(
                        R.drawable.ic_cloud_done_24dp
                    )
                    else -> binding.content.imageSyncState.setImageResource(R.drawable.ic_cloud_queue_24dp)
                }
            } else {
                binding.content.imageSyncState.setImageResource(R.drawable.ic_cloud_off_24dp)
            }
        }
    }

private fun setIcon(view: ImageView, @DrawableRes drawableRes: Int) {
    val context = view.context
    val drawable = ResourcesCompat.getDrawable(context.resources, drawableRes, context.getTheme())
    if (drawable != null) {
        drawable.mutate() // hack to prevent fab icon tinting (fab has drawable with the same src)
        DrawableCompat.setTint(drawable, ResourcesCompat.getColor(context.resources, R.color.receipt_image_tint, null))
        val pixelPadding = context.resources.getDimensionPixelOffset(R.dimen.card_image_padding)
        view.setImageDrawable(drawable)
        view.setPadding(pixelPadding, pixelPadding, pixelPadding, pixelPadding)
    }
}