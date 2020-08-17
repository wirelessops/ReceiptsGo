package co.smartreceipts.android.filters;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.TimeZone;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.impl.SinglePriceImpl;
import co.smartreceipts.android.utils.TripUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class TripFilterTest {

    // Test constants for Price checking
    private static final Price PRICE_NORMAL = new SinglePriceImpl(new BigDecimal(100.00d), TripUtils.Constants.CURRENCY, new ExchangeRateBuilderFactory().setBaseCurrency(TripUtils.Constants.CURRENCY).build());
    private static final Price PRICE_HIGH = new SinglePriceImpl(new BigDecimal(150.00d), TripUtils.Constants.CURRENCY, new ExchangeRateBuilderFactory().setBaseCurrency(TripUtils.Constants.CURRENCY).build());
    private static final Price PRICE_LOW = new SinglePriceImpl(new BigDecimal(50.00d), TripUtils.Constants.CURRENCY, new ExchangeRateBuilderFactory().setBaseCurrency(TripUtils.Constants.CURRENCY).build());

    // Test constants for Date checking
    private static final TimeZone TZ = TimeZone.getTimeZone("America/New_York");
    private static final long MILLIS = System.currentTimeMillis();
    private static final Date NOW = new Date(MILLIS);
    private static final Date FUTURE = new Date(MILLIS + 1000);
    private static final Date PAST = new Date(MILLIS - 1000);

    @Test
    public void tripMinimumPriceFilterTest() throws JSONException {
        final Trip tripNormal = TripUtils.newDefaultTripBuilderFactory().build();
        final Trip tripHigh = TripUtils.newDefaultTripBuilderFactory().build();
        final Trip tripLow = TripUtils.newDefaultTripBuilderFactory().build();
        tripNormal.setPrice(PRICE_NORMAL);
        tripHigh.setPrice(PRICE_HIGH);
        tripLow.setPrice(PRICE_LOW);

        final TripMinimumPriceFilter filter = new TripMinimumPriceFilter(PRICE_NORMAL.getPriceAsFloat(), TripUtils.Constants.CURRENCY_CODE);

        assertTrue(filter.accept(tripNormal));
        assertTrue(filter.accept(tripHigh));
        assertFalse(filter.accept(tripLow));
        assertEquals(filter, FilterFactory.getTripFilter(filter.getJsonRepresentation()));
        assertEquals(filter.getNameResource(), co.smartreceipts.android.R.string.filter_name_trip_min_price);
        assertEquals(filter.getType(), FilterType.Float);
    }

    @Test
    public void tripMaximumPriceFilterTest() throws JSONException {
        final Trip tripNormal = TripUtils.newDefaultTripBuilderFactory().build();
        final Trip tripHigh = TripUtils.newDefaultTripBuilderFactory().build();
        final Trip tripLow = TripUtils.newDefaultTripBuilderFactory().build();
        tripNormal.setPrice(PRICE_NORMAL);
        tripHigh.setPrice(PRICE_HIGH);
        tripLow.setPrice(PRICE_LOW);

        final TripMaximumPriceFilter filter = new TripMaximumPriceFilter(PRICE_NORMAL.getPriceAsFloat(), TripUtils.Constants.CURRENCY_CODE);

        assertTrue(filter.accept(tripNormal));
        assertFalse(filter.accept(tripHigh));
        assertTrue(filter.accept(tripLow));
        assertEquals(filter, FilterFactory.getTripFilter(filter.getJsonRepresentation()));
        assertEquals(filter.getNameResource(), co.smartreceipts.android.R.string.filter_name_trip_max_price);
        assertEquals(filter.getType(), FilterType.Float);
    }

    @Test
    public void tripStartsOnOrAfterDayFilterTest() throws JSONException {
        final Trip tripNow = TripUtils.newDefaultTripBuilderFactory().setStartDate(NOW).build();
        final Trip tripFuture = TripUtils.newDefaultTripBuilderFactory().setStartDate(FUTURE).build();
        final Trip tripPast = TripUtils.newDefaultTripBuilderFactory().setStartDate(PAST).build();
        final TripStartsOnOrAfterDayFilter filter = new TripStartsOnOrAfterDayFilter(NOW, TZ);

        assertTrue(filter.accept(tripNow));
        assertTrue(filter.accept(tripFuture));
        assertFalse(filter.accept(tripPast));
        assertEquals(filter, FilterFactory.getTripFilter(filter.getJsonRepresentation()));
        assertEquals(filter.getNameResource(), co.smartreceipts.android.R.string.filter_name_trip_starts_on_or_after);
        assertEquals(filter.getType(), FilterType.Date);
    }

    @Test
    public void tripStartsOnOrBeforeDayFilterTest() throws JSONException {
        final Trip tripNow = TripUtils.newDefaultTripBuilderFactory().setStartDate(NOW).build();
        final Trip tripFuture = TripUtils.newDefaultTripBuilderFactory().setStartDate(FUTURE).build();
        final Trip tripPast = TripUtils.newDefaultTripBuilderFactory().setStartDate(PAST).build();
        final TripStartsOnOrBeforeDayFilter filter = new TripStartsOnOrBeforeDayFilter(NOW, TZ);

        assertTrue(filter.accept(tripNow));
        assertFalse(filter.accept(tripFuture));
        assertTrue(filter.accept(tripPast));
        assertEquals(filter, FilterFactory.getTripFilter(filter.getJsonRepresentation()));
        assertEquals(filter.getNameResource(), co.smartreceipts.android.R.string.filter_name_trip_starts_on_or_before);
        assertEquals(filter.getType(), FilterType.Date);
    }

    @Test
    public void tripEndsOnOrAfterDayFilterTest() throws JSONException {
        final Trip tripNow = TripUtils.newDefaultTripBuilderFactory().setEndDate(NOW).build();
        final Trip tripFuture = TripUtils.newDefaultTripBuilderFactory().setEndDate(FUTURE).build();
        final Trip tripPast = TripUtils.newDefaultTripBuilderFactory().setEndDate(PAST).build();
        final TripEndsOnOrAfterDayFilter filter = new TripEndsOnOrAfterDayFilter(NOW, TZ);

        assertTrue(filter.accept(tripNow));
        assertTrue(filter.accept(tripFuture));
        assertFalse(filter.accept(tripPast));
        assertEquals(filter, FilterFactory.getTripFilter(filter.getJsonRepresentation()));
        assertEquals(filter.getNameResource(), co.smartreceipts.android.R.string.filter_name_trip_ends_on_or_after);
        assertEquals(filter.getType(), FilterType.Date);
    }

    @Test
    public void tripEndsOnOrBeforeDayFilterTest() throws JSONException {
        final Trip tripNow = TripUtils.newDefaultTripBuilderFactory().setEndDate(NOW).build();
        final Trip tripFuture = TripUtils.newDefaultTripBuilderFactory().setEndDate(FUTURE).build();
        final Trip tripPast = TripUtils.newDefaultTripBuilderFactory().setEndDate(PAST).build();
        final TripEndsOnOrBeforeDayFilter filter = new TripEndsOnOrBeforeDayFilter(NOW, TZ);

        assertTrue(filter.accept(tripNow));
        assertFalse(filter.accept(tripFuture));
        assertTrue(filter.accept(tripPast));
        assertEquals(filter, FilterFactory.getTripFilter(filter.getJsonRepresentation()));
        assertEquals(filter.getNameResource(), co.smartreceipts.android.R.string.filter_name_trip_ends_on_or_before);
        assertEquals(filter.getType(), FilterType.Date);
    }

    @Test
    public void tripOrFilterTest() throws JSONException {
        // in this test scenario, trip with normal price will NOT be accepted
        // accept rule: price >= high OR price <= LOW

        final Trip tripNormal = TripUtils.newDefaultTripBuilderFactory().build();
        final Trip tripHigh = TripUtils.newDefaultTripBuilderFactory().build();
        final Trip tripLow = TripUtils.newDefaultTripBuilderFactory().build();
        tripNormal.setPrice(PRICE_NORMAL);
        tripHigh.setPrice(PRICE_HIGH);
        tripLow.setPrice(PRICE_LOW);

        final TripMinimumPriceFilter minPrice = new TripMinimumPriceFilter(PRICE_HIGH.getPriceAsFloat(), TripUtils.Constants.CURRENCY_CODE);
        final TripMaximumPriceFilter maxPrice = new TripMaximumPriceFilter(PRICE_LOW.getPriceAsFloat(), TripUtils.Constants.CURRENCY_CODE);
        final TripOrFilter orFilter = new TripOrFilter();
        orFilter.or(minPrice);
        orFilter.or(maxPrice);

        assertTrue(orFilter.accept(tripHigh));
        assertTrue(orFilter.accept(tripLow));
        assertFalse(orFilter.accept(tripNormal)); // rejected

        assertEquals(orFilter, FilterFactory.getTripFilter(orFilter.getJsonRepresentation()));
        assertEquals(orFilter.getNameResource(), co.smartreceipts.android.R.string.filter_name_or);
        assertEquals(orFilter.getType(), FilterType.Composite);
    }

    @Test
    public void tripAndFilterTest() throws JSONException {
        // in this test scenario, we will only accept cheap trip started in the past (i.e. tripPastLow)
        // accept rule: starts on or before past AND price is low

        final Trip tripPastNormal = TripUtils.newDefaultTripBuilderFactory().setStartDate(PAST).build();
        final Trip tripPastHigh = TripUtils.newDefaultTripBuilderFactory().setStartDate(PAST).build();
        final Trip tripPastLow = TripUtils.newDefaultTripBuilderFactory().setStartDate(PAST).build();
        final Trip tripFutureNormal = TripUtils.newDefaultTripBuilderFactory().setStartDate(FUTURE).build();
        final Trip tripFutureHigh = TripUtils.newDefaultTripBuilderFactory().setStartDate(FUTURE).build();
        final Trip tripFutureLow = TripUtils.newDefaultTripBuilderFactory().setStartDate(FUTURE).build();
        tripPastHigh.setPrice(PRICE_HIGH);
        tripPastNormal.setPrice(PRICE_NORMAL);
        tripPastLow.setPrice(PRICE_LOW);
        tripFutureHigh.setPrice(PRICE_HIGH);
        tripFutureNormal.setPrice(PRICE_NORMAL);
        tripFutureLow.setPrice(PRICE_LOW);

        final TripStartsOnOrBeforeDayFilter dateFilter = new TripStartsOnOrBeforeDayFilter(PAST, TripUtils.Constants.START_TIMEZONE);
        final TripMaximumPriceFilter priceFilter = new TripMaximumPriceFilter(PRICE_LOW.getPriceAsFloat(), TripUtils.Constants.CURRENCY_CODE);
        final TripAndFilter andFilter = new TripAndFilter();
        andFilter.and(dateFilter);
        andFilter.and(priceFilter);

        assertFalse(andFilter.accept(tripPastNormal));
        assertFalse(andFilter.accept(tripPastHigh));
        assertTrue(andFilter.accept(tripPastLow)); // accepted
        assertFalse(andFilter.accept(tripFutureNormal));
        assertFalse(andFilter.accept(tripFutureHigh));
        assertFalse(andFilter.accept(tripFutureLow));

        assertEquals(andFilter, FilterFactory.getTripFilter(andFilter.getJsonRepresentation()));
        assertEquals(andFilter.getNameResource(), co.smartreceipts.android.R.string.filter_name_and);
        assertEquals(andFilter.getType(), FilterType.Composite);
    }

    @Test
    public void tripNotFilterTest() throws JSONException {
        // in this test scenario, we will only accept tripHigh
        // accept rule: NOT (price <= normal)
        // equivalent to: (price > normal)

        final Trip tripNormal = TripUtils.newDefaultTripBuilderFactory().build();
        final Trip tripHigh = TripUtils.newDefaultTripBuilderFactory().build();
        final Trip tripLow = TripUtils.newDefaultTripBuilderFactory().build();
        tripNormal.setPrice(PRICE_NORMAL);
        tripHigh.setPrice(PRICE_HIGH);
        tripLow.setPrice(PRICE_LOW);

        final TripMaximumPriceFilter priceFilter = new TripMaximumPriceFilter(PRICE_NORMAL.getPriceAsFloat(), TripUtils.Constants.CURRENCY_CODE);
        final TripNotFilter notFilter = new TripNotFilter(priceFilter);

        assertFalse(notFilter.accept(tripNormal));
        assertTrue(notFilter.accept(tripHigh)); // accepted
        assertFalse(notFilter.accept(tripLow));

        assertEquals(notFilter, FilterFactory.getTripFilter(notFilter.getJsonRepresentation()));
        assertEquals(notFilter.getNameResource(), co.smartreceipts.android.R.string.filter_name_not);
        assertEquals(notFilter.getType(), FilterType.Composite);
    }

    @Test
    public void tripAndFilterConstructorTest() throws JSONException {
        // in this test scenario,
        // filters constructed with same data but different method should be equal

        final TripStartsOnOrBeforeDayFilter dateFilter = new TripStartsOnOrBeforeDayFilter(PAST, TripUtils.Constants.START_TIMEZONE);
        final TripMaximumPriceFilter priceFilter = new TripMaximumPriceFilter(PRICE_LOW.getPriceAsFloat(), TripUtils.Constants.CURRENCY_CODE);

        // filter 1 -- composited filters added in object instantiation (i.e. constructor)
        final ArrayList<Filter<Trip>> filters = new ArrayList<>();
        filters.add(dateFilter);
        filters.add(priceFilter);
        final TripAndFilter filter1 = new TripAndFilter(filters);

        // filter 2 -- composited filters added after object instantiation
        final TripAndFilter filter2 = new TripAndFilter();
        filter2.and(dateFilter);
        filter2.and(priceFilter);

        assertEquals(filter1, filter2);
        assertEquals(filter1, FilterFactory.getTripFilter(filter1.getJsonRepresentation()));
        assertEquals(filter2, FilterFactory.getTripFilter(filter2.getJsonRepresentation()));
    }

    @Test
    public void tripOrFilterConstructorTest() throws JSONException {
        // in this test scenario,
        // filters constructed with same data but different method should be equal

        final TripStartsOnOrBeforeDayFilter dateFilter = new TripStartsOnOrBeforeDayFilter(PAST, TripUtils.Constants.START_TIMEZONE);
        final TripMaximumPriceFilter priceFilter = new TripMaximumPriceFilter(PRICE_LOW.getPriceAsFloat(), TripUtils.Constants.CURRENCY_CODE);

        // filter 1 -- composited filters added in object instantiation (i.e. constructor)
        final ArrayList<Filter<Trip>> filters = new ArrayList<>();
        filters.add(dateFilter);
        filters.add(priceFilter);
        final TripOrFilter filter1 = new TripOrFilter(filters);

        // filter 2 -- composited filters added after object instantiation
        final TripOrFilter filter2 = new TripOrFilter();
        filter2.or(dateFilter);
        filter2.or(priceFilter);

        assertEquals(filter1, filter2);
        assertEquals(filter1, FilterFactory.getTripFilter(filter1.getJsonRepresentation()));
        assertEquals(filter2, FilterFactory.getTripFilter(filter2.getJsonRepresentation()));
    }
}