package com.wops.core.sync.model.impl;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.wops.core.sync.provider.SyncProvider;

/**
 * When syncing information about when to "delete" an object, we cannot locally delete it until it has been
 * removed remotely. As a result, we need to locally "mark" it for deletion and only fully delete it once
 * we have received confirmation from our remote servers that this is okay.
 *
 * A JSON parser (e.g GSON) can be used to convert JSON data into instances of this object
 */
public class MarkedForDeletionMap implements Serializable {

    @SerializedName("deletion_map")
    private final Map<SyncProvider, Boolean> markedForDeletionMap;

    public MarkedForDeletionMap(@NonNull Map<SyncProvider, Boolean> markedForDeletionMap) {
        this.markedForDeletionMap = new HashMap<>(Preconditions.checkNotNull(markedForDeletionMap));
    }

    /**
     * Checks if this item has been marked for deletion
     *
     * @param provider the {@link SyncProvider} for to check for
     * @return {@code true} if this item is marked for remote deletion
     */
    public boolean isMarkedForDeletion(@NonNull SyncProvider provider) {
        if (this.markedForDeletionMap.containsKey(provider)) {
            return this.markedForDeletionMap.get(provider);
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MarkedForDeletionMap)) return false;

        MarkedForDeletionMap that = (MarkedForDeletionMap) o;

        return markedForDeletionMap.equals(that.markedForDeletionMap);

    }

    @Override
    public int hashCode() {
        return markedForDeletionMap.hashCode();
    }
}