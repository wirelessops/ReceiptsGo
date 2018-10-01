package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.sql.Date;
import java.util.TimeZone;
import java.util.UUID;

import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.TripBuilderFactory;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.model.SyncState;
import wb.android.storage.StorageManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class TripDatabaseAdapterTest {

    private static final int ID = 15;
    private static final UUID TRIP_UUID = UUID.randomUUID();
    private static final String NAME = "Trip";
//    private static final String PRIMARY_KEY_NAME = "Trip_PK_Update";
    private static final long START_DATE = 1409703721000L;
    private static final long END_DATE = 1409703794000L;
    private static final String START_TIMEZONE = TimeZone.getAvailableIDs()[0];
    private static final String END_TIMEZONE = TimeZone.getAvailableIDs()[1];
    private static final String COMMENT = "Comment";
    private static final String COST_CENTER = "Cost Center";
    private static final String CURRENCY_CODE = "USD";
    private static final String USER_PREFERENCES_CURRENCY_CODE = "EUR";

    // Class under test
    TripDatabaseAdapter mTripDatabaseAdapter;

    @Mock
    Trip mTrip;

    @Mock
    Cursor mCursor;

    @Mock
    PrimaryKey<Trip, Integer> mPrimaryKey;

    @Mock
    StorageManager mStorageManager;

    @Mock
    UserPreferenceManager mPreferences;

    @Mock
    SyncStateAdapter mSyncStateAdapter;

    @Mock
    SyncState mSyncState, mGetSyncState;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        final int nameIndex = 1;
        final int fromDateIndex = 2;
        final int toDateIndex = 3;
        final int fromTimeZoneIndex = 4;
        final int toTimezoneIndex = 5;
        final int commentIndex = 6;
        final int costCenterIndex = 7;
        final int defaultCurrencyIndex = 8;
        final int idIndex = 9;
        final int uuidIndex = 10;

        when(mCursor.getColumnIndex("id")).thenReturn(idIndex);
        when(mCursor.getColumnIndex("name")).thenReturn(nameIndex);
        when(mCursor.getColumnIndex("from_date")).thenReturn(fromDateIndex);
        when(mCursor.getColumnIndex("to_date")).thenReturn(toDateIndex);
        when(mCursor.getColumnIndex("from_timezone")).thenReturn(fromTimeZoneIndex);
        when(mCursor.getColumnIndex("to_timezone")).thenReturn(toTimezoneIndex);
        when(mCursor.getColumnIndex("trips_comment")).thenReturn(commentIndex);
        when(mCursor.getColumnIndex("trips_cost_center")).thenReturn(costCenterIndex);
        when(mCursor.getColumnIndex("trips_default_currency")).thenReturn(defaultCurrencyIndex);
        when(mCursor.getColumnIndex("entity_uuid")).thenReturn(uuidIndex);

        when(mCursor.getInt(idIndex)).thenReturn(ID);
        when(mCursor.getString(nameIndex)).thenReturn(NAME);
        when(mCursor.getLong(fromDateIndex)).thenReturn(START_DATE);
        when(mCursor.getLong(toDateIndex)).thenReturn(END_DATE);
        when(mCursor.getString(fromTimeZoneIndex)).thenReturn(START_TIMEZONE);
        when(mCursor.getString(toTimezoneIndex)).thenReturn(END_TIMEZONE);
        when(mCursor.getString(commentIndex)).thenReturn(COMMENT);
        when(mCursor.getString(costCenterIndex)).thenReturn(COST_CENTER);
        when(mCursor.getString(defaultCurrencyIndex)).thenReturn(CURRENCY_CODE);
        when(mCursor.getString(uuidIndex)).thenReturn(TRIP_UUID.toString());


        when(mTrip.getId()).thenReturn(ID);
        when(mTrip.getName()).thenReturn(NAME);
        when(mTrip.getStartDate()).thenReturn(new Date(START_DATE));
        when(mTrip.getEndDate()).thenReturn(new Date(END_DATE));
        when(mTrip.getStartTimeZone()).thenReturn(TimeZone.getTimeZone(START_TIMEZONE));
        when(mTrip.getEndTimeZone()).thenReturn(TimeZone.getTimeZone(END_TIMEZONE));
        when(mTrip.getComment()).thenReturn(COMMENT);
        when(mTrip.getCostCenter()).thenReturn(COST_CENTER);
        when(mTrip.getDefaultCurrencyCode()).thenReturn(CURRENCY_CODE);
        when(mTrip.getSource()).thenReturn(Source.Undefined);
        when(mTrip.getSyncState()).thenReturn(mSyncState);
        when(mTrip.getUuid()).thenReturn(TRIP_UUID);

        when(mPrimaryKey.getPrimaryKeyValue(mTrip)).thenReturn(ID);

        when(mPreferences.get(UserPreference.General.DefaultCurrency)).thenReturn(USER_PREFERENCES_CURRENCY_CODE);
        when(mStorageManager.getFile(NAME)).thenReturn(new File(NAME));
        when(mStorageManager.mkdir(NAME)).thenReturn(new File(NAME));

        when(mSyncStateAdapter.read(mCursor)).thenReturn(mSyncState);
        when(mSyncStateAdapter.get(any(SyncState.class), any(DatabaseOperationMetadata.class))).thenReturn(mGetSyncState);

        mTripDatabaseAdapter = new TripDatabaseAdapter(mStorageManager, mPreferences, mSyncStateAdapter);
    }

    @Test
    public void read() throws Exception {
        final Trip trip = new TripBuilderFactory()
                .setId(ID)
                .setUuid(TRIP_UUID)
                .setDirectory(mStorageManager.getFile(NAME))
                .setStartDate(START_DATE)
                .setEndDate(END_DATE)
                .setStartTimeZone(START_TIMEZONE)
                .setEndTimeZone(END_TIMEZONE)
                .setComment(COMMENT)
                .setCostCenter(COST_CENTER)
                .setDefaultCurrency(CURRENCY_CODE, mPreferences.get(UserPreference.General.DefaultCurrency))
                .setSourceAsCache()
                .setSyncState(mSyncState)
                .build();
        assertEquals(trip, mTripDatabaseAdapter.read(mCursor));
    }

    @Test
    public void writeUnsycned() throws Exception {
        final String sync = "sync";
        final ContentValues syncValues = new ContentValues();
        syncValues.put(sync, sync);
        when(mSyncStateAdapter.writeUnsynced(mSyncState)).thenReturn(syncValues);

        final ContentValues contentValues = mTripDatabaseAdapter.write(mTrip, new DatabaseOperationMetadata());

        assertEquals(NAME, contentValues.getAsString("name"));
        assertEquals(START_DATE, (long) contentValues.getAsLong("from_date"));
        assertEquals(END_DATE, (long) contentValues.getAsLong("to_date"));
        assertEquals(START_TIMEZONE, contentValues.getAsString("from_timezone"));
        assertEquals(END_TIMEZONE, contentValues.getAsString("to_timezone"));
        assertEquals(COMMENT, contentValues.getAsString("trips_comment"));
        assertEquals(COST_CENTER, contentValues.getAsString("trips_cost_center"));
        assertEquals(CURRENCY_CODE, contentValues.getAsString("trips_default_currency"));
        assertEquals(TRIP_UUID.toString(), contentValues.getAsString("entity_uuid"));
        assertEquals(sync, contentValues.getAsString(sync));
        assertFalse(contentValues.containsKey("miles_new"));
        assertFalse(contentValues.containsKey("trips_filters"));
        assertFalse(contentValues.containsKey("trip_processing_status"));
        assertFalse(contentValues.containsKey("price"));
    }

    @Test
    public void write() throws Exception {
        final String sync = "sync";
        final ContentValues syncValues = new ContentValues();
        syncValues.put(sync, sync);
        when(mSyncStateAdapter.write(mSyncState)).thenReturn(syncValues);

        final ContentValues contentValues = mTripDatabaseAdapter.write(mTrip, new DatabaseOperationMetadata(OperationFamilyType.Sync));

        assertEquals(NAME, contentValues.getAsString("name"));
        assertEquals(START_DATE, (long) contentValues.getAsLong("from_date"));
        assertEquals(END_DATE, (long) contentValues.getAsLong("to_date"));
        assertEquals(START_TIMEZONE, contentValues.getAsString("from_timezone"));
        assertEquals(END_TIMEZONE, contentValues.getAsString("to_timezone"));
        assertEquals(COMMENT, contentValues.getAsString("trips_comment"));
        assertEquals(COST_CENTER, contentValues.getAsString("trips_cost_center"));
        assertEquals(CURRENCY_CODE, contentValues.getAsString("trips_default_currency"));
        assertEquals(TRIP_UUID.toString(), contentValues.getAsString("entity_uuid"));
        assertEquals(sync, contentValues.getAsString(sync));
        assertFalse(contentValues.containsKey("miles_new"));
        assertFalse(contentValues.containsKey("trips_filters"));
        assertFalse(contentValues.containsKey("trip_processing_status"));
        assertFalse(contentValues.containsKey("price"));
    }

    @Test
    public void build() throws Exception {
        final Trip trip = new TripBuilderFactory()
                .setId(ID)
                .setUuid(TRIP_UUID)
                .setDirectory(mStorageManager.getFile(NAME))
                .setStartDate(START_DATE)
                .setEndDate(END_DATE)
                .setStartTimeZone(START_TIMEZONE)
                .setEndTimeZone(END_TIMEZONE)
                .setComment(COMMENT)
                .setCostCenter(COST_CENTER)
                .setDefaultCurrency(CURRENCY_CODE, mPreferences.get(UserPreference.General.DefaultCurrency))
                .setSourceAsCache()
                .setSyncState(mGetSyncState)
                .build();
        assertEquals(trip, mTripDatabaseAdapter.build(mTrip, mPrimaryKey, TRIP_UUID, mock(DatabaseOperationMetadata.class)));
        assertEquals(trip.getSyncState(), mTripDatabaseAdapter.build(mTrip, mPrimaryKey, TRIP_UUID, mock(DatabaseOperationMetadata.class)).getSyncState());
    }

}