package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

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
import org.robolectric.RuntimeEnvironment;

import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

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
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;
import co.smartreceipts.android.sync.model.impl.Identifier;
import co.smartreceipts.android.sync.model.impl.IdentifierMap;
import co.smartreceipts.android.sync.model.impl.MarkedForDeletionMap;
import co.smartreceipts.android.sync.model.impl.SyncStatusMap;
import co.smartreceipts.android.sync.provider.SyncProvider;
import io.reactivex.Single;
import wb.android.storage.StorageManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
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
    private static final String TRIP_1 = "Trip";
    public static final Date DATE_1 = new Date(1200000000000L);
    private static final double PRICE_2 = 140d;
    private static final String NAME_2 = "Name2";
    private static final String TRIP_2 = "Trip2";
    public static final Date DATE_2 = new Date(1300000000000L);
    private static final double PRICE_3 = 12.123;
    private static final String NAME_3 = "Name3";
    private static final String TRIP_3 = "Trip3";
    public static final Date DATE_3 = new Date(1400000000000L);

    private static final String CURRENCY_CODE = "USD";

    // Class under test
    ReceiptsTable mReceiptsTable;

    @Mock
    SQLiteDatabase mSQLiteDatabase;

    @Mock
    TableDefaultsCustomizer mTableDefaultsCustomizer;

    @Mock
    Table<Trip, String> mTripsTable;

    @Mock
    Table<PaymentMethod, Integer> mPaymentMethodTable;

    @Mock
    Table<Category, Integer> mCategoryTable;

    @Mock
    PersistenceManager mPersistenceManager;

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

        when(mTrip1.getName()).thenReturn(TRIP_1);
        when(mTrip2.getName()).thenReturn(TRIP_2);
        when(mTrip3.getName()).thenReturn(TRIP_3);
        when(mTrip1.getDefaultCurrencyCode()).thenReturn(CURRENCY_CODE);
        when(mTrip2.getDefaultCurrencyCode()).thenReturn(CURRENCY_CODE);
        when(mTrip3.getDefaultCurrencyCode()).thenReturn(CURRENCY_CODE);
        when(mTrip1.getTripCurrency()).thenReturn(PriceCurrency.getInstance(CURRENCY_CODE));
        when(mTrip2.getTripCurrency()).thenReturn(PriceCurrency.getInstance(CURRENCY_CODE));
        when(mTrip3.getTripCurrency()).thenReturn(PriceCurrency.getInstance(CURRENCY_CODE));

        when(mTripsTable.findByPrimaryKey(TRIP_1)).thenReturn(Single.just(mTrip1));
        when(mTripsTable.findByPrimaryKey(TRIP_2)).thenReturn(Single.just(mTrip2));
        when(mTripsTable.findByPrimaryKey(TRIP_3)).thenReturn(Single.just(mTrip3));

        when(mCategoryTable.findByPrimaryKey(anyInt())).thenReturn(Single.just(mCategory));
        when(mPaymentMethodTable.findByPrimaryKey(anyInt())).thenReturn(Single.just(mPaymentMethod));

        when(mPersistenceManager.getPreferenceManager()).thenReturn(mPreferences);
        when(mPersistenceManager.getStorageManager()).thenReturn(mStorageManager);
        when(mPreferences.get(UserPreference.General.DefaultCurrency)).thenReturn(CURRENCY_CODE);

        mSQLiteOpenHelper = new TestSQLiteOpenHelper(RuntimeEnvironment.application);
        mReceiptsTable = new ReceiptsTable(mSQLiteOpenHelper, mTripsTable, mPaymentMethodTable, mCategoryTable,
                mPersistenceManager.getStorageManager(), mPersistenceManager.getPreferenceManager());

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
                .setPaymentMethod(mPaymentMethod);
        mReceipt1 = mReceiptsTable.insert(mBuilder.setName(NAME_1).setPrice(PRICE_1).setTrip(mTrip1).setDate(DATE_1).setIndex(1).build(), new DatabaseOperationMetadata()).blockingGet();
        mReceipt2 = mReceiptsTable.insert(mBuilder.setName(NAME_2).setPrice(PRICE_2).setTrip(mTrip2).setDate(DATE_2).setIndex(2).build(), new DatabaseOperationMetadata()).blockingGet();
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

        assertTrue(mSqlCaptor.getValue().contains("CREATE TABLE receipts"));
        assertTrue(mSqlCaptor.getValue().contains("id INTEGER PRIMARY KEY AUTOINCREMENT"));
        assertTrue(mSqlCaptor.getValue().contains("path TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("name TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("parent TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("categoryKey INTEGER"));
        assertTrue(mSqlCaptor.getValue().contains("price DECIMAL(10, 2)"));
        assertTrue(mSqlCaptor.getValue().contains("tax DECIMAL(10, 2)"));
        assertTrue(mSqlCaptor.getValue().contains("exchange_rate DECIMAL(10, 10)"));
        assertTrue(mSqlCaptor.getValue().contains("rcpt_date DATE"));
        assertTrue(mSqlCaptor.getValue().contains("timezone TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("comment TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("expenseable BOOLEAN"));
        assertTrue(mSqlCaptor.getValue().contains("isocode TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("paymentMethodKey INTEGER"));
        assertTrue(mSqlCaptor.getValue().contains("fullpageimage BOOLEAN"));
        assertTrue(mSqlCaptor.getValue().contains("receipt_processing_status TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("extra_edittext_1 TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("extra_edittext_2 TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("extra_edittext_3 TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("drive_sync_id TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("drive_is_synced BOOLEAN DEFAULT 0"));
        assertTrue(mSqlCaptor.getValue().contains("drive_marked_for_deletion BOOLEAN DEFAULT 0"));
        assertTrue(mSqlCaptor.getValue().contains("last_local_modification_time DATE"));
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

    @Test
    public void get() {
        final List<Receipt> receipts = mReceiptsTable.get().blockingGet();
        assertEquals(receipts, Arrays.asList(mReceipt2, mReceipt1)); // Note: The receipt with the more recent date (ie 2) appears first
    }

    @Test
    public void getForTrip() {
        // Note: We're adding this one to trip 1
        final Receipt receipt = mReceiptsTable.insert(mBuilder.setName(NAME_3).setPrice(PRICE_3).setTrip(mTrip1).setDate(DATE_3).setIndex(2).build(), new DatabaseOperationMetadata()).blockingGet();
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
        final Receipt receipt = mReceiptsTable.insert(mBuilder.setName(NAME_3).setPrice(PRICE_3).setTrip(mTrip3).setDate(DATE_3).setIndex(3).build(), new DatabaseOperationMetadata()).blockingGet();
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
        final Receipt updatedReceipt = mReceiptsTable.update(mReceipt1, mBuilder.setName(NAME_3).setPrice(PRICE_3).setTrip(mTrip3).setDate(DATE_3).setIndex(2).build(), new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(updatedReceipt);
        assertFalse(mReceipt1.equals(updatedReceipt));

        final List<Receipt> receipts = mReceiptsTable.get().blockingGet();
        assertEquals(receipts, Arrays.asList(updatedReceipt, new ReceiptBuilderFactory(mReceipt2).setIndex(1).build())); // Note: The receipt with the more recent date appears first
    }

    @Test
    public void updateWithOlderDate() {
        final Receipt updatedReceipt = mReceiptsTable.update(mReceipt1, mBuilder.setName(NAME_3).setPrice(PRICE_3).setTrip(mTrip3).setDate(DATE_1).setIndex(1).build(), new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(updatedReceipt);
        assertFalse(mReceipt1.equals(updatedReceipt));

        final List<Receipt> receipts = mReceiptsTable.get().blockingGet();
        assertEquals(receipts, Arrays.asList(mReceipt2, updatedReceipt)); // Note: The receipt with the more recent date appears first
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