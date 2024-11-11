package com.wops.receiptsgo.search.delegates

import android.net.Uri
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.wops.receiptsgo.R
import com.wops.receiptsgo.sync.BackupProvidersManager
import co.smartreceipts.core.sync.model.Syncable
import co.smartreceipts.core.sync.provider.SyncProvider
import com.squareup.picasso.Picasso

object SyncStateViewHelper {

    /**
     * This function sets correct sync state image for trip or distance object
     * For receipts another logic should be used
     */
    fun setSyncStateImage(image: ImageView, data: Syncable, backupProvidersManager: BackupProvidersManager) {

        val context = image.context

        val cloudDisabledDrawable = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_cloud_off_24dp, context.theme)
        val notSyncedDrawable = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_cloud_queue_24dp, context.theme)
        val syncedDrawable = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_cloud_done_24dp, context.theme)

        image.isClickable = false

            if (backupProvidersManager.syncProvider === SyncProvider.GoogleDrive) {
                if (backupProvidersManager.lastDatabaseSyncTime.time >= data.syncState.lastLocalModificationTime.time
                    && data.syncState.lastLocalModificationTime.time >= 0
                ) {
                    Picasso.get().load(Uri.EMPTY).placeholder(syncedDrawable!!).into(image)
                } else {
                    Picasso.get().load(Uri.EMPTY).placeholder(notSyncedDrawable!!).into(image)
                }
            } else {
                if (backupProvidersManager.lastDatabaseSyncTime.time < data.syncState.lastLocalModificationTime.time) {
                    Picasso.get().load(Uri.EMPTY).placeholder(cloudDisabledDrawable!!).into(image)
                } else if (backupProvidersManager.lastDatabaseSyncTime.time >= data.syncState.lastLocalModificationTime.time) {
                    Picasso.get().load(Uri.EMPTY).placeholder(syncedDrawable!!).into(image)
                }
            }
    }
}