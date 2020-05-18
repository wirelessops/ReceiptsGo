package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;

import android.text.TextUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.verification.VerificationMode;
import org.robolectric.RobolectricTestRunner;

import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderingPreferencesManager;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.core.sync.model.SyncState;
import co.smartreceipts.core.sync.model.impl.DefaultSyncState;
import co.smartreceipts.core.sync.model.impl.Identifier;
import co.smartreceipts.core.sync.model.impl.IdentifierMap;
import co.smartreceipts.core.sync.model.impl.MarkedForDeletionMap;
import co.smartreceipts.core.sync.model.impl.SyncStatusMap;
import co.smartreceipts.core.sync.provider.SyncProvider;
import io.reactivex.Single;
import wb.android.storage.StorageManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReceiptsTableTest {

    private static final double PRICE_1 = 12.55d;
    private static final String NAME_1 = "Name1";
    private static final int TRIP_ID_1 = 96;
    private static final Date DATE_1 = new Date(1200000000000L);
    private static final UUID UUID_1 = UUID.randomUUID();
    private static final double PRICE_2 = 140d;
    private static final String NAME_2 = "Name2";
    private static final int TRIP_ID_2 = 97;
    public static final Date DATE_2 = new Date(1300000000000L);
    private static final UUID UUID_2 = UUID.randomUUID();
    private static final double PRICE_3 = 12.123;
    private static final String NAME_3 = "Name3";
    private static final int TRIP_ID_3 = 98;
    public static final Date DATE_3 = new Date(1400000000000L);
    private static final UUID UUID_3 = UUID.randomUUID();

    private static final String CURRENCY_CODE = "USD";

    // Class under test
    ReceiptsTable mReceiptsTable;

    @Mock
    SQLiteDatabase mSQLiteDatabase;

    @Mock
    TableDefaultsCustomizer mTableDefaultsCustomizer;

    @Mock
    Table<Trip> mTripsTable;

    @Mock
    Table<PaymentMethod> mPaymentMethodTable;

    @Mock
    Table<Category> mCategoryTable;

    @Mock
    PersistenceManager mPersistenceManager;

    @Mock
    OrderingPreferencesManager orderingPreferencesManager;

    @Mock
    StorageManager mStorageManager;

    @Mock
    UserPreferenceManager mPreferences;

    @Mock
    Trip mTrip1;

    @Mock
    Trip mTrip2;

    @Mock
    Trip mTrip3;

    @Mock
    Category mCategory;

    @Mock
    PaymentMethod mPaymentMethod;

    @Captor
    ArgumentCaptor<String> mSqlCaptor;

    SQLiteOpenHelper mSQLiteOpenHelper;

    Receipt mReceipt1;

    Receipt mReceipt2;

    ReceiptBuilderFactory mBuilder;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(mTrip1.getId()).thenReturn(TRIP_ID_1);
        when(mTrip2.getId()).thenReturn(TRIP_ID_2);
        when(mTrip3.getId()).thenReturn(TRIP_ID_3);
        when(mTrip1.getDefaultCurrencyCode()).thenReturn(CURRENCY_CODE);
        when(mTrip2.getDefaultCurrencyCode()).thenReturn(CURRENCY_CODE);
        when(mTrip3.getDefaultCurrencyCode()).thenReturn(CURRENCY_CODE);
        when(mTrip1.getTripCurrency()).thenReturn(PriceCurrency.getInstance(CURRENCY_CODE));
        when(mTrip2.getTripCurrency()).thenReturn(PriceCurrency.getInstance(CURRENCY_CODE));
        when(mTrip3.getTripCurrency()).thenReturn(PriceCurrency.getInstance(CURRENCY_CODE));

        when(mTripsTable.findByPrimaryKey(TRIP_ID_1)).thenReturn(Single.just(mTrip1));
        when(mTripsTable.findByPrimaryKey(TRIP_ID_2)).thenReturn(Single.just(mTrip2));
        when(mTripsTable.findByPrimaryKey(TRIP_ID_3)).thenReturn(Single.just(mTrip3));

        when(mCategoryTable.findByPrimaryKey(anyInt())).thenReturn(Single.just(mCategory));
        when(mPaymentMethodTable.findByPrimaryKey(anyInt())).thenReturn(Single.just(mPaymentMethod));

        when(mPersistenceManager.getPreferenceManager()).thenReturn(mPreferences);
        when(mPersistenceManager.getStorageManager()).thenReturn(mStorageManager);
        when(mPreferences.get(UserPreference.General.DefaultCurrency)).thenReturn(CURRENCY_CODE);

        mSQLiteOpenHelper = new TestSQLiteOpenHelper(ApplicationProvider.getApplicationContext());
        mReceiptsTable = new ReceiptsTable(mSQLiteOpenHelper, mTripsTable, mPaymentMethodTable, mCategoryTable,
                mPersistenceManager.getStorageManager(), mPersistenceManager.getPreferenceManager(), orderingPreferencesManager);

        // Now create the table and insert some defaults
        mReceiptsTable.onCreate(mSQLiteOpenHelper.getWritableDatabase(), mTableDefaultsCustomizer);
        mBuilder = new ReceiptBuilderFactory();
        mBuilder.setCategory(mCategory)
                .setFile(null)
                .setDate(System.currentTimeMillis())
                .setTimeZone(TimeZone.getDefault())
                .setComment("")
                .setIsReimbursable(true)
                .setCurrency(CURRENCY_CODE)
                .setIsFullPage(false)
                .setPaymentMethod(mPaymentMethod)
                .setNameHiddenFromAutoComplete(false)
                .setCommentHiddenFromAutoComplete(false);
        mReceipt1 = mReceiptsTable.insert(mBuilder.setName(NAME_1).setPrice(PRICE_1).setTrip(mTrip1).setDate(DATE_1).setIndex(1).setUuid(UUID_1).build(),
                new DatabaseOperationMetadata()).blockingGet();
        mReceipt2 = mReceiptsTable.insert(mBuilder.setName(NAME_2).setPrice(PRICE_2).setTrip(mTrip2).setDate(DATE_2).setIndex(2).setUuid(UUID_2).build(),
                new DatabaseOperationMetadata()).blockingGet();
    }

    @After
    public void tearDown() {
        mSQLiteOpenHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + mReceiptsTable.getTableName());
    }

    @Test
    public void getTableName() {
        assertEquals("receipts", mReceiptsTable.getTableName());
    }

    @Test
    public void onCreate() {
        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mReceiptsTable.onCreate(mSQLiteDatabase, customizer);
        verify(mSQLiteDatabase).execSQL(mSqlCaptor.capture());
        verifyZeroInteractions(customizer);

        final String creatingTable = mSqlCaptor.getValue();
        assertTrue(creatingTable.contains("CREATE TABLE receipts"));
        assertTrue(creatingTable.contains("id INTEGER PRIMARY KEY AUTOINCREMENT"));
        assertTrue(creatingTable.contains("path TEXT"));
        assertTrue(creatingTable.contains("name TEXT"));
        assertTrue(creatingTable.contains("parentKey INTEGER"));
        assertTrue(creatingTable.contains("categoryKey INTEGER"));
        assertTrue(creatingTable.contains("price DECIMAL(10, 2)"));
        assertTrue(creatingTable.contains("tax DECIMAL(10, 2)"));
        assertTrue(creatingTable.contains("tax2 DECIMAL(10, 2)"));
        assertTrue(creatingTable.contains("exchange_rate DECIMAL(10, 10)"));
        assertTrue(creatingTable.contains("rcpt_date DATE"));
        assertTrue(creatingTable.contains("timezone TEXT"));
        assertTrue(creatingTable.contains("comment TEXT"));
        assertTrue(creatingTable.contains("expenseable BOOLEAN"));
        assertTrue(creatingTable.contains("isocode TEXT"));
        assertTrue(creatingTable.contains("paymentMethodKey INTEGER"));
        assertTrue(creatingTable.contains("fullpageimage BOOLEAN"));
        assertTrue(creatingTable.contains("receipt_processing_status TEXT"));
        assertTrue(creatingTable.contains("extra_edittext_1 TEXT"));
        assertTrue(creatingTable.contains("extra_edittext_2 TEXT"));
        assertTrue(creatingTable.contains("extra_edittext_3 TEXT"));
        assertTrue(creatingTable.contains("drive_sync_id TEXT"));
        assertTrue(creatingTable.contains("drive_is_synced BOOLEAN DEFAULT 0"));
        assertTrue(creatingTable.contains("drive_marked_for_deletion BOOLEAN DEFAULT 0"));
        assertTrue(creatingTable.contains("last_local_modification_time DATE"));
        assertTrue(creatingTable.contains("custom_order_id INTEGER DEFAULT 0"));
        assertTrue(creatingTable.contains("uuid TEXT"));
        assertTrue(creatingTable.contains("name_hidden_auto_complete BOOLEAN DEFAULT 0"));
        assertTrue(creatingTable.contains("comment_hidden_auto_complete BOOLEAN DEFAULT 0"));
    }

    @Test
    public void onUpgradeFromV1() {
        final int oldVersion = 1;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mReceiptsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV1Upgrade(times(1));
        verifyV3Upgrade(times(1));
        verifyV4Upgrade(times(1));
        verifyV7Upgrade(times(1));
        verifyV11Upgrade(times(1));
        verifyV12Upgrade(times(1));
        verifyV13Upgrade(times(1));
        verifyV14Upgrade(times(1));
        verifyV15Upgrade(times(1));
        verifyV18Upgrade(times(1));
        verifyV19Upgrade(times(1));
        verifyV20Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV3() {
        final int oldVersion = 3;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mReceiptsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV1Upgrade(never());
        verifyV3Upgrade(times(1));
        verifyV4Upgrade(times(1));
        verifyV7Upgrade(times(1));
        verifyV11Upgrade(times(1));
        verifyV12Upgrade(times(1));
        verifyV13Upgrade(times(1));
        verifyV14Upgrade(times(1));
        verifyV15Upgrade(times(1));
        verifyV18Upgrade(times(1));
        verifyV19Upgrade(times(1));
        verifyV20Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV4() {
        final int oldVersion = 4;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mReceiptsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV1Upgrade(never());
        verifyV3Upgrade(never());
        verifyV4Upgrade(times(1));
        verifyV7Upgrade(times(1));
        verifyV11Upgrade(times(1));
        verifyV12Upgrade(times(1));
        verifyV13Upgrade(times(1));
        verifyV14Upgrade(times(1));
        verifyV15Upgrade(times(1));
        verifyV18Upgrade(times(1));
        verifyV19Upgrade(times(1));
        verifyV20Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV7() {
        final int oldVersion = 7;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mReceiptsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV1Upgrade(never());
        verifyV3Upgrade(never());
        verifyV4Upgrade(never());
        verifyV7Upgrade(times(1));
        verifyV11Upgrade(times(1));
        verifyV12Upgrade(times(1));
        verifyV13Upgrade(times(1));
        verifyV14Upgrade(times(1));
        verifyV15Upgrade(times(1));
        verifyV18Upgrade(times(1));
        verifyV19Upgrade(times(1));
        verifyV20Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV11() {
        final int oldVersion = 11;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mReceiptsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV1Upgrade(never());
        verifyV3Upgrade(never());
        verifyV4Upgrade(never());
        verifyV7Upgrade(never());
        verifyV11Upgrade(times(1));
        verifyV12Upgrade(times(1));
        verifyV13Upgrade(times(1));
        verifyV14Upgrade(times(1));
        verifyV15Upgrade(times(1));
        verifyV18Upgrade(times(1));
        verifyV19Upgrade(times(1));
        verifyV20Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV12() {
        final int oldVersion = 12;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mReceiptsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV1Upgrade(never());
        verifyV3Upgrade(never());
        verifyV4Upgrade(never());
        verifyV7Upgrade(never());
        verifyV11Upgrade(never());
        verifyV12Upgrade(times(1));
        verifyV13Upgrade(times(1));
        verifyV14Upgrade(times(1));
        verifyV15Upgrade(times(1));
        verifyV18Upgrade(times(1));
        verifyV19Upgrade(times(1));
        verifyV20Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV13() {
        final int oldVersion = 13;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mReceiptsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV1Upgrade(never());
        verifyV3Upgrade(never());
        verifyV4Upgrade(never());
        verifyV7Upgrade(never());
        verifyV11Upgrade(never());
        verifyV12Upgrade(never());
        verifyV13Upgrade(times(1));
        verifyV14Upgrade(times(1));
        verifyV15Upgrade(times(1));
        verifyV18Upgrade(times(1));
        verifyV19Upgrade(times(1));
        verifyV20Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV14() {
        final int oldVersion = 14;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mReceiptsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV1Upgrade(never());
        verifyV3Upgrade(never());
        verifyV4Upgrade(never());
        verifyV7Upgrade(never());
        verifyV11Upgrade(never());
        verifyV12Upgrade(never());
        verifyV13Upgrade(never());
        verifyV14Upgrade(times(1));
        verifyV15Upgrade(times(1));
        verifyV18Upgrade(times(1));
        verifyV19Upgrade(times(1));
        verifyV20Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV15() {
        final int oldVersion = 15;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mReceiptsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV1Upgrade(never());
        verifyV3Upgrade(never());
        verifyV4Upgrade(never());
        verifyV7Upgrade(never());
        verifyV11Upgrade(never());
        verifyV12Upgrade(never());
        verifyV13Upgrade(never());
        verifyV14Upgrade(never());
        verifyV15Upgrade(times(1));
        verifyV18Upgrade(times(1));
        verifyV19Upgrade(times(1));
        verifyV20Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV18() {
        final int oldVersion = 18;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mReceiptsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV1Upgrade(never());
        verifyV3Upgrade(never());
        verifyV4Upgrade(never());
        verifyV7Upgrade(never());
        verifyV11Upgrade(never());
        verifyV12Upgrade(never());
        verifyV13Upgrade(never());
        verifyV14Upgrade(never());
        verifyV15Upgrade(never());
        verifyV18Upgrade(times(1));
        verifyV19Upgrade(times(1));
        verifyV20Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV19() {
        final int oldVersion = 19;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mReceiptsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV1Upgrade(never());
        verifyV3Upgrade(never());
        verifyV4Upgrade(never());
        verifyV7Upgrade(never());
        verifyV11Upgrade(never());
        verifyV12Upgrade(never());
        verifyV13Upgrade(never());
        verifyV14Upgrade(never());
        verifyV15Upgrade(never());
        verifyV18Upgrade(never());
        verifyV19Upgrade(times(1));
        verifyV20Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV20() {
        final int oldVersion = 20;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mReceiptsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV1Upgrade(never());
        verifyV3Upgrade(never());
        verifyV4Upgrade(never());
        verifyV7Upgrade(never());
        verifyV11Upgrade(never());
        verifyV12Upgrade(never());
        verifyV13Upgrade(never());
        verifyV14Upgrade(never());
        verifyV15Upgrade(never());
        verifyV18Upgrade(never());
        verifyV19Upgrade(never());
        verifyV20Upgrade(times(1));
    }

    @Test
    public void onUpgradeAlreadyOccurred() {
        final int oldVersion = DatabaseHelper.DATABASE_VERSION;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mReceiptsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV1Upgrade(never());
        verifyV3Upgrade(never());
        verifyV4Upgrade(never());
        verifyV7Upgrade(never());
        verifyV11Upgrade(never());
        verifyV12Upgrade(never());
        verifyV13Upgrade(never());
        verifyV14Upgrade(never());
        verifyV15Upgrade(never());
        verifyV18Upgrade(never());
        verifyV19Upgrade(never());
        verifyV20Upgrade(never());
    }

    private void verifyV1Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE receipts ADD isocode TEXT NOT NULL DEFAULT USD");
    }

    private void verifyV3Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE receipts ADD extra_edittext_1 TEXT");
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE receipts ADD extra_edittext_2 TEXT");
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE receipts ADD extra_edittext_3 TEXT");
    }

    private void verifyV4Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE receipts ADD tax DECIMAL(10, 2) DEFAULT 0.00");
    }

    private void verifyV7Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE receipts ADD timezone TEXT");
    }

    private void verifyV11Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE receipts ADD paymentMethodKey INTEGER REFERENCES paymentmethods ON DELETE NO ACTION");
    }

    private void verifyV12Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE receipts ADD receipt_processing_status TEXT");
    }

    private void verifyV13Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE receipts ADD exchange_rate DECIMAL(10, 10) DEFAULT -1.00");
    }

    private void verifyV14Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE " + mReceiptsTable.getTableName() + " ADD drive_sync_id TEXT");
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE " + mReceiptsTable.getTableName() + " ADD drive_is_synced BOOLEAN DEFAULT 0");
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE " + mReceiptsTable.getTableName() + " ADD drive_marked_for_deletion BOOLEAN DEFAULT 0");
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE " + mReceiptsTable.getTableName() + " ADD last_local_modification_time DATE");
    }

    private void verifyV15Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE " + mReceiptsTable.getTableName() +
                " ADD " + ReceiptsTable.COLUMN_CATEGORY_ID + " INTEGER REFERENCES " + CategoriesTable.TABLE_NAME + " ON DELETE NO ACTION");

        verify(mSQLiteDatabase, verificationMode).execSQL("UPDATE " + ReceiptsTable.TABLE_NAME + " SET " + ReceiptsTable.COLUMN_CATEGORY_ID +
                " = ( SELECT " + CategoriesTable.COLUMN_ID + " FROM " + CategoriesTable.TABLE_NAME +
                " WHERE " + CategoriesTable.COLUMN_NAME + " = category LIMIT 1 )");

        verify(mSQLiteDatabase, verificationMode).execSQL("CREATE TABLE " + ReceiptsTable.TABLE_NAME + "_copy" + " ("
                + ReceiptsTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ReceiptsTable.COLUMN_PATH + " TEXT, "
                + ReceiptsTable.COLUMN_PARENT + " TEXT REFERENCES " + TripsTable.TABLE_NAME + " ON DELETE CASCADE, "
                + ReceiptsTable.COLUMN_NAME + " TEXT DEFAULT \"New Receipt\", "
                + ReceiptsTable.COLUMN_CATEGORY_ID + " INTEGER REFERENCES " + CategoriesTable.TABLE_NAME + " ON DELETE NO ACTION, "
                + ReceiptsTable.COLUMN_DATE + " DATE DEFAULT (DATE('now', 'localtime')), "
                + ReceiptsTable.COLUMN_TIMEZONE + " TEXT, "
                + ReceiptsTable.COLUMN_COMMENT + " TEXT, "
                + ReceiptsTable.COLUMN_ISO4217 + " TEXT NOT NULL, "
                + ReceiptsTable.COLUMN_PRICE + " DECIMAL(10, 2) DEFAULT 0.00, "
                + ReceiptsTable.COLUMN_TAX + " DECIMAL(10, 2) DEFAULT 0.00, "
                + ReceiptsTable.COLUMN_EXCHANGE_RATE + " DECIMAL(10, 10) DEFAULT -1.00, "
                + ReceiptsTable.COLUMN_PAYMENT_METHOD_ID + " INTEGER REFERENCES " + PaymentMethodsTable.TABLE_NAME + " ON DELETE NO ACTION, "
                + ReceiptsTable.COLUMN_REIMBURSABLE + " BOOLEAN DEFAULT 1, "
                + ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE + " BOOLEAN DEFAULT 1, "
                + ReceiptsTable.COLUMN_PROCESSING_STATUS + " TEXT, "
                + ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1 + " TEXT, "
                + ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2 + " TEXT, "
                + ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3 + " TEXT, "
                + AbstractSqlTable.COLUMN_DRIVE_SYNC_ID + " TEXT, "
                + AbstractSqlTable.COLUMN_DRIVE_IS_SYNCED + " BOOLEAN DEFAULT 0, "
                + AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION + " BOOLEAN DEFAULT 0, "
                + AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME + " DATE, "
                + AbstractSqlTable.COLUMN_CUSTOM_ORDER_ID + " INTEGER DEFAULT 0"
                + ");");

        final String finalColumns = TextUtils.join(", ", new String[]{
                ReceiptsTable.COLUMN_ID, ReceiptsTable.COLUMN_PATH, ReceiptsTable.COLUMN_PARENT,
                ReceiptsTable.COLUMN_NAME, ReceiptsTable.COLUMN_CATEGORY_ID, ReceiptsTable.COLUMN_DATE,
                ReceiptsTable.COLUMN_TIMEZONE, ReceiptsTable.COLUMN_COMMENT, ReceiptsTable.COLUMN_ISO4217,
                ReceiptsTable.COLUMN_PRICE, ReceiptsTable.COLUMN_TAX, ReceiptsTable.COLUMN_EXCHANGE_RATE,
                ReceiptsTable.COLUMN_PAYMENT_METHOD_ID, ReceiptsTable.COLUMN_REIMBURSABLE,
                ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE, ReceiptsTable.COLUMN_PROCESSING_STATUS,
                ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1, ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2,
                ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3, AbstractSqlTable.COLUMN_DRIVE_SYNC_ID,
                AbstractSqlTable.COLUMN_DRIVE_IS_SYNCED, AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION,
                AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME});

        verify(mSQLiteDatabase, atLeast(0)).execSQL("INSERT INTO " + ReceiptsTable.TABLE_NAME + "_copy" + " ( " + finalColumns + " ) "
                + "SELECT " + finalColumns
                + " FROM " + ReceiptsTable.TABLE_NAME + " ;");

        verify(mSQLiteDatabase, atLeast(0)).execSQL("DROP TABLE " + ReceiptsTable.TABLE_NAME + ";");

        verify(mSQLiteDatabase, atLeast(0)).execSQL("ALTER TABLE " + ReceiptsTable.TABLE_NAME + "_copy" + " RENAME TO " + ReceiptsTable.TABLE_NAME + ";");
    }
    
    private void verifyV18Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL(String.format("ALTER TABLE %s ADD %s INTEGER REFERENCES %s ON DELETE CASCADE",
                ReceiptsTable.TABLE_NAME, ReceiptsTable.COLUMN_PARENT_TRIP_ID, TripsTable.TABLE_NAME));

        verify(mSQLiteDatabase, verificationMode).execSQL(String.format("UPDATE %s SET %s = ( SELECT %s FROM %s WHERE %s = %s LIMIT 1 )",
                ReceiptsTable.TABLE_NAME, ReceiptsTable.COLUMN_PARENT_TRIP_ID, TripsTable.COLUMN_ID, TripsTable.TABLE_NAME,
                TripsTable.COLUMN_NAME, ReceiptsTable.COLUMN_PARENT));

        verify(mSQLiteDatabase, verificationMode).execSQL("CREATE TABLE " + ReceiptsTable.TABLE_NAME + "_copy" + " ("
                + ReceiptsTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ReceiptsTable.COLUMN_PATH + " TEXT, "
                + ReceiptsTable.COLUMN_PARENT_TRIP_ID + " INTEGER REFERENCES " + TripsTable.TABLE_NAME + " ON DELETE CASCADE, "
                + ReceiptsTable.COLUMN_NAME + " TEXT DEFAULT \"New Receipt\", "
                + ReceiptsTable.COLUMN_CATEGORY_ID + " INTEGER REFERENCES " + CategoriesTable.TABLE_NAME + " ON DELETE NO ACTION, "
                + ReceiptsTable.COLUMN_DATE + " DATE DEFAULT (DATE('now', 'localtime')), "
                + ReceiptsTable.COLUMN_TIMEZONE + " TEXT, "
                + ReceiptsTable.COLUMN_COMMENT + " TEXT, "
                + ReceiptsTable.COLUMN_ISO4217 + " TEXT NOT NULL, "
                + ReceiptsTable.COLUMN_PRICE + " DECIMAL(10, 2) DEFAULT 0.00, "
                + ReceiptsTable.COLUMN_TAX + " DECIMAL(10, 2) DEFAULT 0.00, "
                + ReceiptsTable.COLUMN_EXCHANGE_RATE + " DECIMAL(10, 10) DEFAULT -1.00, "
                + ReceiptsTable.COLUMN_PAYMENT_METHOD_ID + " INTEGER REFERENCES " + PaymentMethodsTable.TABLE_NAME + " ON DELETE NO ACTION, "
                + ReceiptsTable.COLUMN_REIMBURSABLE + " BOOLEAN DEFAULT 1, "
                + ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE + " BOOLEAN DEFAULT 1, "
                + ReceiptsTable.COLUMN_PROCESSING_STATUS + " TEXT, "
                + ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1 + " TEXT, "
                + ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2 + " TEXT, "
                + ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3 + " TEXT, "
                + AbstractSqlTable.COLUMN_DRIVE_SYNC_ID + " TEXT, "
                + AbstractSqlTable.COLUMN_DRIVE_IS_SYNCED + " BOOLEAN DEFAULT 0, "
                + AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION + " BOOLEAN DEFAULT 0, "
                + AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME + " DATE, "
                + AbstractSqlTable.COLUMN_CUSTOM_ORDER_ID + " INTEGER DEFAULT 0"
                + ");");

        final String finalColumns = TextUtils.join(",", new String[]{
                ReceiptsTable.COLUMN_ID, ReceiptsTable.COLUMN_PATH, ReceiptsTable.COLUMN_PARENT_TRIP_ID, ReceiptsTable.COLUMN_NAME,
                ReceiptsTable.COLUMN_CATEGORY_ID, ReceiptsTable.COLUMN_DATE, ReceiptsTable.COLUMN_TIMEZONE, ReceiptsTable.COLUMN_COMMENT,
                ReceiptsTable.COLUMN_ISO4217, ReceiptsTable.COLUMN_PRICE, ReceiptsTable.COLUMN_TAX, ReceiptsTable.COLUMN_EXCHANGE_RATE,
                ReceiptsTable.COLUMN_PAYMENT_METHOD_ID, ReceiptsTable.COLUMN_REIMBURSABLE, ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE,
                ReceiptsTable.COLUMN_PROCESSING_STATUS, ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1, ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2,
                ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3, AbstractSqlTable.COLUMN_DRIVE_SYNC_ID, AbstractSqlTable.COLUMN_DRIVE_IS_SYNCED,
                AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION, AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME});


        verify(mSQLiteDatabase, atLeast(0)).execSQL("INSERT INTO " + ReceiptsTable.TABLE_NAME + "_copy" + " ( " + finalColumns + " ) "
                + "SELECT " + finalColumns
                + " FROM " + ReceiptsTable.TABLE_NAME + " ;");

        verify(mSQLiteDatabase, atLeast(0)).execSQL("DROP TABLE " + ReceiptsTable.TABLE_NAME + ";");

        verify(mSQLiteDatabase, atLeast(0)).execSQL("ALTER TABLE " + ReceiptsTable.TABLE_NAME + "_copy" + " RENAME TO " + ReceiptsTable.TABLE_NAME + ";");
    }

    private void verifyV19Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_NAME_HIDDEN_AUTO_COMPLETE + " BOOLEAN DEFAULT 0");
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_COMMENT_HIDDEN_AUTO_COMPLETE + " BOOLEAN DEFAULT 0");
    }

    private void verifyV20Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_TAX2 + " DECIMAL(10, 2) DEFAULT 0.00");
    }

    @Test
    public void get() {
        final List<Receipt> receipts = mReceiptsTable.get().blockingGet();
        assertEquals(receipts, Arrays.asList(mReceipt2, mReceipt1)); // Note: The receipt with the more recent date (ie 2) appears first
    }

    @Test
    public void getForTrip() {
        // Note: We're adding this one to trip 1
        final Receipt receiptToInsert = mBuilder.setName(NAME_3).setPrice(PRICE_3).setTrip(mTrip1).setDate(DATE_3).setIndex(2).build();
        final Receipt receipt = mReceiptsTable.insert(receiptToInsert, new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(receipt);

        final List<Receipt> list1 = mReceiptsTable.get(mTrip1).blockingGet();
        final List<Receipt> list2 = mReceiptsTable.get(mTrip2).blockingGet();
        final List<Receipt> list3 = mReceiptsTable.get(mTrip3).blockingGet();
        assertEquals(list1, Arrays.asList(receipt, mReceipt1)); // Note: The receipt with the more recent date appears first
        assertEquals(list2, Collections.singletonList(new ReceiptBuilderFactory(mReceipt2).setIndex(1).build()));
        assertEquals(list3, Collections.<Receipt>emptyList());
    }

    @Test
    public void getUnsynced() {
        final SyncState syncStateForSyncedReceipt = new DefaultSyncState(new IdentifierMap(Collections.singletonMap(SyncProvider.GoogleDrive, new Identifier("id"))),
                new SyncStatusMap(Collections.singletonMap(SyncProvider.GoogleDrive, true)),
                new MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, false)),
                new Date(System.currentTimeMillis()));
        final Receipt receipt = mReceiptsTable.insert(mBuilder.setName(NAME_3).setPrice(PRICE_3).setTrip(mTrip1).setDate(DATE_3).setIndex(3).setSyncState(syncStateForSyncedReceipt).build(), new DatabaseOperationMetadata(OperationFamilyType.Sync)).blockingGet();
        assertNotNull(receipt);

        final List<Receipt> list1 = mReceiptsTable.getUnsynced(mTrip1, SyncProvider.GoogleDrive).blockingGet();
        assertEquals(list1, Collections.singletonList(mReceipt1));
    }

    @Test
    public void insert() {
        final Receipt receipt = mReceiptsTable.insert(mBuilder.setName(NAME_3).setPrice(PRICE_3).setTrip(mTrip3).setDate(DATE_3).setIndex(3).setUuid(UUID_3).build(),
                new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(receipt);

        final List<Receipt> receipts = mReceiptsTable.get().blockingGet();
        assertEquals(receipts, Arrays.asList(receipt, mReceipt2, mReceipt1)); // Note: The receipt with the more recent date appears first
    }

    @Test
    public void findByPrimaryKey() {
        mReceiptsTable.findByPrimaryKey(mReceipt1.getId())
                .test()
                .assertNoErrors()
                .assertResult(mReceipt1);
    }

    @Test
    public void findByPrimaryKeyAfterCaching() {
        final List<Receipt> list1 = mReceiptsTable.get(mTrip1).blockingGet();
        assertEquals(list1, Collections.singletonList(mReceipt1));
        mReceiptsTable.findByPrimaryKey(mReceipt1.getId())
                .test()
                .assertNoErrors()
                .assertResult(mReceipt1);
    }

    @Test
    public void findByPrimaryMissingKey() {
        mReceiptsTable.findByPrimaryKey(-1)
                .test()
                .assertError(Exception.class);
    }

    @Test
    public void update() {
        final Receipt updatedReceipt = mReceiptsTable.update(mReceipt1,
                mBuilder.setName(NAME_3).setPrice(PRICE_3).setTrip(mTrip3).setDate(DATE_3).setIndex(2).setUuid(UUID_3).build(),
                new DatabaseOperationMetadata()).blockingGet();

        assertNotNull(updatedReceipt);
        assertNotEquals(mReceipt1, updatedReceipt);
        assertEquals(UUID_1, updatedReceipt.getUuid());

        final List<Receipt> receipts = mReceiptsTable.get().blockingGet();
        assertEquals(receipts, Arrays.asList(updatedReceipt, new ReceiptBuilderFactory(mReceipt2).setIndex(1).build())); // Note: The receipt with the more recent date appears first
    }

    @Test
    public void updateWithOlderDate() {
        final Receipt updatedReceipt = mReceiptsTable.update(mReceipt1, mBuilder.setName(NAME_3).setPrice(PRICE_3).setTrip(mTrip3).setDate(DATE_1).setIndex(1).build(), new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(updatedReceipt);
        assertNotEquals(mReceipt1, updatedReceipt);

        final List<Receipt> receipts = mReceiptsTable.get().blockingGet();
        assertEquals(receipts, Arrays.asList(mReceipt2, updatedReceipt)); // Note: The receipt with the more recent date appears first
    }

    @Test
    public void getAllMarkedReceipts() {
        final SyncState syncState = new DefaultSyncState(new IdentifierMap(Collections.singletonMap(SyncProvider.GoogleDrive, new Identifier("id"))),
                new SyncStatusMap(Collections.singletonMap(SyncProvider.GoogleDrive, false)),
                new MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, false)),
                new Date(System.currentTimeMillis()));
        final Receipt receipt = mReceiptsTable.insert(mBuilder.setName(NAME_3).setPrice(PRICE_3).setTrip(mTrip3).setSyncState(syncState).build(), new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(receipt);

        assertEquals(receipt, mReceiptsTable.delete(receipt, new DatabaseOperationMetadata()).blockingGet());

        final List<Receipt> markedForDeletionReceipts = mReceiptsTable.getAllMarkedForDeletionItems(SyncProvider.GoogleDrive).blockingGet();
        assertEquals(markedForDeletionReceipts, Collections.singletonList(new ReceiptBuilderFactory(receipt).setIndex(1).build()));
    }

    @Test
    public void deleteUnmarkedReceipt() {
        assertEquals(mReceipt1, mReceiptsTable.delete(mReceipt1, new DatabaseOperationMetadata()).blockingGet());

        final List<Receipt> receipts = mReceiptsTable.get().blockingGet();
        assertEquals(receipts, Collections.singletonList(new ReceiptBuilderFactory(mReceipt2).setIndex(1).build()));

        final List<Receipt> tripReceipts = mReceiptsTable.get(mTrip1).blockingGet();
        assertEquals(tripReceipts, Collections.<Receipt>emptyList());
    }

    @Test
    public void deleteUnmarkedReceiptAfterCaching() {
        mReceiptsTable.get(mTrip1).blockingGet();
        mReceiptsTable.get(mTrip2).blockingGet();
        mReceiptsTable.get(mTrip3).blockingGet();

        assertEquals(mReceipt1, mReceiptsTable.delete(mReceipt1, new DatabaseOperationMetadata()).blockingGet());

        final List<Receipt> receipts = mReceiptsTable.get().blockingGet();
        assertEquals(receipts, Collections.singletonList(new ReceiptBuilderFactory(mReceipt2).setIndex(1).build()));

        final List<Receipt> tripReceipts = mReceiptsTable.get(mTrip1).blockingGet();
        assertEquals(tripReceipts, Collections.<Receipt>emptyList());
    }

    @Test
    public void deleteMarkedReceipt() {
        final SyncState syncState = new DefaultSyncState(new IdentifierMap(Collections.singletonMap(SyncProvider.GoogleDrive, new Identifier("id"))),
                new SyncStatusMap(Collections.singletonMap(SyncProvider.GoogleDrive, false)),
                new MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, true)),
                new Date(System.currentTimeMillis()));
        final Receipt receipt = mReceiptsTable.insert(mBuilder.setName(NAME_3).setPrice(PRICE_3).setTrip(mTrip3).setSyncState(syncState).build(), new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(receipt);

        assertEquals(receipt, mReceiptsTable.delete(receipt, new DatabaseOperationMetadata()).blockingGet());

        final List<Receipt> receipts = mReceiptsTable.get().blockingGet();
        assertEquals(receipts, Arrays.asList(mReceipt2, mReceipt1));

        final List<Receipt> trip1Receipts = mReceiptsTable.get(mTrip1).blockingGet();
        assertEquals(trip1Receipts, Collections.singletonList(mReceipt1));

        final List<Receipt> trip3Receipts = mReceiptsTable.get(mTrip3).blockingGet();
        assertEquals(trip3Receipts, Collections.<Receipt>emptyList());
    }

    @Test
    public void deleteMarkedReceiptAfterCaching() {
        mReceiptsTable.get(mTrip1).blockingGet();
        mReceiptsTable.get(mTrip2).blockingGet();
        mReceiptsTable.get(mTrip3).blockingGet();

        final SyncState syncState = new DefaultSyncState(new IdentifierMap(Collections.singletonMap(SyncProvider.GoogleDrive, new Identifier("id"))),
                new SyncStatusMap(Collections.singletonMap(SyncProvider.GoogleDrive, false)),
                new MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, true)),
                new Date(System.currentTimeMillis()));
        final Receipt receipt = mReceiptsTable.insert(mBuilder.setName(NAME_3).setPrice(PRICE_3).setTrip(mTrip3).setSyncState(syncState).build(), new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(receipt);

        assertEquals(receipt, mReceiptsTable.delete(receipt, new DatabaseOperationMetadata()).blockingGet());

        final List<Receipt> receipts = mReceiptsTable.get().blockingGet();
        assertEquals(receipts, Arrays.asList(mReceipt2, mReceipt1));

        final List<Receipt> trip1Receipts = mReceiptsTable.get(mTrip1).blockingGet();
        assertEquals(trip1Receipts, Collections.singletonList(mReceipt1));

        final List<Receipt> trip3Receipts = mReceiptsTable.get(mTrip3).blockingGet();
        assertEquals(trip3Receipts, Collections.<Receipt>emptyList());
    }

}