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

import static org.junit.Assert.assertEquals;
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
        mCategoriesTable = new CategoriesTable(mSQLiteOpenHelper);

        // Now create the table and insert some defaults
        mCategoriesTable.onCreate(mSQLiteOpenHelper.getWritableDatabase(), mTableDefaultsCustomizer);
        mCategory1 = mCategoriesTable.insert(new CategoryBuilderFactory().setName(NAME1).setCode(CODE1).build(), new DatabaseOperationMetadata()).blockingGet();
        mCategory2 = mCategoriesTable.insert(new CategoryBuilderFactory().setName(NAME2).setCode(CODE2).build(), new DatabaseOperationMetadata()).blockingGet();
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
        verify(mSQLiteDatabase).execSQL(mSqlCaptor.capture());
        verify(customizer).insertCategoryDefaults(mCategoriesTable);

        assertTrue(mSqlCaptor.getValue().contains("CREATE TABLE categories"));
        assertTrue(mSqlCaptor.getValue().contains("name TEXT PRIMARY KEY"));
        assertTrue(mSqlCaptor.getValue().contains("code TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("breakdown BOOLEAN"));
        assertTrue(mSqlCaptor.getValue().contains("drive_sync_id TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("drive_is_synced BOOLEAN DEFAULT 0"));
        assertTrue(mSqlCaptor.getValue().contains("drive_marked_for_deletion BOOLEAN DEFAULT 0"));
        assertTrue(mSqlCaptor.getValue().contains("last_local_modification_time DATE"));
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
        final Category zCategory = mCategoriesTable.insert(new CategoryBuilderFactory().setName("zz").setCode("zz").build(), new DatabaseOperationMetadata()).blockingGet();
        final Category aCategory = mCategoriesTable.insert(new CategoryBuilderFactory().setName("aa").setCode("aa").build(), new DatabaseOperationMetadata()).blockingGet();

        final List<Category> categories = mCategoriesTable.get().blockingGet();
        assertEquals(categories, Arrays.asList(aCategory, mCategory1, mCategory2, zCategory));
    }

    @Test
    public void findByPrimaryKey() {
        mCategoriesTable.findByPrimaryKey(mCategory1.getName())
                .test()
                .assertNoErrors()
                .assertResult(mCategory1);
    }

    @Test
    public void findByPrimaryMissingKey() {
        mCategoriesTable.findByPrimaryKey("xxx")
                .test()
                .assertError(Exception.class);
    }

    @Test
    public void insert() {
        final String name = "abc";
        final String code = "abc";
        final Category insertCategory = new CategoryBuilderFactory().setName(name).setCode(code).build();
        assertEquals(insertCategory, mCategoriesTable.insert(insertCategory, new DatabaseOperationMetadata()).blockingGet());

        final List<Category> categories = mCategoriesTable.get().blockingGet();
        assertEquals(categories, Arrays.asList(insertCategory, mCategory1, mCategory2));
    }
    
    @Test
    public void update() {
        final String name = "NewName";
        final String code = "NewCode";
        final Category updateCategory = new CategoryBuilderFactory().setName(name).setCode(code).build();
        assertEquals(updateCategory, mCategoriesTable.update(mCategory1, updateCategory, new DatabaseOperationMetadata()).blockingGet());

        final List<Category> categories = mCategoriesTable.get().blockingGet();
        assertTrue(categories.contains(updateCategory));
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