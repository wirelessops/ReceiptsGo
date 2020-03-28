package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import co.smartreceipts.android.date.DateFormatter;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptCategoryNameColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptNameColumn;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptPriceColumn;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderingPreferencesManager;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.core.sync.model.impl.DefaultSyncState;
import co.smartreceipts.android.workers.reports.ReportResourcesManager;

import static co.smartreceipts.android.persistence.database.tables.AbstractColumnTable.COLUMN_ID;
import static co.smartreceipts.android.persistence.database.tables.AbstractColumnTable.COLUMN_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class CSVTableTest {

    // Class Under Test
    CSVTable csvTable;

    @Mock
    ReportResourcesManager reportResourcesManager;

    @Mock
    UserPreferenceManager preferences;

    @Mock
    DateFormatter dateFormatter;

    @Mock
    SQLiteDatabase database;

    @Mock
    TableDefaultsCustomizer tableDefaultsCustomizer;

    @Mock
    OrderingPreferencesManager orderingPreferencesManager;

    SQLiteOpenHelper sqliteOpenHelper;

    @Captor
    ArgumentCaptor<String> sqlCaptor;

    Column<Receipt> receiptNameColumn;
    Column<Receipt> receiptPriceColumn;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        sqliteOpenHelper = new TestSQLiteOpenHelper(ApplicationProvider.getApplicationContext());
        final ReceiptColumnDefinitions receiptColumnDefinitions = new ReceiptColumnDefinitions(reportResourcesManager, preferences, dateFormatter);
        csvTable = new CSVTable(sqliteOpenHelper, receiptColumnDefinitions, orderingPreferencesManager);

        // Now create the table and insert some defaults
        csvTable.onCreate(sqliteOpenHelper.getWritableDatabase(), tableDefaultsCustomizer);
        receiptNameColumn = csvTable.insert(new ReceiptNameColumn(-1, new DefaultSyncState(), 0, UUID.randomUUID()), new DatabaseOperationMetadata()).blockingGet();
        receiptPriceColumn = csvTable.insert(new ReceiptPriceColumn(-1, new DefaultSyncState(), 0, UUID.randomUUID()), new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(receiptNameColumn);
        assertNotNull(receiptPriceColumn);
    }

    @After
    public void tearDown() {
        sqliteOpenHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + csvTable.getTableName());
    }

    @Test
    public void getTableName() {
        assertEquals("csvcolumns", csvTable.getTableName());
    }

    @Test
    public void onCreate() {
        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        csvTable.onCreate(database, customizer);
        verify(database).execSQL(sqlCaptor.capture());
        verify(customizer).insertCSVDefaults(csvTable);

        assertTrue(sqlCaptor.getValue().contains("CREATE TABLE csvcolumns"));
        assertTrue(sqlCaptor.getValue().contains("id INTEGER PRIMARY KEY AUTOINCREMENT"));
        assertTrue(sqlCaptor.getValue().contains("column_type INTEGER DEFAULT 0"));
        assertTrue(sqlCaptor.getValue().contains("drive_sync_id TEXT"));
        assertTrue(sqlCaptor.getValue().contains("drive_is_synced BOOLEAN"));
        assertTrue(sqlCaptor.getValue().contains("drive_marked_for_deletion BOOLEAN"));
        assertTrue(sqlCaptor.getValue().contains("last_local_modification_time DATE"));
        assertTrue(sqlCaptor.getValue().contains("custom_order_id INTEGER DEFAULT 0"));
        assertTrue(sqlCaptor.getValue().contains("entity_uuid TEXT"));
    }

    @Test
    public void onUpgradeFromV2() {
        final int oldVersion = 2;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        csvTable.onUpgrade(database, oldVersion, newVersion, customizer);
        verify(database, atLeastOnce()).execSQL(sqlCaptor.capture());
        verify(customizer).insertCSVDefaults(csvTable);

        assertTrue(sqlCaptor.getAllValues().get(0).contains(CSVTable.TABLE_NAME));
        assertTrue(sqlCaptor.getAllValues().get(0).contains(CSVTable.DEPRECATED_COLUMN_ID_AS_NAME));
        assertTrue(sqlCaptor.getAllValues().get(0).contains(CSVTable.DEPRECATED_COLUMN_TYPE_AS_NAME));
        assertEquals(sqlCaptor.getAllValues().get(0), "CREATE TABLE csvcolumns (id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT);");
        assertEquals(sqlCaptor.getAllValues().get(1), "ALTER TABLE " + csvTable.getTableName() + " ADD drive_sync_id TEXT");
        assertEquals(sqlCaptor.getAllValues().get(2), "ALTER TABLE " + csvTable.getTableName() + " ADD drive_is_synced BOOLEAN DEFAULT 0");
        assertEquals(sqlCaptor.getAllValues().get(3), "ALTER TABLE " + csvTable.getTableName() + " ADD drive_marked_for_deletion BOOLEAN DEFAULT 0");
        assertEquals(sqlCaptor.getAllValues().get(4), "ALTER TABLE " + csvTable.getTableName() + " ADD last_local_modification_time DATE");
    }

    @Test
    public void onUpgradeFromV14() {
        final int oldVersion = 14;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        csvTable.onUpgrade(database, oldVersion, newVersion, customizer);
        verify(database, atLeastOnce()).execSQL(sqlCaptor.capture());
        verify(customizer, never()).insertCSVDefaults(csvTable);

        assertEquals(sqlCaptor.getAllValues().get(0), "ALTER TABLE " + csvTable.getTableName() + " ADD drive_sync_id TEXT");
        assertEquals(sqlCaptor.getAllValues().get(1), "ALTER TABLE " + csvTable.getTableName() + " ADD drive_is_synced BOOLEAN DEFAULT 0");
        assertEquals(sqlCaptor.getAllValues().get(2), "ALTER TABLE " + csvTable.getTableName() + " ADD drive_marked_for_deletion BOOLEAN DEFAULT 0");
        assertEquals(sqlCaptor.getAllValues().get(3), "ALTER TABLE " + csvTable.getTableName() + " ADD last_local_modification_time DATE");
    }

    @Test
    public void onUpgradeFromV15() {
        final int oldVersion = 15;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        csvTable.onUpgrade(database, oldVersion, newVersion, customizer);
        verify(database, atLeastOnce()).execSQL(sqlCaptor.capture());
        verify(customizer, never()).insertCSVDefaults(csvTable);

        assertEquals(sqlCaptor.getAllValues().get(0), "ALTER TABLE " + csvTable.getTableName() + " ADD COLUMN custom_order_id INTEGER DEFAULT 0;");
    }

    @Test
    public void onUpgradeFromV17() {
        final int oldVersion = 17;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        csvTable.onUpgrade(database, oldVersion, newVersion, customizer);
        verify(database, atLeastOnce()).execSQL(sqlCaptor.capture());
        verify(customizer, never()).insertCSVDefaults(csvTable);

        assertEquals(sqlCaptor.getAllValues().get(0), String.format("ALTER TABLE %s ADD COLUMN %s INTEGER DEFAULT 0;", csvTable.getTableName(), AbstractColumnTable.COLUMN_TYPE));

        assertEquals(sqlCaptor.getAllValues().get(1), String.format("ALTER TABLE %s RENAME TO %s;", csvTable.getTableName(), csvTable.getTableName() + "_tmp"));

        final String createNewTable = "CREATE TABLE " + csvTable.getTableName() + " ("
                + AbstractColumnTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AbstractColumnTable.COLUMN_TYPE + " INTEGER DEFAULT 0, "
                + AbstractSqlTable.COLUMN_DRIVE_SYNC_ID + " TEXT, "
                + AbstractSqlTable.COLUMN_DRIVE_IS_SYNCED + " BOOLEAN DEFAULT 0, "
                + AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION + " BOOLEAN DEFAULT 0, "
                + AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME + " DATE, "
                + AbstractSqlTable.COLUMN_CUSTOM_ORDER_ID + " INTEGER DEFAULT 0"
                + ");";
        assertEquals(sqlCaptor.getAllValues().get(2), createNewTable);

        final String baseColumns = String.format("%s, %s, %s, %s, %s, %s", COLUMN_TYPE, AbstractSqlTable.COLUMN_DRIVE_SYNC_ID,
                AbstractSqlTable.COLUMN_DRIVE_IS_SYNCED, AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION,
                AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME,
                AbstractSqlTable.COLUMN_CUSTOM_ORDER_ID);
        final String insertData = "INSERT INTO " + csvTable.getTableName()
                + " (" + COLUMN_ID + ", " + baseColumns + ") "
                + "SELECT " + AbstractColumnTable.DEPRECATED_COLUMN_ID_AS_NAME + ", " + baseColumns
                + " FROM " + csvTable.getTableName() + "_tmp" + ";";
        assertEquals(sqlCaptor.getAllValues().get(3), insertData);

        assertEquals(sqlCaptor.getAllValues().get(4), "DROP TABLE " + csvTable.getTableName() + "_tmp" + ";");
    }

    @Test
    public void onUpgradeFromV18() {
        final int oldVersion = 18;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        csvTable.onUpgrade(database, oldVersion, newVersion, customizer);
        verify(database, atLeastOnce()).execSQL(sqlCaptor.capture());
        verify(customizer, never()).insertCSVDefaults(csvTable);

        List<String> allValues = sqlCaptor.getAllValues();
        assertEquals(1, allValues.size());

        assertEquals(allValues.get(0), String.format("ALTER TABLE %s ADD entity_uuid TEXT", CSVTable.TABLE_NAME));
    }

    @Test
    public void onUpgradeAlreadyOccurred() {
        final int oldVersion = DatabaseHelper.DATABASE_VERSION;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        csvTable.onUpgrade(database, oldVersion, newVersion, customizer);
        verify(database, never()).execSQL(sqlCaptor.capture());
        verify(customizer, never()).insertCSVDefaults(csvTable);
    }

    @Test
    public void get() {
        final List<Column<Receipt>> columns = csvTable.get().blockingGet();
        assertEquals(columns, Arrays.asList(receiptNameColumn, receiptPriceColumn));
    }

    @Test
    public void findByPrimaryKey() {
        csvTable.findByPrimaryKey(receiptNameColumn.getId())
                .test()
                .assertNoErrors()
                .assertValue(receiptNameColumn);
    }

    @Test
    public void findByPrimaryMissingKey() {
        csvTable.findByPrimaryKey(-1)
                .test()
                .assertError(Exception.class);
    }

    @Test
    public void insert() {
        final UUID uuid = UUID.randomUUID();
        final Column<Receipt> column = csvTable.insert(new ReceiptCategoryNameColumn(-1,
                new DefaultSyncState(), 0, uuid), new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(column);

        assertEquals(ReceiptColumnDefinitions.ActualDefinition.CATEGORY_NAME.getColumnType(), column.getType());
        assertEquals(ReceiptColumnDefinitions.ActualDefinition.CATEGORY_NAME.getColumnHeaderId(), column.getHeaderStringResId());
        assertEquals(uuid, column.getUuid());

        final List<Column<Receipt>> columns = csvTable.get().blockingGet();
        assertEquals(columns, Arrays.asList(receiptNameColumn, receiptPriceColumn, column));
    }

    @Test
    public void update() {
        final UUID oldUuid = receiptNameColumn.getUuid();

        final Column<Receipt> column = csvTable.update(receiptNameColumn,
                new ReceiptCategoryNameColumn(-1, new DefaultSyncState(), 0, UUID.randomUUID()),
                new DatabaseOperationMetadata())
                .blockingGet();
        assertNotNull(column);
        assertEquals(ReceiptColumnDefinitions.ActualDefinition.CATEGORY_NAME.getColumnType(), column.getType());
        assertEquals(oldUuid, column.getUuid());

        final List<Column<Receipt>> columns = csvTable.get().blockingGet();
        assertEquals(columns, Arrays.asList(column, receiptPriceColumn));
    }

    @Test
    public void delete() {
        final Column<Receipt> deletedColumn = csvTable.delete(receiptNameColumn, new DatabaseOperationMetadata()).blockingGet();
        assertEquals(receiptNameColumn, deletedColumn);
        assertEquals(csvTable.get().blockingGet(), Collections.singletonList(receiptPriceColumn));
    }

}
