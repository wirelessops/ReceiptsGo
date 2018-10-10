package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.TimeZone;
import java.util.UUID;

import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.DistanceBuilderFactory;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.DistanceTable;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.sync.model.SyncState;
import io.reactivex.Single;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DistanceDatabaseAdapterTest {

    private static final int ID = 5;
    private static final int PRIMARY_KEY_ID = 11;
    private static final int PARENT_ID = 15;
    private static final double DISTANCE = 12.55d;
    private static final String LOCATION = "Location";
    private static final long DATE = 1409703721000L;
    private static final String TIMEZONE = TimeZone.getDefault().getID();
    private static final String COMMENT = "Comment";
    private static final double RATE = 0.33d;
    private static final String CURRENCY_CODE = "USD";
    private static final UUID DIST_UUID = UUID.randomUUID();


    // Class under test
    DistanceDatabaseAdapter mDistanceDatabaseAdapter;

    @Mock
    Table<Trip> mTripsTable;

    @Mock
    Trip mTrip;

    @Mock
    Cursor mCursor;

    @Mock
    Distance mDistance;

    @Mock
    Price mPrice;

    @Mock
    SyncStateAdapter mSyncStateAdapter;

    @Mock
    SyncState mSyncState, mGetSyncState;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        final int idIndex = 1;
        final int parentIndex = 2;
        final int distanceIndex = 3;
        final int locationIndex = 4;
        final int dateIndex = 5;
        final int timezoneIndex = 6;
        final int commentIndex = 7;
        final int rateIndex = 8;
        final int rateCurrencyIndex = 9;
        final int uuidIndex = 10;
        when(mCursor.getColumnIndex("id")).thenReturn(idIndex);
        when(mCursor.getColumnIndex("parentKey")).thenReturn(parentIndex);
        when(mCursor.getColumnIndex("distance")).thenReturn(distanceIndex);
        when(mCursor.getColumnIndex("location")).thenReturn(locationIndex);
        when(mCursor.getColumnIndex("date")).thenReturn(dateIndex);
        when(mCursor.getColumnIndex("timezone")).thenReturn(timezoneIndex);
        when(mCursor.getColumnIndex("comment")).thenReturn(commentIndex);
        when(mCursor.getColumnIndex("rate")).thenReturn(rateIndex);
        when(mCursor.getColumnIndex("rate_currency")).thenReturn(rateCurrencyIndex);
        when(mCursor.getColumnIndex("entity_uuid")).thenReturn(uuidIndex);

        when(mCursor.getInt(idIndex)).thenReturn(ID);
        when(mCursor.getInt(parentIndex)).thenReturn(PARENT_ID);
        when(mCursor.getDouble(distanceIndex)).thenReturn(DISTANCE);
        when(mCursor.getString(locationIndex)).thenReturn(LOCATION);
        when(mCursor.getLong(dateIndex)).thenReturn(DATE);
        when(mCursor.getString(timezoneIndex)).thenReturn(TIMEZONE);
        when(mCursor.getString(commentIndex)).thenReturn(COMMENT);
        when(mCursor.getDouble(rateIndex)).thenReturn(RATE);
        when(mCursor.getString(rateCurrencyIndex)).thenReturn(CURRENCY_CODE);
        when(mCursor.getString(uuidIndex)).thenReturn(DIST_UUID.toString());

        when(mDistance.getTrip()).thenReturn(mTrip);
        when(mDistance.getLocation()).thenReturn(LOCATION);
        when(mDistance.getDistance()).thenReturn(new BigDecimal(DISTANCE));
        when(mDistance.getTimeZone()).thenReturn(TimeZone.getTimeZone(TIMEZONE));
        when(mDistance.getDate()).thenReturn(new Date(DATE));
        when(mDistance.getRate()).thenReturn(new BigDecimal(RATE));
        when(mDistance.getPrice()).thenReturn(mPrice);
        when(mDistance.getComment()).thenReturn(COMMENT);
        when(mDistance.getSyncState()).thenReturn(mSyncState);
        when(mDistance.getUuid()).thenReturn(DIST_UUID);

        when(mTrip.getId()).thenReturn(PARENT_ID);
        when(mPrice.getCurrencyCode()).thenReturn(CURRENCY_CODE);
        when(mPrice.getCurrency()).thenReturn(PriceCurrency.getInstance(CURRENCY_CODE));

        when(mTripsTable.findByPrimaryKey(PARENT_ID)).thenReturn(Single.just(mTrip));

        when(mSyncStateAdapter.read(mCursor)).thenReturn(mSyncState);
        when(mSyncStateAdapter.get(any(SyncState.class), any(DatabaseOperationMetadata.class))).thenReturn(mGetSyncState);

        mDistanceDatabaseAdapter = new DistanceDatabaseAdapter(mTripsTable, mSyncStateAdapter);
    }

    @Test
    public void read() throws Exception {
        final Distance distance = new DistanceBuilderFactory(ID).setUuid(DIST_UUID).setTrip(mTrip).setLocation(LOCATION).setDistance(DISTANCE).setDate(DATE).setTimezone(TIMEZONE).setRate(RATE).setCurrency(CURRENCY_CODE).setComment(COMMENT).setSyncState(mSyncState).build();
        assertEquals(distance, mDistanceDatabaseAdapter.read(mCursor));
    }

    @Test
    public void readForSelectionDescending() throws Exception {
        final Distance distance = new DistanceBuilderFactory(ID).setUuid(DIST_UUID).setTrip(mTrip).setLocation(LOCATION).setDistance(DISTANCE).setDate(DATE).setTimezone(TIMEZONE).setRate(RATE).setCurrency(CURRENCY_CODE).setComment(COMMENT).setSyncState(mSyncState).build();
        assertEquals(distance, mDistanceDatabaseAdapter.readForSelection(mCursor, mTrip, true));
    }

    @Test
    public void readForSelectionAscending() throws Exception {
        final Distance distance = new DistanceBuilderFactory(ID).setUuid(DIST_UUID).setTrip(mTrip).setLocation(LOCATION).setDistance(DISTANCE).setDate(DATE).setTimezone(TIMEZONE).setRate(RATE).setCurrency(CURRENCY_CODE).setComment(COMMENT).setSyncState(mSyncState).build();
        assertEquals(distance, mDistanceDatabaseAdapter.readForSelection(mCursor, mTrip, false));
    }

    @Test
    public void writeUnsycned() throws Exception {
        final String sync = "sync";
        final ContentValues syncValues = new ContentValues();
        syncValues.put(sync, sync);
        when(mSyncStateAdapter.writeUnsynced(mSyncState)).thenReturn(syncValues);

        final ContentValues contentValues = mDistanceDatabaseAdapter.write(mDistance, new DatabaseOperationMetadata());
        assertTrue(PARENT_ID == contentValues.getAsInteger(DistanceTable.COLUMN_PARENT_TRIP_ID));
        assertEquals(DISTANCE, contentValues.getAsDouble(DistanceTable.COLUMN_DISTANCE), 0.0001d);
        assertEquals(LOCATION, contentValues.getAsString(DistanceTable.COLUMN_LOCATION));
        assertEquals(DATE, (long) contentValues.getAsLong(DistanceTable.COLUMN_DATE));
        assertEquals(TIMEZONE, contentValues.getAsString(DistanceTable.COLUMN_TIMEZONE));
        assertEquals(COMMENT, contentValues.getAsString(DistanceTable.COLUMN_COMMENT));
        assertEquals(RATE, contentValues.getAsDouble(DistanceTable.COLUMN_RATE), 0.0001d);
        assertEquals(CURRENCY_CODE, contentValues.getAsString(DistanceTable.COLUMN_RATE_CURRENCY));
        assertEquals(DIST_UUID.toString(), contentValues.getAsString(DistanceTable.COLUMN_UUID));
        assertEquals(sync, contentValues.getAsString(sync));
        assertFalse(contentValues.containsKey(DistanceTable.COLUMN_ID));
    }

    @Test
    public void write() throws Exception {
        final String sync = "sync";
        final ContentValues syncValues = new ContentValues();
        syncValues.put(sync, sync);
        when(mSyncStateAdapter.write(mSyncState)).thenReturn(syncValues);

        final ContentValues contentValues = mDistanceDatabaseAdapter.write(mDistance, new DatabaseOperationMetadata(OperationFamilyType.Sync));
        assertTrue(PARENT_ID == contentValues.getAsInteger(DistanceTable.COLUMN_PARENT_TRIP_ID));
        assertEquals(DISTANCE, contentValues.getAsDouble(DistanceTable.COLUMN_DISTANCE), 0.0001d);
        assertEquals(LOCATION, contentValues.getAsString(DistanceTable.COLUMN_LOCATION));
        assertEquals(DATE, (long) contentValues.getAsLong(DistanceTable.COLUMN_DATE));
        assertEquals(TIMEZONE, contentValues.getAsString(DistanceTable.COLUMN_TIMEZONE));
        assertEquals(COMMENT, contentValues.getAsString(DistanceTable.COLUMN_COMMENT));
        assertEquals(RATE, contentValues.getAsDouble(DistanceTable.COLUMN_RATE), 0.0001d);
        assertEquals(CURRENCY_CODE, contentValues.getAsString(DistanceTable.COLUMN_RATE_CURRENCY));
        assertEquals(DIST_UUID.toString(), contentValues.getAsString(DistanceTable.COLUMN_UUID));
        assertEquals(sync, contentValues.getAsString(sync));
        assertFalse(contentValues.containsKey(DistanceTable.COLUMN_ID));
    }

    @Test
    public void build() throws Exception {
        final Distance distance = new DistanceBuilderFactory(ID).setUuid(DIST_UUID).setTrip(mTrip).setLocation(LOCATION).setDistance(DISTANCE).setDate(DATE).setTimezone(TIMEZONE).setRate(RATE).setCurrency(CURRENCY_CODE).setComment(COMMENT).setSyncState(mGetSyncState).build();
        assertEquals(distance, mDistanceDatabaseAdapter.build(mDistance, ID, DIST_UUID, mock(DatabaseOperationMetadata.class)));
        assertEquals(distance.getSyncState(), mDistanceDatabaseAdapter.build(mDistance, ID, DIST_UUID, mock(DatabaseOperationMetadata.class)).getSyncState());
    }

}