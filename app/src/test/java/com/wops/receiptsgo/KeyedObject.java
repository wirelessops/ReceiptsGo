package com.wops.receiptsgo;

import androidx.annotation.VisibleForTesting;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import com.wops.receiptsgo.model.Keyed;
import com.wops.core.sync.model.SyncState;
import com.wops.core.sync.model.Syncable;
import com.wops.core.sync.model.impl.DefaultSyncState;

@VisibleForTesting
public class KeyedObject implements Keyed, Syncable {

    @Override
    public int getId() {
        return Keyed.MISSING_ID;
    }

    @NotNull
    @Override
    public UUID getUuid() {
        return Keyed.Companion.getMISSING_UUID();
    }

    @NotNull
    @Override
    public SyncState getSyncState() {
        return new DefaultSyncState();
    }
}