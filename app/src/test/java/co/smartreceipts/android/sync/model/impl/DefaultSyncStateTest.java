package co.smartreceipts.android.sync.model.impl;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.sql.Date;
import java.util.Collections;

import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.core.sync.model.impl.Identifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class DefaultSyncStateTest {

    //Class under testing
    DefaultSyncState mDefaultSyncState;

    Identifier mIdentifier;

    IdentifierMap mIdentifierMap;

    SyncStatusMap mSyncStatusMap;

    MarkedForDeletionMap mMarkedForDeletionMap;

    Date mLastLocalModificationTime;

    @Before
    public void setUp() throws Exception {
        mIdentifier = new Identifier("abc");
        mIdentifierMap = new IdentifierMap(Collections.singletonMap(SyncProvider.GoogleDrive, mIdentifier));
        mSyncStatusMap = new SyncStatusMap(Collections.singletonMap(SyncProvider.GoogleDrive, true));
        mMarkedForDeletionMap = new MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, true));
        mLastLocalModificationTime = new Date(System.currentTimeMillis());
        mDefaultSyncState = new DefaultSyncState(mIdentifierMap, mSyncStatusMap, mMarkedForDeletionMap, mLastLocalModificationTime);
    }

    @Test
    public void getSyncId() {
        assertEquals(mIdentifier, mDefaultSyncState.getSyncId(SyncProvider.GoogleDrive));
        assertNull(mDefaultSyncState.getSyncId(SyncProvider.None));
    }

    @Test
    public void isSynced() {
        assertEquals(true, mDefaultSyncState.isSynced(SyncProvider.GoogleDrive));
        assertEquals(false, mDefaultSyncState.isSynced(SyncProvider.None));
    }

    @Test
    public void isMarkedForDeletion() {
        assertEquals(true, mDefaultSyncState.isMarkedForDeletion(SyncProvider.GoogleDrive));
        assertEquals(false, mDefaultSyncState.isMarkedForDeletion(SyncProvider.None));
    }

    @Test
    public void getLastLocalModificationTime() {
        assertEquals(mLastLocalModificationTime, mDefaultSyncState.getLastLocalModificationTime());
    }

    @Test
    public void parcelEquality() {
        final Parcel parcel = Parcel.obtain();
        mDefaultSyncState.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        final DefaultSyncState parceledSyncState = DefaultSyncState.CREATOR.createFromParcel(parcel);
        assertNotNull(parceledSyncState);
        assertEquals(parceledSyncState, mDefaultSyncState);
    }

}