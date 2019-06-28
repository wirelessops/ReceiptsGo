package co.smartreceipts.android.sync.model;

import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.sql.Date;

import co.smartreceipts.android.sync.model.impl.Identifier;
import co.smartreceipts.android.sync.provider.SyncProvider;

public interface SyncState extends Parcelable {

    /**
     * Gets the unique id associated with the "cloud" version of this object
     *
     * @param provider the {@link SyncProvider} for this identifier
     * @return the {@link Identifier} instance or {@code null} if this is an unknown provider
     */
    @Nullable
    Identifier getSyncId(@NonNull SyncProvider provider);

    /**
     * Checks if this item has been synced
     *
     * @param provider the {@link SyncProvider} for to check for
     * @return {@code true} if this item is synced
     */
    boolean isSynced(@NonNull SyncProvider provider);

    /**
     * Checks if this item has been marked for deletion
     *
     * @param provider the {@link SyncProvider} for to check for
     * @return {@code true} if this item is marked for remote deletion
     */
    boolean isMarkedForDeletion(@NonNull SyncProvider provider);

    /**
     * Gets the last time (in UTC) that this item was modified locally
     *
     * @return the last {@link Date} time this item was modified
     */
    @NonNull
    Date getLastLocalModificationTime();


}
