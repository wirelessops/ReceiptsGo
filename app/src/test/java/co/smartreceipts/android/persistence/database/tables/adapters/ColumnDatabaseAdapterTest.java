package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.ConstantColumn;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;
import co.smartreceipts.android.sync.model.SyncState;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ColumnDatabaseAdapterTest {

    private static final String ID_COLUMN = "id_column";
    private static final String NAME_COLUMN = "name_column";
    private static final String CUSTOM_ORDER_ID_COLUMN = "custom_order_id";

    private static final int ID = 5;
    private static final int PRIMARY_KEY_ID = 11;
    private static final String NAME = "abcd";
    private static final long CUSTOM_ORDER_ID = 10;

    // Class under test
    ColumnDatabaseAdapter mColumnDatabaseAdapter;

    @Mock
    Cursor mCursor;

    @Mock
    Column<Receipt> mColumn;

    @Mock
    PrimaryKey<Column<Receipt>, Integer> mPrimaryKey;

    @Mock
    ColumnDefinitions<Receipt> mReceiptColumnDefinitions;

    @Mock
    SyncStateAdapter mSyncStateAdapter;

    @Mock
    SyncState mSyncState, mGetSyncState;

    Column<Receipt> mIdColumn;

    Column<Receipt> mPrimaryKeyIdColumn;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        final int idIndex = 1;
        final int nameIndex = 2;
        final int customOrderIdIndex = 3;
        when(mCursor.getColumnIndex(ID_COLUMN)).thenReturn(idIndex);
        when(mCursor.getColumnIndex(NAME_COLUMN)).thenReturn(nameIndex);
        when(mCursor.getInt(idIndex)).thenReturn(ID);
        when(mCursor.getString(nameIndex)).thenReturn(NAME);
        when(mCursor.getLong(customOrderIdIndex)).thenReturn(CUSTOM_ORDER_ID);

        when(mColumn.getId()).thenReturn(ID);
        when(mColumn.getName()).thenReturn(NAME);
        when(mColumn.getSyncState()).thenReturn(mSyncState);
        when(mColumn.getCustomOrderId()).thenReturn(CUSTOM_ORDER_ID);

        mIdColumn = new ConstantColumn<>(ID, NAME, mSyncState, CUSTOM_ORDER_ID);
        mPrimaryKeyIdColumn = new ConstantColumn<>(PRIMARY_KEY_ID, NAME, mGetSyncState, CUSTOM_ORDER_ID);
        when(mReceiptColumnDefinitions.getColumn(ID, NAME, mSyncState, 0)).thenReturn(mIdColumn);
        when(mReceiptColumnDefinitions.getColumn(PRIMARY_KEY_ID, NAME, mGetSyncState, 0)).thenReturn(mPrimaryKeyIdColumn);

        when(mPrimaryKey.getPrimaryKeyValue(mColumn)).thenReturn(PRIMARY_KEY_ID);

        when(mSyncStateAdapter.read(mCursor)).thenReturn(mSyncState);
        when(mSyncStateAdapter.get(any(SyncState.class), any(DatabaseOperationMetadata.class))).thenReturn(mGetSyncState);

        mColumnDatabaseAdapter = new ColumnDatabaseAdapter(mReceiptColumnDefinitions, ID_COLUMN, NAME_COLUMN, mSyncStateAdapter);
    }

    @Test
    public void read() throws Exception {
        assertEquals(mIdColumn, mColumnDatabaseAdapter.read(mCursor));
    }

    @Test
    public void write() throws Exception {
        final String sync = "sync";
        final ContentValues syncValues = new ContentValues();
        syncValues.put(sync, sync);
        when(mSyncStateAdapter.writeUnsynced(mSyncState)).thenReturn(syncValues);

        final ContentValues contentValues = mColumnDatabaseAdapter.write(mColumn, new DatabaseOperationMetadata());
        assertEquals(NAME, contentValues.getAsString(NAME_COLUMN));
        assertEquals(sync, contentValues.getAsString(sync));
        assertFalse(contentValues.containsKey(ID_COLUMN));
        assertEquals(CUSTOM_ORDER_ID, (long) contentValues.getAsLong(CUSTOM_ORDER_ID_COLUMN));
    }

    @Test
    public void writeUnsycned() throws Exception {
        final String sync = "sync";
        final ContentValues syncValues = new ContentValues();
        syncValues.put(sync, sync);
        when(mSyncStateAdapter.write(mSyncState)).thenReturn(syncValues);

        final ContentValues contentValues = mColumnDatabaseAdapter.write(mColumn, new DatabaseOperationMetadata(OperationFamilyType.Sync));
        assertEquals(NAME, contentValues.getAsString(NAME_COLUMN));
        assertEquals(sync, contentValues.getAsString(sync));
        assertFalse(contentValues.containsKey(ID_COLUMN));
        assertEquals(CUSTOM_ORDER_ID, (long) contentValues.getAsLong(CUSTOM_ORDER_ID_COLUMN));
    }

    @Test
    public void build() throws Exception {
        assertEquals(mPrimaryKeyIdColumn, mColumnDatabaseAdapter.build(mColumn, mPrimaryKey, mock(DatabaseOperationMetadata.class)));
        assertEquals(mPrimaryKeyIdColumn.getSyncState(), mColumnDatabaseAdapter.build(mColumn, mPrimaryKey, mock(DatabaseOperationMetadata.class)).getSyncState());
    }
}