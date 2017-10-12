package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.factory.CategoryBuilderFactory;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;
import co.smartreceipts.android.sync.model.SyncState;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CategoryDatabaseAdapterTest {

    private static final String NAME = "name_123";
    private static final int PRIMARY_KEY_INT = 15;
    private static final String CODE = "code_123";

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
        when(mCursor.getColumnIndex("id")).thenReturn(idIndex);
        when(mCursor.getColumnIndex("name")).thenReturn(nameIndex);
        when(mCursor.getColumnIndex("code")).thenReturn(codeIndex);
        when(mCursor.getInt(idIndex)).thenReturn(PRIMARY_KEY_INT);
        when(mCursor.getString(nameIndex)).thenReturn(NAME);
        when(mCursor.getString(codeIndex)).thenReturn(CODE);

        when(mCategory.getId()).thenReturn(PRIMARY_KEY_INT);
        when(mCategory.getName()).thenReturn(NAME);
        when(mCategory.getCode()).thenReturn(CODE);
        when(mCategory.getSyncState()).thenReturn(mSyncState);

        when(mPrimaryKey.getPrimaryKeyValue(mCategory)).thenReturn(PRIMARY_KEY_INT);

        when(mSyncStateAdapter.read(mCursor)).thenReturn(mSyncState);
        when(mSyncStateAdapter.get(any(SyncState.class), any(DatabaseOperationMetadata.class))).thenReturn(mGetSyncState);

        mCategoryDatabaseAdapter = new CategoryDatabaseAdapter(mSyncStateAdapter);
    }

    @Test
    public void read() throws Exception {
        final Category category = new CategoryBuilderFactory().setId(PRIMARY_KEY_INT).setName(NAME).setCode(CODE).setSyncState(mSyncState).build();
        assertEquals(category, mCategoryDatabaseAdapter.read(mCursor));
    }

    @Test
    public void writeUnsynced() throws Exception {
        final String sync = "sync";
        final ContentValues syncValues = new ContentValues();
        syncValues.put(sync, sync);
        when(mSyncStateAdapter.writeUnsynced(mSyncState)).thenReturn(syncValues);

        final ContentValues contentValues = mCategoryDatabaseAdapter.write(mCategory, new DatabaseOperationMetadata());
        assertEquals(NAME, contentValues.getAsString("name"));
        assertEquals(CODE, contentValues.getAsString("code"));
        assertEquals(sync, contentValues.getAsString(sync));
    }

    @Test
    public void write() throws Exception {
        final String sync = "sync";
        final ContentValues syncValues = new ContentValues();
        syncValues.put(sync, sync);
        when(mSyncStateAdapter.write(mSyncState)).thenReturn(syncValues);

        final ContentValues contentValues = mCategoryDatabaseAdapter.write(mCategory, new DatabaseOperationMetadata(OperationFamilyType.Sync));
        assertEquals(NAME, contentValues.getAsString("name"));
        assertEquals(CODE, contentValues.getAsString("code"));
        assertEquals(sync, contentValues.getAsString(sync));
    }

    @Test
    public void build() throws Exception {
        final Category category = new CategoryBuilderFactory().setId(PRIMARY_KEY_INT).setName(NAME).setCode(CODE).setSyncState(mGetSyncState).build();
        assertEquals(category, mCategoryDatabaseAdapter.build(mCategory, mPrimaryKey, mock(DatabaseOperationMetadata.class)));
        assertEquals(category.getSyncState(), mCategoryDatabaseAdapter.build(mCategory, mPrimaryKey, mock(DatabaseOperationMetadata.class)).getSyncState());
    }

    // TODO: 11.10.2017 test custom order id
}