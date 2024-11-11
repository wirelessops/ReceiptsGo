package com.wops.receiptsgo.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.UUID;

import com.wops.receiptsgo.model.PaymentMethod;
import com.wops.receiptsgo.model.factory.PaymentMethodBuilderFactory;
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;
import com.wops.receiptsgo.persistence.database.operations.OperationFamilyType;
import com.wops.core.sync.model.SyncState;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PaymentMethodDatabaseAdapterTest {

    private static final int ID = 5;
    private static final String METHOD = "abcd";
    private static final long CUSTOM_ORDER = 8;
    private static final UUID PM_UUID = UUID.randomUUID();

    // Class under test
    PaymentMethodDatabaseAdapter mPaymentMethodDatabaseAdapter;

    @Mock
    Cursor mCursor;

    @Mock
    PaymentMethod mPaymentMethod;

    @Mock
    SyncStateAdapter mSyncStateAdapter;

    @Mock
    SyncState mSyncState, mGetSyncState;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        final int idIndex = 1;
        final int methodIndex = 2;
        final int orderIndex = 3;
        final int uuidIndex = 4;
        when(mCursor.getColumnIndex("id")).thenReturn(idIndex);
        when(mCursor.getColumnIndex("method")).thenReturn(methodIndex);
        when(mCursor.getColumnIndex("custom_order_id")).thenReturn(orderIndex);
        when(mCursor.getColumnIndex("entity_uuid")).thenReturn(uuidIndex);
        when(mCursor.getInt(idIndex)).thenReturn(ID);
        when(mCursor.getString(methodIndex)).thenReturn(METHOD);
        when(mCursor.getLong(orderIndex)).thenReturn(CUSTOM_ORDER);
        when(mCursor.getString(uuidIndex)).thenReturn(PM_UUID.toString());

        when(mPaymentMethod.getId()).thenReturn(ID);
        when(mPaymentMethod.getMethod()).thenReturn(METHOD);
        when(mPaymentMethod.getSyncState()).thenReturn(mSyncState);
        when(mPaymentMethod.getCustomOrderId()).thenReturn(CUSTOM_ORDER);
        when(mPaymentMethod.getUuid()).thenReturn(PM_UUID);

        when(mSyncStateAdapter.read(mCursor)).thenReturn(mSyncState);
        when(mSyncStateAdapter.get(any(SyncState.class), any(DatabaseOperationMetadata.class))).thenReturn(mGetSyncState);

        mPaymentMethodDatabaseAdapter = new PaymentMethodDatabaseAdapter(mSyncStateAdapter);
    }

    @Test
    public void read() throws Exception {
        final PaymentMethod paymentMethod = new PaymentMethodBuilderFactory()
                .setId(ID)
                .setUuid(PM_UUID)
                .setMethod(METHOD)
                .setSyncState(mSyncState)
                .setCustomOrderId(CUSTOM_ORDER)
                .build();
        assertEquals(paymentMethod, mPaymentMethodDatabaseAdapter.read(mCursor));
    }

    @Test
    public void writeUnsynced() throws Exception {
        final String sync = "sync";
        final ContentValues syncValues = new ContentValues();
        syncValues.put(sync, sync);
        when(mSyncStateAdapter.writeUnsynced(mSyncState)).thenReturn(syncValues);

        final ContentValues contentValues = mPaymentMethodDatabaseAdapter.write(mPaymentMethod, new DatabaseOperationMetadata());
        assertEquals(METHOD, contentValues.getAsString("method"));
        assertEquals(sync, contentValues.getAsString(sync));
        assertEquals(CUSTOM_ORDER, (int) contentValues.getAsInteger("custom_order_id"));
        assertEquals(PM_UUID.toString(), contentValues.getAsString("entity_uuid"));
        assertFalse(contentValues.containsKey("id"));
    }

    @Test
    public void write() throws Exception {
        final String sync = "sync";
        final ContentValues syncValues = new ContentValues();
        syncValues.put(sync, sync);
        when(mSyncStateAdapter.write(mSyncState)).thenReturn(syncValues);

        final ContentValues contentValues = mPaymentMethodDatabaseAdapter.write(mPaymentMethod, new DatabaseOperationMetadata(OperationFamilyType.Sync));
        assertEquals(METHOD, contentValues.getAsString("method"));
        assertEquals(sync, contentValues.getAsString(sync));
        assertEquals(CUSTOM_ORDER, (int) contentValues.getAsInteger("custom_order_id"));
        assertEquals(PM_UUID.toString(), contentValues.getAsString("entity_uuid"));
        assertFalse(contentValues.containsKey("id"));
    }

    @Test
    public void build() throws Exception {
        final PaymentMethod paymentMethod = new PaymentMethodBuilderFactory()
                .setId(ID)
                .setUuid(PM_UUID)
                .setMethod(METHOD)
                .setSyncState(mGetSyncState)
                .setCustomOrderId(CUSTOM_ORDER)
                .build();
        final PaymentMethod actual = mPaymentMethodDatabaseAdapter.build(mPaymentMethod, ID, PM_UUID, mock(DatabaseOperationMetadata.class));

        assertEquals(paymentMethod, actual);
    }
}