package com.wops.receiptsgo.persistence.database.tables.adapters;

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

import com.wops.receiptsgo.model.AutoCompleteMetadata;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.model.factory.TripBuilderFactory;
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;
import com.wops.receiptsgo.persistence.database.operations.OperationFamilyType;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.settings.catalog.UserPreference;
import co.smartreceipts.core.sync.model.SyncState;
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
    private static final long START_DATE = 1409703721000L;
    private static final long END_DATE = 1409703794000L;
    private static final String START_TIMEZONE = TimeZone.getAvailableIDs()[0];
    private static final String END_TIMEZONE = TimeZone.getAvailableIDs()[1];
    private static final String COMMENT = "Comment";
    private static final String COST_CENTER = "Cost Center";
    private static final String CURRENCY_CODE = "USD";
    private static final String USER_PREFERENCES_CURRENCY_CODE = "EUR";
    private static final boolean NAME_HIDDEN_FROM_AUTO_COMPLETE = false;
    private static final boolean COMMENT_HIDDEN_FROM_AUTO_COMPLETE = false;
    private static final boolean COST_CENTER_HIDDEN_FROM_AUTO_COMPLETE = false;

    // Class under test
    TripDatabaseAdapter mTripDatabaseAdapter;

    @Mock
    Trip mTrip;

    @Mock
    Cursor mCursor;

    @Mock
    StorageManager mStorageManager;

    @Mock
    UserPreferenceManager mPreferences;

    @Mock
    SyncStateAdapter mSyncStateAdapter;

    @Mock
    SyncState mSyncState, mGetSyncState;

    @Mock
    AutoCompleteMetadata mAutoCompleteMetadata;

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
        final int nameHiddenFromAutoCompleteIndex = 11;
        final int commentHiddenFromAutoCompleteIndex = 12;
        final int costCenterHiddenFromAutoCompleteIndex = 13;

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
        when(mCursor.getColumnIndex("name_hidden_auto_complete")).thenReturn(nameHiddenFromAutoCompleteIndex);
        when(mCursor.getColumnIndex("comment_hidden_auto_complete")).thenReturn(commentHiddenFromAutoCompleteIndex);
        when(mCursor.getColumnIndex("costcenter_hidden_auto_complete")).thenReturn(costCenterHiddenFromAutoCompleteIndex);

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
        when(mCursor.getInt(nameHiddenFromAutoCompleteIndex)).thenReturn(NAME_HIDDEN_FROM_AUTO_COMPLETE ? 1 : 0);
        when(mCursor.getInt(commentHiddenFromAutoCompleteIndex)).thenReturn(COMMENT_HIDDEN_FROM_AUTO_COMPLETE ? 1 : 0);
        when(mCursor.getInt(costCenterHiddenFromAutoCompleteIndex)).thenReturn(COST_CENTER_HIDDEN_FROM_AUTO_COMPLETE ? 1 : 0);


        when(mTrip.getId()).thenReturn(ID);
        when(mTrip.getName()).thenReturn(NAME);
        when(mTrip.getStartDate()).thenReturn(new Date(START_DATE));
        when(mTrip.getEndDate()).thenReturn(new Date(END_DATE));
        when(mTrip.getStartTimeZone()).thenReturn(TimeZone.getTimeZone(START_TIMEZONE));
        when(mTrip.getEndTimeZone()).thenReturn(TimeZone.getTimeZone(END_TIMEZONE));
        when(mTrip.getComment()).thenReturn(COMMENT);
        when(mTrip.getCostCenter()).thenReturn(COST_CENTER);
        when(mTrip.getDefaultCurrencyCode()).thenReturn(CURRENCY_CODE);
        when(mTrip.getSyncState()).thenReturn(mSyncState);
        when(mTrip.getUuid()).thenReturn(TRIP_UUID);
        when(mTrip.getAutoCompleteMetadata()).thenReturn(mAutoCompleteMetadata);
        when(mTrip.getAutoCompleteMetadata().isNameHiddenFromAutoComplete()).thenReturn(NAME_HIDDEN_FROM_AUTO_COMPLETE);
        when(mTrip.getAutoCompleteMetadata().isCommentHiddenFromAutoComplete()).thenReturn(COMMENT_HIDDEN_FROM_AUTO_COMPLETE);
        when(mTrip.getAutoCompleteMetadata().isCostCenterHiddenFromAutoComplete()).thenReturn(COST_CENTER_HIDDEN_FROM_AUTO_COMPLETE);


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
                .setSyncState(mSyncState)
                .setNameHiddenFromAutoComplete(NAME_HIDDEN_FROM_AUTO_COMPLETE)
                .setCommentHiddenFromAutoComplete(COMMENT_HIDDEN_FROM_AUTO_COMPLETE)
                .setCostCenterHiddenFromAutoComplete(COST_CENTER_HIDDEN_FROM_AUTO_COMPLETE)
                .build();
        assertEquals(trip, mTripDatabaseAdapter.read(mCursor));
    }

    @Test
    public void writeUnsynced() throws Exception {
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
        assertEquals(NAME_HIDDEN_FROM_AUTO_COMPLETE, contentValues.getAsBoolean("name_hidden_auto_complete"));
        assertEquals(COMMENT_HIDDEN_FROM_AUTO_COMPLETE, contentValues.getAsBoolean("comment_hidden_auto_complete"));
        assertEquals(COST_CENTER_HIDDEN_FROM_AUTO_COMPLETE, contentValues.getAsBoolean("costcenter_hidden_auto_complete"));

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
        assertEquals(NAME_HIDDEN_FROM_AUTO_COMPLETE, contentValues.getAsBoolean("name_hidden_auto_complete"));
        assertEquals(COMMENT_HIDDEN_FROM_AUTO_COMPLETE, contentValues.getAsBoolean("comment_hidden_auto_complete"));
        assertEquals(COST_CENTER_HIDDEN_FROM_AUTO_COMPLETE, contentValues.getAsBoolean("costcenter_hidden_auto_complete"));
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
                .setSyncState(mGetSyncState)
                .setNameHiddenFromAutoComplete(NAME_HIDDEN_FROM_AUTO_COMPLETE)
                .setCommentHiddenFromAutoComplete(COMMENT_HIDDEN_FROM_AUTO_COMPLETE)
                .setCostCenterHiddenFromAutoComplete(COST_CENTER_HIDDEN_FROM_AUTO_COMPLETE)
                .build();
        assertEquals(trip, mTripDatabaseAdapter.build(mTrip, ID, TRIP_UUID, mock(DatabaseOperationMetadata.class)));
        assertEquals(trip.getSyncState(), mTripDatabaseAdapter.build(mTrip, ID, TRIP_UUID, mock(DatabaseOperationMetadata.class)).getSyncState());
    }

}