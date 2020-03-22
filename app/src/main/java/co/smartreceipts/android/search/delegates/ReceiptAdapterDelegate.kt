package co.smartreceipts.android.search.delegates

import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import co.smartreceipts.android.R
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.core.sync.provider.SyncProvider
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateLayoutContainer
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_receipt_card.*


fun receiptAdapterDelegate(itemClickedListener: (Receipt) -> Unit, syncProvider: SyncProvider) =
    adapterDelegateLayoutContainer<Receipt, Any>(R.layout.item_receipt_card) {

        itemView.setOnClickListener { itemClickedListener(item) }

        bind {
            card_menu.visibility = View.GONE

            title.text = item.name
            price.text = item.price.currencyFormattedPrice

            card_category.visibility = View.VISIBLE
            card_category.text = item.category.name

            if (item.hasPDF()) {
                setIcon(card_image, R.drawable.ic_file_black_24dp)
            } else if (item.hasImage() && item.file != null) {
                card_image.setPadding(0, 0, 0, 0)
                Picasso.get()
                    .load(item.file!!)
                    .fit()
                    .centerCrop()
                    .into(card_image)
            } else {
                setIcon(card_image, R.drawable.ic_receipt_white_24dp)
            }

            if (syncProvider == SyncProvider.GoogleDrive) {
                when {
                    item.syncState.isSynced(SyncProvider.GoogleDrive) -> card_sync_state.setImageResource(R.drawable.ic_cloud_done_24dp)
                    else -> card_sync_state.setImageResource(R.drawable.ic_cloud_queue_24dp)
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