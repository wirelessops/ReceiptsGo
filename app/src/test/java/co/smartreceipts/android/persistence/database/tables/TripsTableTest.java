package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;

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

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import co.smartreceipts.android.model.Keyed;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.TripBuilderFactory;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import wb.android.storage.StorageManager;

import static co.smartreceipts.android.persistence.database.tables.AbstractSqlTable.COLUMN_ID;
import static co.smartreceipts.android.persistence.database.tables.TripsTable.COLUMN_COMMENT;
import static co.smartreceipts.android.persistence.database.tables.TripsTable.COLUMN_COMMENT_HIDDEN_AUTO_COMPLETE;
import static co.smartreceipts.android.persistence.database.tables.TripsTable.COLUMN_COSTCENTER_HIDDEN_AUTO_COMPLETE;
import static co.smartreceipts.android.persistence.database.tables.TripsTable.COLUMN_COST_CENTER;
import static co.smartreceipts.android.persistence.database.tables.TripsTable.COLUMN_DEFAULT_CURRENCY;
import static co.smartreceipts.android.persistence.database.tables.TripsTable.COLUMN_DRIVE_IS_SYNCED;
import static co.smartreceipts.android.persistence.database.tables.TripsTable.COLUMN_DRIVE_MARKED_FOR_DELETION;
import static co.smartreceipts.android.persistence.database.tables.TripsTable.COLUMN_DRIVE_SYNC_ID;
import static co.smartreceipts.android.persistence.database.tables.TripsTable.COLUMN_FILTERS;
import static co.smartreceipts.android.persistence.database.tables.TripsTable.COLUMN_FROM;
import static co.smartreceipts.android.persistence.database.tables.TripsTable.COLUMN_FROM_TIMEZONE;
import static co.smartreceipts.android.persistence.database.tables.TripsTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME;
import static co.smartreceipts.android.persistence.database.tables.TripsTable.COLUMN_NAME;
import static co.smartreceipts.android.persistence.database.tables.TripsTable.COLUMN_NAME_HIDDEN_AUTO_COMPLETE;
import static co.smartreceipts.android.persistence.database.tables.TripsTable.COLUMN_PROCESSING_STATUS;
import static co.smartreceipts.android.persistence.database.tables.TripsTable.COLUMN_TO;
import static co.smartreceipts.android.persistence.database.tables.TripsTable.COLUMN_TO_TIMEZONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class TripsTableTest {

    private static final String NAME_1 = "Trip1";
    private static final String NAME_2 = "Trip2";
    private static final String NAME_3 = "Trip3";

    // Use the to verify that sort ordering is on the End Date (i.e. End3 > End1 > End2)
    private static final long START_DATE_2 = 1409703721000L;
    private static final long START_DATE_1 = 1409703722000L;
    private static final long END_DATE_2 = 1409703793000L;
    private static final long END_DATE_1 = 1409703794000L;
    private static final long START_DATE_3 = 1409703891000L;
    private static final long END_DATE_3 = 1409703893000L;

    private static final String START_TIMEZONE = TimeZone.getAvailableIDs()[0];
    private static final String END_TIMEZONE = TimeZone.getAvailableIDs()[1];
    private static final String COMMENT = "Comment";
    private static final String COST_CENTER = "Cost Center";
    private static final String CURRENCY_CODE = "USD";
    private static final String USER_PREFERENCES_CURRENCY_CODE = "EUR";

    // Class under test
    TripsTable mTripsTable;

    @Mock
    SQLiteDatabase mSQLiteDatabase;

    @Mock
    TableDefaultsCustomizer mTableDefaultsCustomizer;

    @Mock
    PersistenceManager mPersistenceManager;

    @Mock
    StorageManager mStorageManager;

    @Mock
    UserPreferenceManager mPreferences;

    @Captor
    ArgumentCaptor<String> mSqlCaptor;

    SQLiteOpenHelper mSQLiteOpenHelper;

    Trip mTrip1;

    Trip mTrip2;

    TripBuilderFactory mBuilder;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        mSQLiteOpenHelper = new TestSQLiteOpenHelper(ApplicationProvider.getApplicationContext());

        when(mPersistenceManager.getStorageManager()).thenReturn(mStorageManager);
        when(mPersistenceManager.getPreferenceManager()).thenReturn(mPreferences);
        when(mStorageManager.getFile(NAME_1)).thenReturn(new File(NAME_1));
        when(mStorageManager.getFile(NAME_2)).thenReturn(new File(NAME_2));
        when(mStorageManager.getFile(NAME_3)).thenReturn(new File(NAME_3));
        when(mStorageManager.mkdir(NAME_1)).thenReturn(new File(NAME_1));
        when(mStorageManager.mkdir(NAME_2)).thenReturn(new File(NAME_2));
        when(mStorageManager.mkdir(NAME_3)).thenReturn(new File(NAME_3));
        when(mPreferences.get(UserPreference.General.DefaultCurrency)).thenReturn(USER_PREFERENCES_CURRENCY_CODE);

        mTripsTable = new TripsTable(mSQLiteOpenHelper, mPersistenceManager.getStorageManager(), mPersistenceManager.getPreferenceManager());

        // Now create the table and insert some defaults
        mTripsTable.onCreate(mSQLiteOpenHelper.getWritableDatabase(), mTableDefaultsCustomizer);
        mBuilder = new TripBuilderFactory();
        mBuilder.setStartTimeZone(START_TIMEZONE).setEndTimeZone(END_TIMEZONE).setComment(COMMENT).setCostCenter(COST_CENTER)
                .setDefaultCurrency(CURRENCY_CODE, mPreferences.get(UserPreference.General.DefaultCurrency))
                .setNameHiddenFromAutoComplete(false).setCommentHiddenFromAutoComplete(false).setCostCenterHiddenFromAutoComplete(false);
        final Trip trip1 = mBuilder.setStartDate(START_DATE_1).setEndDate(END_DATE_1).setDirectory(mStorageManager.getFile(NAME_1)).build();
        final Trip trip2 = mBuilder.setStartDate(START_DATE_2).setEndDate(END_DATE_2).setDirectory(mStorageManager.getFile(NAME_2)).build();

        mTrip1 = mTripsTable.insert(trip1, new DatabaseOperationMetadata()).blockingGet();
        mTrip2 = mTripsTable.insert(trip2, new DatabaseOperationMetadata()).blockingGet();
    }

    @After
    public void tearDown() {
        mSQLiteOpenHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + mTripsTable.getTableName());
    }

    @Test
    public void getTableName() {
        assertEquals("trips", mTripsTable.getTableName());
    }

    @Test
    public void onCreate() {
        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mTripsTable.onCreate(mSQLiteDatabase, customizer);
        verify(mSQLiteDatabase).execSQL(mSqlCaptor.capture());
        verifyZeroInteractions(customizer);

        final String creatingTable = mSqlCaptor.getValue();
        assertTrue(creatingTable.contains("CREATE TABLE trips")); // Table name
        assertTrue(creatingTable.contains("id INTEGER PRIMARY KEY AUTOINCREMENT"));
        assertTrue(creatingTable.contains("name TEXT UNIQUE"));
        assertTrue(creatingTable.contains("from_date DATE"));
        assertTrue(creatingTable.contains("to_date DATE"));
        assertTrue(creatingTable.contains("from_timezone TEXT"));
        assertTrue(creatingTable.contains("to_timezone TEXT"));
        assertTrue(creatingTable.contains("trips_comment TEXT"));
        assertTrue(creatingTable.contains("trips_cost_center TEXT"));
        assertTrue(creatingTable.contains("trips_default_currency TEXT"));
        assertTrue(creatingTable.contains("trips_filters TEXT"));
        assertTrue(creatingTable.contains("trip_processing_status TEXT"));
        assertTrue(creatingTable.contains("drive_sync_id TEXT"));
        assertTrue(creatingTable.contains("drive_is_synced BOOLEAN DEFAULT 0"));
        assertTrue(creatingTable.contains("drive_marked_for_deletion BOOLEAN DEFAULT 0"));
        assertTrue(creatingTable.contains("last_local_modification_time DATE"));
        assertTrue(creatingTable.contains("entity_uuid TEXT"));
        assertTrue(creatingTable.contains("name_hidden_auto_complete BOOLEAN DEFAULT 0"));
        assertTrue(creatingTable.contains("comment_hidden_auto_complete BOOLEAN DEFAULT 0"));
        assertTrue(creatingTable.contains("costcenter_hidden_auto_complete BOOLEAN DEFAULT 0"));
    }

    @Test
    public void onUpgradeFromV8() {
        final int oldVersion = 8;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mTripsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV8Upgrade(times(1));
        verifyV10Upgrade(times(1));
        verifyV11Upgrade(times(1));
        verifyV12Upgrade(times(1));
        verifyV14Upgrade(times(1));
        verifyV18Upgrade(times(1));
        verifyV19Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV10() {
        final int oldVersion = 10;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mTripsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV8Upgrade(never());
        verifyV10Upgrade(times(1));
        verifyV11Upgrade(times(1));
        verifyV12Upgrade(times(1));
        verifyV14Upgrade(times(1));
        verifyV18Upgrade(times(1));
        verifyV19Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV11() {
        final int oldVersion = 11;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mTripsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV8Upgrade(never());
        verifyV10Upgrade(never());
        verifyV11Upgrade(times(1));
        verifyV12Upgrade(times(1));
        verifyV14Upgrade(times(1));
        verifyV18Upgrade(times(1));
        verifyV19Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV12() {
        final int oldVersion = 12;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mTripsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV8Upgrade(never());
        verifyV10Upgrade(never());
        verifyV11Upgrade(never());
        verifyV12Upgrade(times(1));
        verifyV14Upgrade(times(1));
        verifyV18Upgrade(times(1));
        verifyV19Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV14() {
        final int oldVersion = 14;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mTripsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV8Upgrade(never());
        verifyV10Upgrade(never());
        verifyV11Upgrade(never());
        verifyV12Upgrade(never());
        verifyV14Upgrade(times(1));
        verifyV18Upgrade(times(1));
        verifyV19Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV18() {
        final int oldVersion = 18;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mTripsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV8Upgrade(never());
        verifyV10Upgrade(never());
        verifyV11Upgrade(never());
        verifyV12Upgrade(never());
        verifyV14Upgrade(never());
        verifyV18Upgrade(times(1));
        verifyV19Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV19() {
        final int oldVersion = 19;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mTripsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV8Upgrade(never());
        verifyV10Upgrade(never());
        verifyV11Upgrade(never());
        verifyV12Upgrade(never());
        verifyV14Upgrade(never());
        verifyV18Upgrade(never());
        verifyV19Upgrade(times(1));
    }

    private void verifyV8Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE trips ADD from_timezone TEXT");
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE trips ADD to_timezone TEXT");
    }

    private void verifyV10Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE trips ADD trips_comment TEXT");
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE trips ADD trips_default_currency TEXT");
    }

    private void verifyV11Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE trips ADD trips_filters TEXT");
    }

    private void verifyV12Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE trips ADD trips_cost_center TEXT");
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE trips ADD trip_processing_status TEXT");
    }

    private void verifyV14Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE " + mTripsTable.getTableName() + " ADD drive_sync_id TEXT");
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE " + mTripsTable.getTableName() + " ADD drive_is_synced BOOLEAN DEFAULT 0");
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE " + mTripsTable.getTableName() + " ADD drive_marked_for_deletion BOOLEAN DEFAULT 0");
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE " + mTripsTable.getTableName() + " ADD last_local_modification_time DATE");
    }

    private void verifyV18Upgrade(@NonNull VerificationMode verificationMode) {
        final String copyTable = "CREATE TABLE " + TripsTable.TABLE_NAME + "_copy" + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT UNIQUE, "
                + COLUMN_FROM + " DATE, "
                + COLUMN_TO + " DATE, "
                + COLUMN_FROM_TIMEZONE + " TEXT, "
                + COLUMN_TO_TIMEZONE + " TEXT, "
                + COLUMN_COMMENT + " TEXT, "
                + COLUMN_COST_CENTER + " TEXT, "
                + COLUMN_DEFAULT_CURRENCY + " TEXT, "
                + COLUMN_PROCESSING_STATUS + " TEXT, "
                + COLUMN_FILTERS + " TEXT, "
                + AbstractSqlTable.COLUMN_DRIVE_SYNC_ID + " TEXT, "
                + AbstractSqlTable.COLUMN_DRIVE_IS_SYNCED + " BOOLEAN DEFAULT 0, "
                + AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION + " BOOLEAN DEFAULT 0, "
                + AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME + " DATE"
                + ");";

        final String baseColumns = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s",
                COLUMN_NAME, COLUMN_FROM, COLUMN_TO, COLUMN_FROM_TIMEZONE,
                COLUMN_TO_TIMEZONE, COLUMN_COMMENT, COLUMN_COST_CENTER, COLUMN_DEFAULT_CURRENCY, COLUMN_PROCESSING_STATUS, COLUMN_FILTERS, COLUMN_DRIVE_SYNC_ID,
                COLUMN_DRIVE_IS_SYNCED, COLUMN_DRIVE_MARKED_FOR_DELETION, COLUMN_LAST_LOCAL_MODIFICATION_TIME);

        final String insertData = "INSERT INTO " + TripsTable.TABLE_NAME + "_copy"
                + " (" + baseColumns + ") "
                + "SELECT " + baseColumns
                + " FROM " + TripsTable.TABLE_NAME + ";";

        final String dropOldTable = "DROP TABLE " + TripsTable.TABLE_NAME + ";";

        final String renameTable = "ALTER TABLE " + TripsTable.TABLE_NAME + "_copy" + " RENAME TO " + TripsTable.TABLE_NAME + ";";

        verify(mSQLiteDatabase, verificationMode).execSQL(copyTable);
        verify(mSQLiteDatabase, verificationMode).execSQL(insertData);
        verify(mSQLiteDatabase, verificationMode).execSQL(dropOldTable);
        verify(mSQLiteDatabase, verificationMode).execSQL(renameTable);
    }

    private void verifyV19Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE " + TripsTable.TABLE_NAME + " ADD " + COLUMN_NAME_HIDDEN_AUTO_COMPLETE + " BOOLEAN DEFAULT 0");
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE " + TripsTable.TABLE_NAME + " ADD " + COLUMN_COMMENT_HIDDEN_AUTO_COMPLETE + " BOOLEAN DEFAULT 0");
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE " + TripsTable.TABLE_NAME + " ADD " + COLUMN_COSTCENTER_HIDDEN_AUTO_COMPLETE + " BOOLEAN DEFAULT 0");
    }

    @Test
    public void onUpgradeAlreadyOccurred() {
        final int oldVersion = DatabaseHelper.DATABASE_VERSION;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mTripsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, never()).execSQL(mSqlCaptor.capture());
        verifyZeroInteractions(customizer);
    }

    @Test
    public void get() {
        final List<Trip> trips = mTripsTable.get().blockingGet();
        assertEquals(trips, Arrays.asList(mTrip1, mTrip2));
    }

    @Test
    public void insert() {
        final Trip trip = mTripsTable.insert(mBuilder.setStartDate(START_DATE_3).setEndDate(END_DATE_3).setDirectory(mStorageManager.getFile(NAME_3)).build(), new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(trip);

        final List<Trip> trips = mTripsTable.get().blockingGet();
        // Also confirm the new one is first b/c of date ordering
        assertEquals(trips, Arrays.asList(trip, mTrip1, mTrip2));
        assertNotEquals(trip.getUuid(), Keyed.Companion.getMISSING_UUID());
    }

    @Test
    public void insertWithSameName() {
        mTripsTable.insert(mBuilder.setDirectory(mStorageManager.getFile(NAME_3)).build(), new DatabaseOperationMetadata())
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValueCount(1);

        mTripsTable.insert(mBuilder.setDirectory(mStorageManager.getFile(NAME_3)).build(), new DatabaseOperationMetadata())
                .test()
                .assertNotComplete()
                .assertError(Exception.class);
    }

    @Test
    public void findByPrimaryKey() {
        mTripsTable.findByPrimaryKey(mTrip1.getId())
                .test()
                .assertNoErrors()
                .assertResult(mTrip1);
    }

    @Test
    public void findByPrimaryMissingKey() {
        mTripsTable.findByPrimaryKey(-1)
                .test()
                .assertError(Exception.class);
    }

    @Test
    public void update() {
        final UUID oldUuid = mTrip1.getUuid();

        final Trip updatedTrip = mTripsTable.update(mTrip1, mBuilder.setDirectory(mStorageManager.getFile(NAME_3)).setUuid(UUID.randomUUID()).build(), new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(updatedTrip);
        assertNotEquals(mTrip1, updatedTrip);
        assertEquals(oldUuid, updatedTrip.getUuid());

        final List<Trip> trips = mTripsTable.get().blockingGet();
        assertEquals(trips, Arrays.asList(updatedTrip, mTrip2));
    }

    @Test
    public void delete() {
        assertEquals(mTrip1, mTripsTable.delete(mTrip1, new DatabaseOperationMetadata()).blockingGet());

        final List<Trip> trips = mTripsTable.get().blockingGet();
        assertEquals(trips, Collections.singletonList(mTrip2));
    }

}