package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import junit.framework.Assert;

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
import java.util.TimeZone;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.DistanceBuilderFactory;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import io.reactivex.Single;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DistanceTableTest {

    private static final double DISTANCE_1 = 12.55d;
    private static final String LOCATION_1 = "Location";
    private static final String TRIP_1 = "Trip";
    private static final double DISTANCE_2 = 140d;
    private static final String LOCATION_2 = "Location2";
    private static final String TRIP_2 = "Trip2";
    private static final double DISTANCE_3 = 12.123;
    private static final String LOCATION_3 = "Location3";
    private static final String TRIP_3 = "Trip3";

    private static final long DATE = 1409703721000L;
    private static final String TIMEZONE = TimeZone.getDefault().getID();
    private static final String COMMENT = "Comment";
    private static final double RATE = 0.33d;
    private static final String CURRENCY_CODE = "USD";

    // Class under test
    DistanceTable mDistanceTable;

    @Mock
    SQLiteDatabase mSQLiteDatabase;

    @Mock
    TableDefaultsCustomizer mTableDefaultsCustomizer;

    @Mock
    Table<Trip, String> mTripsTable;

    @Mock
    UserPreferenceManager userPreferenceManager;

    @Mock
    Trip mTrip1;

    @Mock
    Trip mTrip2;

    @Mock
    Trip mTrip3;

    @Captor
    ArgumentCaptor<String> mSqlCaptor;

    SQLiteOpenHelper mSQLiteOpenHelper;

    Distance mDistance1;

    Distance mDistance2;

    DistanceBuilderFactory mBuilder;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(mTrip1.getName()).thenReturn(TRIP_1);
        when(mTrip2.getName()).thenReturn(TRIP_2);
        when(mTrip3.getName()).thenReturn(TRIP_3);

        when(mTripsTable.findByPrimaryKey(TRIP_1)).thenReturn(Single.just(mTrip1));
        when(mTripsTable.findByPrimaryKey(TRIP_2)).thenReturn(Single.just(mTrip2));
        when(mTripsTable.findByPrimaryKey(TRIP_3)).thenReturn(Single.just(mTrip3));
        when(userPreferenceManager.get(UserPreference.General.DefaultCurrency)).thenReturn(CURRENCY_CODE);

        mSQLiteOpenHelper = new TestSQLiteOpenHelper(RuntimeEnvironment.application);
        mDistanceTable = new DistanceTable(mSQLiteOpenHelper, mTripsTable, userPreferenceManager);

        // Now create the table and insert some defaults
        mDistanceTable.onCreate(mSQLiteOpenHelper.getWritableDatabase(), mTableDefaultsCustomizer);
        mBuilder = new DistanceBuilderFactory();
        mBuilder.setDate(DATE).setTimezone(TIMEZONE).setComment(COMMENT).setRate(RATE).setCurrency(CURRENCY_CODE);
        mDistance1 = mDistanceTable.insert(mBuilder.setDistance(DISTANCE_1).setLocation(LOCATION_1).setTrip(mTrip1).build(), new DatabaseOperationMetadata()).blockingGet();
        mDistance2 = mDistanceTable.insert(mBuilder.setDistance(DISTANCE_2).setLocation(LOCATION_2).setTrip(mTrip2).build(), new DatabaseOperationMetadata()).blockingGet();
    }

    @After
    public void tearDown() {
        mSQLiteOpenHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + mDistanceTable.getTableName());
    }

    @Test
    public void getTableName() {
        assertEquals("distance", mDistanceTable.getTableName());
    }

    @Test
    public void onCreate() {
        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mDistanceTable.onCreate(mSQLiteDatabase, customizer);
        verify(mSQLiteDatabase).execSQL(mSqlCaptor.capture());
        verifyZeroInteractions(customizer);

        assertTrue(mSqlCaptor.getValue().contains("CREATE TABLE distance")); // Table name
        assertTrue(mSqlCaptor.getValue().contains("id INTEGER PRIMARY KEY AUTOINCREMENT"));
        assertTrue(mSqlCaptor.getValue().contains("parent TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("distance DECIMAL(10, 2)"));
        assertTrue(mSqlCaptor.getValue().contains("location TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("date DATE"));
        assertTrue(mSqlCaptor.getValue().contains("timezone TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("comment TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("rate DECIMAL(10, 2)"));
        assertTrue(mSqlCaptor.getValue().contains("rate_currency TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("drive_sync_id TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("drive_is_synced BOOLEAN DEFAULT 0"));
        assertTrue(mSqlCaptor.getValue().contains("drive_marked_for_deletion BOOLEAN DEFAULT 0"));
        assertTrue(mSqlCaptor.getValue().contains("last_local_modification_time DATE"));
    }

    @Test
    public void onUpgradeFromV12() {
        final int oldVersion = 12;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mDistanceTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, atLeastOnce()).execSQL(mSqlCaptor.capture());
        verifyZeroInteractions(customizer);

        assertTrue(mSqlCaptor.getAllValues().get(0).contains("distance")); // Table name
        assertTrue(mSqlCaptor.getAllValues().get(0).contains("id"));
        assertTrue(mSqlCaptor.getAllValues().get(0).contains("parent"));
        assertTrue(mSqlCaptor.getAllValues().get(0).contains("distance"));
        assertTrue(mSqlCaptor.getAllValues().get(0).contains("location"));
        assertTrue(mSqlCaptor.getAllValues().get(0).contains("date"));
        assertTrue(mSqlCaptor.getAllValues().get(0).contains("timezone"));
        assertTrue(mSqlCaptor.getAllValues().get(0).contains("comment"));
        assertTrue(mSqlCaptor.getAllValues().get(0).contains("rate"));
        assertTrue(mSqlCaptor.getAllValues().get(0).contains("rate_currency"));

        // Create
        assertEquals(mSqlCaptor.getAllValues().get(0), "CREATE TABLE distance (id INTEGER PRIMARY KEY AUTOINCREMENT,parent TEXT REFERENCES name ON DELETE CASCADE,distance DECIMAL(10, 2) DEFAULT 0.00,location TEXT,date DATE,timezone TEXT,comment TEXT,rate_currency TEXT NOT NULL, rate DECIMAL(10, 2) DEFAULT 0.00);");

        // Migrate Trip Distances to Distance WHERE the Trip Currency != NULL
        assertEquals(mSqlCaptor.getAllValues().get(1), "INSERT INTO distance(parent, distance, location, date, timezone, comment, rate_currency) SELECT name, miles_new , \"\" as location, from_date, from_timezone , \"\" as comment, trips_default_currency FROM trips WHERE trips_default_currency IS NOT NULL AND miles_new > 0;");

        // Migrate Trip Distances to Distance WHERE the Trip Currency == NULL
        assertEquals(mSqlCaptor.getAllValues().get(2), "INSERT INTO distance(parent, distance, location, date, timezone, comment, rate_currency) SELECT name, miles_new , \"\" as location, from_date, from_timezone , \"\" as comment, \"USD\" as rate_currency FROM trips WHERE trips_default_currency IS NULL AND miles_new > 0;");

        assertEquals(mSqlCaptor.getAllValues().get(3), "ALTER TABLE " + mDistanceTable.getTableName() + " ADD drive_sync_id TEXT");
        assertEquals(mSqlCaptor.getAllValues().get(4), "ALTER TABLE " + mDistanceTable.getTableName() + " ADD drive_is_synced BOOLEAN DEFAULT 0");
        assertEquals(mSqlCaptor.getAllValues().get(5), "ALTER TABLE " + mDistanceTable.getTableName() + " ADD drive_marked_for_deletion BOOLEAN DEFAULT 0");
        assertEquals(mSqlCaptor.getAllValues().get(6), "ALTER TABLE " + mDistanceTable.getTableName() + " ADD last_local_modification_time DATE");
    }

    @Test
    public void onUpgradeFromV14() {
        final int oldVersion = 14;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mDistanceTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, atLeastOnce()).execSQL(mSqlCaptor.capture());
        verifyZeroInteractions(customizer);

        assertEquals(mSqlCaptor.getAllValues().get(0), "ALTER TABLE " + mDistanceTable.getTableName() + " ADD drive_sync_id TEXT");
        assertEquals(mSqlCaptor.getAllValues().get(1), "ALTER TABLE " + mDistanceTable.getTableName() + " ADD drive_is_synced BOOLEAN DEFAULT 0");
        assertEquals(mSqlCaptor.getAllValues().get(2), "ALTER TABLE " + mDistanceTable.getTableName() + " ADD drive_marked_for_deletion BOOLEAN DEFAULT 0");
        assertEquals(mSqlCaptor.getAllValues().get(3), "ALTER TABLE " + mDistanceTable.getTableName() + " ADD last_local_modification_time DATE");
    }

    @Test
    public void onUpgradeAlreadyOccurred() {
        final int oldVersion = DatabaseHelper.DATABASE_VERSION;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mDistanceTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, never()).execSQL(mSqlCaptor.capture());
        verifyZeroInteractions(customizer);
    }

    @Test
    public void get() {
        final List<Distance> distances = mDistanceTable.get().blockingGet();
        assertEquals(distances, Arrays.asList(mDistance1, mDistance2));
    }

    @Test
    public void getForTrip() {
        // Note: We're adding this one to trip 1
        final Distance distance = mDistanceTable.insert(mBuilder.setDistance(DISTANCE_3).setLocation(LOCATION_3).setTrip(mTrip1).build(), new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(distance);

        final List<Distance> list1 = mDistanceTable.get(mTrip1).blockingGet();
        final List<Distance> list2 = mDistanceTable.get(mTrip2).blockingGet();
        final List<Distance> list3 = mDistanceTable.get(mTrip3).blockingGet();
        assertEquals(list1, Arrays.asList(mDistance1, distance));
        assertEquals(list2, Collections.singletonList(mDistance2));
        assertEquals(list3, Collections.<Distance>emptyList());
    }

    @Test
    public void insert() {
        final Distance distance = mDistanceTable.insert(mBuilder.setDistance(DISTANCE_3).setLocation(LOCATION_3).setTrip(mTrip3).build(), new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(distance);

        final List<Distance> distances = mDistanceTable.get().blockingGet();
        assertEquals(distances, Arrays.asList(mDistance1, mDistance2, distance));
    }

    @Test
    public void findByPrimaryKey() {
        mDistanceTable.findByPrimaryKey(mDistance1.getId())
                .test()
                .assertNoErrors()
                .assertResult(mDistance1);
    }

    @Test
    public void findByPrimaryMissingKey() {
        mDistanceTable.findByPrimaryKey(-1)
                .test()
                .assertError(Exception.class);
    }

    @Test
    public void update() {
        final Distance updatedDistance = mDistanceTable.update(mDistance1, mBuilder.setDistance(DISTANCE_3).setLocation(LOCATION_3).setTrip(mTrip3).build(), new DatabaseOperationMetadata()).blockingGet();
        assertNotNull(updatedDistance);
        assertFalse(mDistance1.equals(updatedDistance));

        final List<Distance> distances = mDistanceTable.get().blockingGet();
        assertEquals(distances, Arrays.asList(updatedDistance, mDistance2));
    }

    @Test
    public void delete() {
        Assert.assertEquals(mDistance1, mDistanceTable.delete(mDistance1, new DatabaseOperationMetadata()).blockingGet());

        final List<Distance> distances = mDistanceTable.get().blockingGet();
        assertEquals(distances, Collections.singletonList(mDistance2));
    }

}