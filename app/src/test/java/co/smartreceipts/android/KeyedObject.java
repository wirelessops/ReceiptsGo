package co.smartreceipts.android;

import androidx.annotation.VisibleForTesting;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import co.smartreceipts.android.model.Keyed;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.Syncable;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;

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