package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.UUID;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.factory.CategoryBuilderFactory;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;
import co.smartreceipts.android.sync.model.SyncState;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CategoryDatabaseAdapterTest {

    private static final String NAME = "name_123";
    private static final int PRIMARY_KEY_INT = 15;
    private static final String CODE = "code_123";
    private static final long CUSTOM_ORDER_ID = 10;
    private static final UUID CAT_UUID = UUID.randomUUID();

    private static final String CUSTOM_ORDER_ID_KEY = "custom_order_id";
    private static final String ID_KEY = "id";
    private static final String NAME_KEY = "name";
    private static final String CODE_KEY = "code";
    private static final String UUID_KEY = "entity_uuid";

    // Class under test
    CategoryDatabaseAdapter mCategoryDatabaseAdapter;

    @Mock
    Cursor mCursor;

    @Mock
    Category mCategory;

    @Mock
    PrimaryKey<Category, Integer> mPrimaryKey;

    @Mock
    SyncStateAdapter mSyncStateAdapter;

    @Mock
    SyncState mSyncState, mGetSyncState;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        final int idIndex = 1;
        final int nameIndex = 2;
        final int codeIndex = 3;
        final int customOrderIdIndex = 4;
        final int uuidIndex = 5;
        when(mCursor.getColumnIndex(ID_KEY)).thenReturn(idIndex);
        when(mCursor.getColumnIndex(NAME_KEY)).thenReturn(nameIndex);
        when(mCursor.getColumnIndex(CODE_KEY)).thenReturn(codeIndex);
        when(mCursor.getColumnIndex(CUSTOM_ORDER_ID_KEY)).thenReturn(customOrderIdIndex);
        when(mCursor.getColumnIndex(UUID_KEY)).thenReturn(uuidIndex);
        when(mCursor.getInt(idIndex)).thenReturn(PRIMARY_KEY_INT);
        when(mCursor.getString(nameIndex)).thenReturn(NAME);
        when(mCursor.getString(codeIndex)).thenReturn(CODE);
        when(mCursor.getLong(customOrderIdIndex)).thenReturn(CUSTOM_ORDER_ID);
        when(mCursor.getString(uuidIndex)).thenReturn(CAT_UUID.toString());

        when(mCategory.getId()).thenReturn(PRIMARY_KEY_INT);
        when(mCategory.getName()).thenReturn(NAME);
        when(mCategory.getCode()).thenReturn(CODE);
        when(mCategory.getSyncState()).thenReturn(mSyncState);
        when(mCategory.getCustomOrderId()).thenReturn(CUSTOM_ORDER_ID);
        when(mCategory.getUuid()).thenReturn(CAT_UUID);

        when(mPrimaryKey.getPrimaryKeyValue(mCategory)).thenReturn(PRIMARY_KEY_INT);

        when(mSyncStateAdapter.read(mCursor)).thenReturn(mSyncState);
        when(mSyncStateAdapter.get(any(SyncState.class), any(DatabaseOperationMetadata.class))).thenReturn(mGetSyncState);

        mCategoryDatabaseAdapter = new CategoryDatabaseAdapter(mSyncStateAdapter);
    }

    @Test
    public void read() throws Exception {
        final Category category = new CategoryBuilderFactory()
                .setId(PRIMARY_KEY_INT)
                .setUuid(CAT_UUID)
                .setName(NAME)
                .setCode(CODE)
                .setSyncState(mSyncState)
                .setCustomOrderId(CUSTOM_ORDER_ID)
                .build();
        assertEquals(category, mCategoryDatabaseAdapter.read(mCursor));
    }

    @Test
    public void writeUnsynced() throws Exception {
        final String sync = "sync";
        final ContentValues syncValues = new ContentValues();
        syncValues.put(sync, sync);
        when(mSyncStateAdapter.writeUnsynced(mSyncState)).thenReturn(syncValues);

        final ContentValues contentValues = mCategoryDatabaseAdapter.write(mCategory, new DatabaseOperationMetadata());
        assertEquals(NAME, contentValues.getAsString(NAME_KEY));
        assertEquals(CODE, contentValues.getAsString(CODE_KEY));
        assertEquals(sync, contentValues.getAsString(sync));
        assertEquals(CUSTOM_ORDER_ID, (int) contentValues.getAsInteger(CUSTOM_ORDER_ID_KEY));
        assertEquals(CAT_UUID.toString(), contentValues.getAsString(UUID_KEY));
    }

    @Test
    public void write() throws Exception {
        final String sync = "sync";
        final ContentValues syncValues = new ContentValues();
        syncValues.put(sync, sync);
        when(mSyncStateAdapter.write(mSyncState)).thenReturn(syncValues);

        final ContentValues contentValues = mCategoryDatabaseAdapter.write(mCategory, new DatabaseOperationMetadata(OperationFamilyType.Sync));
        assertEquals(NAME, contentValues.getAsString(NAME_KEY));
        assertEquals(CODE, contentValues.getAsString(CODE_KEY));
        assertEquals(sync, contentValues.getAsString(sync));
        assertEquals(CUSTOM_ORDER_ID, (int) contentValues.getAsInteger(CUSTOM_ORDER_ID_KEY));
        assertEquals(CAT_UUID.toString(), contentValues.getAsString(UUID_KEY));
    }

    @Test
    public void build() throws Exception {
        final Category category = new CategoryBuilderFactory()
                .setId(PRIMARY_KEY_INT)
                .setUuid(CAT_UUID)
                .setName(NAME)
                .setCode(CODE)
                .setSyncState(mGetSyncState)
                .setCustomOrderId(CUSTOM_ORDER_ID)
                .build();
        assertEquals(category, mCategoryDatabaseAdapter.build(mCategory, mPrimaryKey, CAT_UUID, mock(DatabaseOperationMetadata.class)));
        assertEquals(category.getSyncState(), mCategoryDatabaseAdapter.build(mCategory, mPrimaryKey, CAT_UUID, mock(DatabaseOperationMetadata.class)).getSyncState());
    }
}