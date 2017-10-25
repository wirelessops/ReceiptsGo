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
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.BlankColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptCategoryNameColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptNameColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptPriceColumn;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PDFTableTest {

    // Class Under Test
    PDFTable mPDFTable;

    @Mock
    ColumnDefinitions<Receipt> mReceiptColumnDefinitions;

    @Mock
    SQLiteDatabase mSQLiteDatabase;

    @Mock
    TableDefaultsCustomizer mTableDefaultsCustomizer;

    SQLiteOpenHelper mSQLiteOpenHelper;

    @Captor
    ArgumentCaptor<String> mSqlCaptor;

    Column<Receipt> mColumn1;

    Column<Receipt> mColumn2;

    Column<Receipt> mDefaultColumn;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        mSQLiteOpenHelper = new TestSQLiteOpenHelper(RuntimeEnvironment.application);
        mPDFTable = new PDFTable(mSQLiteOpenHelper, mReceiptColumnDefinitions);
        mDefaultColumn = new BlankColumn<>(-1, "", new DefaultSyncState());

        when(mReceiptColumnDefinitions.getDefaultInsertColumn()).thenReturn(mDefaultColumn);
        when(mReceiptColumnDefinitions.getColumn(anyInt(), eq(""), any(SyncState.class))).thenReturn(mDefaultColumn);

        // Now create the table and insert some defaults
        mPDFTable.onCreate(mSQLiteOpenHelper.getWritableDatabase(), mTableDefaultsCustomizer);
        mColumn1 = mPDFTable.insert(new ReceiptNameColumn(-1, "Name", new DefaultSyncState()), new DatabaseOperationMetadata()).blockingGet();
        mColumn2 = mPDFTable.insert(new ReceiptPriceColumn(-1, "Price", new DefaultSyncState()), new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(mColumn1);
        assertNotNull(mColumn2);
    }

    @After
    public void tearDown() {
        mSQLiteOpenHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + mPDFTable.getTableName());
    }

    @Test
    public void getTableName() {
        assertEquals("pdfcolumns", mPDFTable.getTableName());
    }

    @Test
    public void onCreate() {
        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mPDFTable.onCreate(mSQLiteDatabase, customizer);
        verify(mSQLiteDatabase).execSQL(mSqlCaptor.capture());
        verify(customizer).insertPDFDefaults(mPDFTable);

        assertTrue(mSqlCaptor.getValue().contains("CREATE TABLE pdfcolumns"));
        assertTrue(mSqlCaptor.getValue().contains("id INTEGER PRIMARY KEY AUTOINCREMENT"));
        assertTrue(mSqlCaptor.getValue().contains("type TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("drive_sync_id TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("drive_is_synced BOOLEAN DEFAULT 0"));
        assertTrue(mSqlCaptor.getValue().contains("drive_marked_for_deletion BOOLEAN DEFAULT 0"));
        assertTrue(mSqlCaptor.getValue().contains("last_local_modification_time DATE"));
        assertTrue(mSqlCaptor.getValue().contains("custom_order_id INTEGER DEFAULT 0"));
    }

    @Test
    public void onUpgradeFromV9() {
        final int oldVersion = 9;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mPDFTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, atLeastOnce()).execSQL(mSqlCaptor.capture());
        verify(customizer).insertPDFDefaults(mPDFTable);

        assertTrue(mSqlCaptor.getAllValues().get(0).contains(PDFTable.TABLE_NAME));
        assertTrue(mSqlCaptor.getAllValues().get(0).contains(PDFTable.COLUMN_ID));
        assertTrue(mSqlCaptor.getAllValues().get(0).contains(PDFTable.COLUMN_TYPE));
        assertEquals(mSqlCaptor.getAllValues().get(0), "CREATE TABLE pdfcolumns (id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT);");
        assertEquals(mSqlCaptor.getAllValues().get(1), "ALTER TABLE " + mPDFTable.getTableName() + " ADD drive_sync_id TEXT");
        assertEquals(mSqlCaptor.getAllValues().get(2), "ALTER TABLE " + mPDFTable.getTableName() + " ADD drive_is_synced BOOLEAN DEFAULT 0");
        assertEquals(mSqlCaptor.getAllValues().get(3), "ALTER TABLE " + mPDFTable.getTableName() + " ADD drive_marked_for_deletion BOOLEAN DEFAULT 0");
        assertEquals(mSqlCaptor.getAllValues().get(4), "ALTER TABLE " + mPDFTable.getTableName() + " ADD last_local_modification_time DATE");
    }

    @Test
    public void onUpgradeFromV14() {
        final int oldVersion = 14;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mPDFTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, atLeastOnce()).execSQL(mSqlCaptor.capture());
        verify(customizer, never()).insertPDFDefaults(mPDFTable);

        assertEquals(mSqlCaptor.getAllValues().get(0), "ALTER TABLE " + mPDFTable.getTableName() + " ADD drive_sync_id TEXT");
        assertEquals(mSqlCaptor.getAllValues().get(1), "ALTER TABLE " + mPDFTable.getTableName() + " ADD drive_is_synced BOOLEAN DEFAULT 0");
        assertEquals(mSqlCaptor.getAllValues().get(2), "ALTER TABLE " + mPDFTable.getTableName() + " ADD drive_marked_for_deletion BOOLEAN DEFAULT 0");
        assertEquals(mSqlCaptor.getAllValues().get(3), "ALTER TABLE " + mPDFTable.getTableName() + " ADD last_local_modification_time DATE");
    }

    @Test
    public void onUpgradeFromV15() {
        final int oldVersion = 15;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mPDFTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase).execSQL(mSqlCaptor.capture());
        verify(customizer, never()).insertPDFDefaults(mPDFTable);

        assertEquals(mSqlCaptor.getValue(), "ALTER TABLE " + mPDFTable.getTableName() + " ADD COLUMN custom_order_id INTEGER DEFAULT 0;");
    }

    @Test
    public void onUpgradeAlreadyOccurred() {
        final int oldVersion = DatabaseHelper.DATABASE_VERSION;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mPDFTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, never()).execSQL(mSqlCaptor.capture());
        verify(customizer, never()).insertPDFDefaults(mPDFTable);
    }

    @Test
    public void get() {
        final List<Column<Receipt>> columns = mPDFTable.get().blockingGet();
        assertEquals(columns, Arrays.asList(mColumn1, mColumn2));
    }

    @Test
    public void findByPrimaryKey() {
        mPDFTable.findByPrimaryKey(mColumn1.getId())
                .test()
                .assertNoErrors()
                .assertResult(mColumn1);
    }

    @Test
    public void findByPrimaryMissingKey() {
        mPDFTable.findByPrimaryKey(-1)
                .test()
                .assertError(Exception.class);
    }

    @Test
    public void insert() {
        final String name = "Code";
        final Column<Receipt> column = mPDFTable.insert(new ReceiptCategoryNameColumn(-1, name, new DefaultSyncState()), new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(column);
        assertEquals(name, column.getName());

        final List<Column<Receipt>> columns = mPDFTable.get().blockingGet();
        assertEquals(columns, Arrays.asList(mColumn1, mColumn2, column));
    }

    @Test
    public void insertDefaultColumn() throws Exception {
        final Column<Receipt> column = mPDFTable.insertDefaultColumn().blockingGet();

        assertNotNull(column);
        assertEquals(column, mDefaultColumn);

        final List<Column<Receipt>> columns = mPDFTable.get().blockingGet();
        assertEquals(Arrays.asList(mColumn1, mColumn2, column), columns);
    }

    @Test
    public void update() {
        final String name = "Code";
        final Column<Receipt> column = mPDFTable.update(mColumn1, new ReceiptCategoryNameColumn(-1, name, new DefaultSyncState()), new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(column);
        assertEquals(name, column.getName());

        final List<Column<Receipt>> columns = mPDFTable.get().blockingGet();
        assertEquals(columns, Arrays.asList(column, mColumn2));
    }

    @Test
    public void delete() {
        assertEquals(mColumn1, mPDFTable.delete(mColumn1, new DatabaseOperationMetadata()).blockingGet());
        assertEquals(mPDFTable.get().blockingGet(), Collections.singletonList(mColumn2));
    }

    @Test
    public void deleteLast() {
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        assertTrue(mPDFTable.deleteLast(databaseOperationMetadata).blockingGet());
        assertEquals(mPDFTable.get().blockingGet(), Collections.singletonList(mColumn1));
        assertTrue(mPDFTable.deleteLast(databaseOperationMetadata).blockingGet());
        assertEquals(mPDFTable.get().blockingGet(), Collections.emptyList());
        assertFalse(mPDFTable.deleteLast(databaseOperationMetadata).blockingGet());
    }

}
