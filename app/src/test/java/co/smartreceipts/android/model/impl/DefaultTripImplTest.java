package co.smartreceipts.android.model.impl;

import android.os.Parcel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.sql.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import co.smartreceipts.android.DefaultObjects;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.utils.TestLocaleToggler;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class DefaultTripImplTest {

    private static final String NAME = "TripName";
    private static final File DIRECTORY = new File(new File(NAME).getAbsolutePath());
    private static final Date START_DATE = new Date(1409703721000L);
    private static final Date END_DATE = new Date(1409783794000L);
    private static final TimeZone START_TIMEZONE = TimeZone.getTimeZone(TimeZone.getAvailableIDs()[0]);
    private static final TimeZone END_TIMEZONE = TimeZone.getTimeZone(TimeZone.getAvailableIDs()[1]);
    private static final String COMMENT = "Comment";
    private static final String COST_CENTER = "Cost Center";
    private static final PriceCurrency CURRENCY = PriceCurrency.getInstance("USD");

    // Class under test
    DefaultTripImpl mTrip;

    SyncState mSyncState;

    @Mock
    Price mPrice;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        TestLocaleToggler.setDefaultLocale(Locale.US);
        mSyncState = DefaultObjects.newDefaultSyncState();
        mTrip = new DefaultTripImpl(DIRECTORY, START_DATE, START_TIMEZONE, END_DATE, END_TIMEZONE, CURRENCY, COMMENT, COST_CENTER, Source.Undefined, mSyncState);
    }

    @After
    public void tearDown() throws Exception {
        TestLocaleToggler.resetDefaultLocale();
    }

    @Test
    public void getName() {
        assertEquals(NAME, mTrip.getName());
    }

    @Test
    public void getDirectory() {
        assertEquals(DIRECTORY, mTrip.getDirectory());
    }

    @Test
    public void getDirectoryPath() {
        assertEquals(DIRECTORY.getAbsolutePath(), mTrip.getDirectoryPath());
    }

    @Test
    public void getStartDate() {
        assertEquals(START_DATE, mTrip.getStartDate());
    }

    @Test
    public void getStartTimeZone() {
        assertEquals(START_TIMEZONE, mTrip.getStartTimeZone());
    }

    @Test
    public void getEndDate() {
        assertEquals(END_DATE, mTrip.getEndDate());
    }

    @Test
    public void getEndTimeZone() {
        assertEquals(END_TIMEZONE, mTrip.getEndTimeZone());
    }

    @Test
    public void isDateInsideTripBounds() {
        assertTrue(mTrip.isDateInsideTripBounds(START_DATE));
        assertTrue(mTrip.isDateInsideTripBounds(END_DATE));
        assertTrue(mTrip.isDateInsideTripBounds(new Date(START_DATE.getTime() + 10)));
        assertTrue(mTrip.isDateInsideTripBounds(new Date(END_DATE.getTime() - 10)));

        assertFalse(mTrip.isDateInsideTripBounds(new Date(START_DATE.getTime() - TimeUnit.DAYS.toMillis(2))));
        assertFalse(mTrip.isDateInsideTripBounds(new Date(END_DATE.getTime() + TimeUnit.DAYS.toMillis(2))));
    }

    @Test
    public void getPrice() {
        assertNotNull(mTrip.getPrice());
        mTrip.setPrice(mPrice);
        assertEquals(mPrice, mTrip.getPrice());
    }

    @Test
    public void getDailySubTotal() {
        assertNotNull(mTrip.getDailySubTotal());
        mTrip.setDailySubTotal(mPrice);
        assertEquals(mPrice, mTrip.getDailySubTotal());
    }

    @Test
    public void getTripCurrency() {
        assertEquals(CURRENCY, mTrip.getTripCurrency());
    }

    @Test
    public void getDefaultCurrencyCode() {
        assertEquals(CURRENCY.getCurrencyCode(), mTrip.getDefaultCurrencyCode());
    }

    @Test
    public void getComment() {
        assertEquals(COMMENT, mTrip.getComment());
    }

    @Test
    public void getCostCenter() {
        assertEquals(COST_CENTER, mTrip.getCostCenter());
    }

    @Test
    public void getFilter() {
        assertNull(mTrip.getFilter());
    }

    @Test
    public void getSyncState() {
        assertEquals(mSyncState, mTrip.getSyncState());
    }

    @Test
    public void compareTo() {
        assertTrue(mTrip.compareTo(new DefaultTripImpl(DIRECTORY, START_DATE, START_TIMEZONE, END_DATE, END_TIMEZONE, CURRENCY, COMMENT, COST_CENTER, Source.Undefined, mSyncState)) == 0);
        assertTrue(mTrip.compareTo(new DefaultTripImpl(DIRECTORY, START_DATE, START_TIMEZONE, new Date(END_DATE.getTime()*2), END_TIMEZONE, CURRENCY, COMMENT, COST_CENTER, Source.Undefined, mSyncState)) > 0);
        assertTrue(mTrip.compareTo(new DefaultTripImpl(DIRECTORY, START_DATE, START_TIMEZONE, new Date(0), END_TIMEZONE, CURRENCY, COMMENT, COST_CENTER, Source.Undefined, mSyncState)) < 0);
    }

    @Test
    public void equals() {
        assertEquals(mTrip, mTrip);
        assertEquals(mTrip, new DefaultTripImpl(DIRECTORY, START_DATE, START_TIMEZONE, END_DATE, END_TIMEZONE, CURRENCY, COMMENT, COST_CENTER, Source.Undefined, mSyncState));
        assertThat(mTrip, not(equalTo(new Object())));
        assertThat(mTrip, not(equalTo(mock(Trip.class))));
        assertThat(mTrip, not(equalTo(new DefaultTripImpl(new File(""), START_DATE, START_TIMEZONE, END_DATE, END_TIMEZONE, CURRENCY, COMMENT, COST_CENTER, Source.Undefined, mSyncState))));
        assertThat(mTrip, not(equalTo(new DefaultTripImpl(DIRECTORY, new Date(System.currentTimeMillis()), START_TIMEZONE, END_DATE, END_TIMEZONE, CURRENCY, COMMENT, COST_CENTER, Source.Undefined, mSyncState))));
        assertThat(mTrip, not(equalTo(new DefaultTripImpl(DIRECTORY, START_DATE, TimeZone.getTimeZone(TimeZone.getAvailableIDs()[2]), END_DATE, END_TIMEZONE, CURRENCY, COMMENT, COST_CENTER, Source.Undefined, mSyncState))));
        assertThat(mTrip, not(equalTo(new DefaultTripImpl(DIRECTORY, START_DATE, START_TIMEZONE, new Date(System.currentTimeMillis()), END_TIMEZONE, CURRENCY, COMMENT, COST_CENTER, Source.Undefined, mSyncState))));
        assertThat(mTrip, not(equalTo(new DefaultTripImpl(DIRECTORY, START_DATE, START_TIMEZONE, END_DATE, TimeZone.getTimeZone(TimeZone.getAvailableIDs()[2]), CURRENCY, COMMENT, COST_CENTER, Source.Undefined, mSyncState))));
        assertThat(mTrip, not(equalTo(new DefaultTripImpl(DIRECTORY, START_DATE, START_TIMEZONE, END_DATE, END_TIMEZONE, PriceCurrency.getInstance("EUR"), COMMENT, COST_CENTER, Source.Undefined, mSyncState))));
        assertThat(mTrip, not(equalTo(new DefaultTripImpl(DIRECTORY, START_DATE, START_TIMEZONE, END_DATE, END_TIMEZONE, CURRENCY, "bad", COST_CENTER, Source.Undefined, mSyncState))));
        assertThat(mTrip, not(equalTo(new DefaultTripImpl(DIRECTORY, START_DATE, START_TIMEZONE, END_DATE, END_TIMEZONE, CURRENCY, COMMENT, "bad", Source.Undefined, mSyncState))));

        // Special equals cases (source, price, and daily subtotal don't cound):
        final Trip tripWithPrice = new DefaultTripImpl(DIRECTORY, START_DATE, START_TIMEZONE, END_DATE, END_TIMEZONE, CURRENCY, COMMENT, COST_CENTER, Source.Undefined, mSyncState);
        final Trip tripWithDailySubTotal = new DefaultTripImpl(DIRECTORY, START_DATE, START_TIMEZONE, END_DATE, END_TIMEZONE, CURRENCY, COMMENT, COST_CENTER, Source.Undefined, mSyncState);
        tripWithPrice.setPrice(mPrice);
        tripWithDailySubTotal.setDailySubTotal(mPrice);
        assertEquals(mTrip, new DefaultTripImpl(DIRECTORY, START_DATE, START_TIMEZONE, END_DATE, END_TIMEZONE, CURRENCY, COMMENT, COST_CENTER, Source.Parcel, mSyncState));
        assertEquals(mTrip, tripWithPrice);
        assertEquals(mTrip, tripWithDailySubTotal);
    }

    @Test
    public void parcelEquality() {
        final Parcel parcel = Parcel.obtain();
        mTrip.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        final DefaultTripImpl trip = DefaultTripImpl.CREATOR.createFromParcel(parcel);
        assertNotNull(trip);
        assertEquals(trip, mTrip);
    }

}