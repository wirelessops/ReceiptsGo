package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.List;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.factory.CategoryBuilderFactory;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;

import static co.smartreceipts.android.persistence.database.tables.AbstractSqlTable.COLUMN_CUSTOM_ORDER_ID;
import static co.smartreceipts.android.persistence.database.tables.AbstractSqlTable.COLUMN_DRIVE_IS_SYNCED;
import static co.smartreceipts.android.persistence.database.tables.AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION;
import static co.smartreceipts.android.persistence.database.tables.AbstractSqlTable.COLUMN_DRIVE_SYNC_ID;
import static co.smartreceipts.android.persistence.database.tables.AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME;
import static co.smartreceipts.android.persistence.database.tables.CategoriesTable.COLUMN_BREAKDOWN;
import static co.smartreceipts.android.persistence.database.tables.CategoriesTable.COLUMN_CODE;
import static co.smartreceipts.android.persistence.database.tables.CategoriesTable.COLUMN_ID;
import static co.smartreceipts.android.persistence.database.tables.CategoriesTable.COLUMN_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class CategoriesTableTest {

    private static final String NAME1 = "name1";
    private static final String NAME2 = "name2";
    private static final String CODE1 = "code1";
    private static final String CODE2 = "code2";
    private static final int ORDER_ID1 = 1;
    private static final int ORDER_ID2 = 2;

    // Class under test
    CategoriesTable mCategoriesTable;

    @Mock
    SQLiteDatabase mSQLiteDatabase;

    @Mock
    TableDefaultsCustomizer mTableDefaultsCustomizer;

    @Captor
    ArgumentCaptor<String> mSqlCaptor;

    SQLiteOpenHelper mSQLiteOpenHelper;

    Category mCategory1;

    Category mCategory2;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        mSQLiteOpenHelper = new TestSQLiteOpenHelper(RuntimeEnvironment.application);
        mCategoriesTable = new CategoriesTable(mSQLiteOpenHelper, false);

        // Now create the table and insert some defaults
        mCategoriesTable.onCreate(mSQLiteOpenHelper.getWritableDatabase(), mTableDefaultsCustomizer);
        mCategory1 = mCategoriesTable.insert(new CategoryBuilderFactory().setName(NAME1).setCode(CODE1).setCustomOrderId(ORDER_ID1).build(), new DatabaseOperationMetadata()).blockingGet();
        mCategory2 = mCategoriesTable.insert(new CategoryBuilderFactory().setName(NAME2).setCode(CODE2).setCustomOrderId(ORDER_ID2).build(), new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(mCategory1);
        assertNotNull(mCategory2);
    }

    @After
    public void tearDown() {
        mSQLiteOpenHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + mCategoriesTable.getTableName());
    }

    @Test
    public void getTableName() {
        assertEquals("categories", mCategoriesTable.getTableName());
    }

    @Test
    public void onCreate() {
        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mCategoriesTable.onCreate(mSQLiteDatabase, customizer);
        verify(mSQLiteDatabase, atLeastOnce()).execSQL(mSqlCaptor.capture());
        verify(customizer).insertCategoryDefaults(mCategoriesTable);

        final List<String> allValues = mSqlCaptor.getAllValues();
        assertEquals(1, allValues.size());
        String creatingTable = allValues.get(0);

        assertTrue(creatingTable.contains("CREATE TABLE categories"));
        assertTrue(creatingTable.contains("id INTEGER PRIMARY KEY AUTOINCREMENT"));
        assertTrue(creatingTable.contains("name TEXT"));
        assertTrue(creatingTable.contains("code TEXT"));
        assertTrue(creatingTable.contains("breakdown BOOLEAN"));
        assertTrue(creatingTable.contains("drive_sync_id TEXT"));
        assertTrue(creatingTable.contains("drive_is_synced BOOLEAN DEFAULT 0"));
        assertTrue(creatingTable.contains("drive_marked_for_deletion BOOLEAN DEFAULT 0"));
        assertTrue(creatingTable.contains("last_local_modification_time DATE"));
        assertTrue(creatingTable.contains("custom_order_id INTEGER DEFAULT 0"));
    }

    @Test
    public void onUpgradeFromV2() {
        final int oldVersion = 2;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mCategoriesTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, atLeastOnce()).execSQL(mSqlCaptor.capture());
        verify(customizer, never()).insertCategoryDefaults(mCategoriesTable);

        assertTrue(mSqlCaptor.getAllValues().get(0).equals("ALTER TABLE categories ADD breakdown BOOLEAN DEFAULT 1"));
        assertEquals(mSqlCaptor.getAllValues().get(1), "ALTER TABLE " + mCategoriesTable.getTableName() + " ADD drive_sync_id TEXT");
        assertEquals(mSqlCaptor.getAllValues().get(2), "ALTER TABLE " + mCategoriesTable.getTableName() + " ADD drive_is_synced BOOLEAN DEFAULT 0");
        assertEquals(mSqlCaptor.getAllValues().get(3), "ALTER TABLE " + mCategoriesTable.getTableName() + " ADD drive_marked_for_deletion BOOLEAN DEFAULT 0");
        assertEquals(mSqlCaptor.getAllValues().get(4), "ALTER TABLE " + mCategoriesTable.getTableName() + " ADD last_local_modification_time DATE");
    }

    @Test
    public void onUpgradeFromV14() {
        final int oldVersion = 14;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mCategoriesTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, atLeastOnce()).execSQL(mSqlCaptor.capture());
        verify(customizer, never()).insertCategoryDefaults(mCategoriesTable);

        assertEquals(mSqlCaptor.getAllValues().get(0), "ALTER TABLE " + mCategoriesTable.getTableName() + " ADD drive_sync_id TEXT");
        assertEquals(mSqlCaptor.getAllValues().get(1), "ALTER TABLE " + mCategoriesTable.getTableName() + " ADD drive_is_synced BOOLEAN DEFAULT 0");
        assertEquals(mSqlCaptor.getAllValues().get(2), "ALTER TABLE " + mCategoriesTable.getTableName() + " ADD drive_marked_for_deletion BOOLEAN DEFAULT 0");
        assertEquals(mSqlCaptor.getAllValues().get(3), "ALTER TABLE " + mCategoriesTable.getTableName() + " ADD last_local_modification_time DATE");
    }

    @Test
    public void onUpgradeFromV15() {
        final int oldVersion = 15;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mCategoriesTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, atLeastOnce()).execSQL(mSqlCaptor.capture());
        verify(customizer, never()).insertCategoryDefaults(mCategoriesTable);

        List<String> allValues = mSqlCaptor.getAllValues();
        assertEquals(4, allValues.size());

        assertEquals(allValues.get(0), "CREATE TABLE " + CategoriesTable.TABLE_NAME + "_copy" + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_CODE + " TEXT, "
                + COLUMN_BREAKDOWN + " BOOLEAN DEFAULT 1, "
                + COLUMN_DRIVE_SYNC_ID + " TEXT, "
                + COLUMN_DRIVE_IS_SYNCED + " BOOLEAN DEFAULT 0, "
                + COLUMN_DRIVE_MARKED_FOR_DELETION + " BOOLEAN DEFAULT 0, "
                + COLUMN_LAST_LOCAL_MODIFICATION_TIME + " DATE, "
                + COLUMN_CUSTOM_ORDER_ID + " INTEGER DEFAULT 0"
                + ");");

        final String baseColumns = String.format("%s, %s, %s, %s, %s, %s, %s",
                COLUMN_NAME, COLUMN_CODE, COLUMN_BREAKDOWN, COLUMN_DRIVE_SYNC_ID,
                COLUMN_DRIVE_IS_SYNCED, COLUMN_DRIVE_MARKED_FOR_DELETION, COLUMN_LAST_LOCAL_MODIFICATION_TIME);


        assertEquals(allValues.get(1), "INSERT INTO " + CategoriesTable.TABLE_NAME + "_copy ("
                + baseColumns + ") "
                + "SELECT " + baseColumns
                + " FROM " + CategoriesTable.TABLE_NAME + ";");

        assertEquals(allValues.get(2), "DROP TABLE " + CategoriesTable.TABLE_NAME + ";");

        assertEquals(allValues.get(3), "ALTER TABLE " + CategoriesTable.TABLE_NAME + "_copy "
                + "RENAME TO " + CategoriesTable.TABLE_NAME + ";");

    }

    @Test
    public void onUpgradeAlreadyOccurred() {
        final int oldVersion = DatabaseHelper.DATABASE_VERSION;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mCategoriesTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, never()).execSQL(mSqlCaptor.capture());
        verify(customizer, never()).insertCategoryDefaults(mCategoriesTable);
    }

    @Test
    public void get() {
        final List<Category> categories = mCategoriesTable.get().blockingGet();
        assertEquals(categories, Arrays.asList(mCategory1, mCategory2));
    }

    @Test
    public void getMaintainsAlphabeticalSortingOrder() {
        final Category zCategory = mCategoriesTable.insert(new CategoryBuilderFactory().setName("zz").setCode("zz").setCustomOrderId(3).build(), new DatabaseOperationMetadata()).blockingGet();
        final Category aCategory = mCategoriesTable.insert(new CategoryBuilderFactory().setName("aa").setCode("aa").setCustomOrderId(4).build(), new DatabaseOperationMetadata()).blockingGet();

        final List<Category> categories = mCategoriesTable.get().blockingGet();
        assertEquals(categories, Arrays.asList(aCategory, mCategory1, mCategory2, zCategory));
    }

    @Test
    public void findByPrimaryKey() {
        mCategoriesTable.findByPrimaryKey(mCategory1.getId())
                .test()
                .assertNoErrors()
                .assertResult(mCategory1);
    }

    @Test
    public void findByPrimaryMissingKey() {
        mCategoriesTable.findByPrimaryKey(28)
                .test()
                .assertError(Exception.class);
    }

    @Test
    public void insert() {
        final String name = "abc";
        final String code = "abc";
        final int customOrderId = 5;
        final Category insertCategory = new CategoryBuilderFactory().setName(name).setCode(code).setCustomOrderId(customOrderId).build();
        Category insertedCategory = mCategoriesTable.insert(insertCategory, new DatabaseOperationMetadata()).blockingGet();

        assertEquals(insertCategory.getName(), insertedCategory.getName());
        assertEquals(insertCategory.getCode(), insertedCategory.getCode());
        assertEquals(insertCategory.getCustomOrderId(), insertedCategory.getCustomOrderId());

        final List<Category> categories = mCategoriesTable.get().blockingGet();
        assertEquals(categories, Arrays.asList(insertedCategory, mCategory1, mCategory2));
    }
    
    @Test
    public void update() {
        final String name = "NewName";
        final String code = "NewCode";
        final int customOrderId = 5;
        final Category updateCategory = new CategoryBuilderFactory().setName(name).setCode(code).setCustomOrderId(customOrderId).build();
        Category updatedCategory = mCategoriesTable.update(mCategory1, updateCategory, new DatabaseOperationMetadata()).blockingGet();

        assertNotNull(updatedCategory);
        assertEquals(name, updatedCategory.getName());
        assertEquals(code, updatedCategory.getCode());
        assertEquals(customOrderId, updatedCategory.getCustomOrderId());
        assertFalse(mCategory1.equals(updatedCategory));

        final List<Category> categories = mCategoriesTable.get().blockingGet();
        assertTrue(categories.contains(new CategoryBuilderFactory()
                .setId(mCategory1.getId())
                .setName(name)
                .setCode(code)
                .setCustomOrderId(customOrderId)
                .build()));
        assertTrue(categories.contains(mCategory2));
    }

    @Test
    public void delete() {
        final List<Category> oldCategories = mCategoriesTable.get().blockingGet();
        assertTrue(oldCategories.contains(mCategory1));
        assertTrue(oldCategories.contains(mCategory2));

        assertEquals(mCategory1, mCategoriesTable.delete(mCategory1, new DatabaseOperationMetadata()).blockingGet());
        assertEquals(mCategory2, mCategoriesTable.delete(mCategory2, new DatabaseOperationMetadata()).blockingGet());

        final List<Category> newCategories = mCategoriesTable.get().blockingGet();
        assertTrue(newCategories.isEmpty());
    }

}