package co.smartreceipts.android.filters;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.sql.Date;
import java.util.ArrayList;
import java.util.TimeZone;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.CategoryBuilderFactory;
import co.smartreceipts.android.utils.ReceiptUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class ReceiptFilterTest {

    // Test constants for Price checking
    private static final String PRICE_NORMAL = "100.00";
    private static final String PRICE_HIGH = "150.00";
    private static final String PRICE_LOW = "50.00";

    // Test constants for Date checking
    private static final TimeZone TZ = ReceiptUtils.Constants.TIMEZONE;
    private static final long MILLIS = new java.util.Date().getTime();
    private static final Date NOW = new Date(MILLIS);
    private static final Date FUTURE = new Date(MILLIS + 1000);
    private static final Date PAST = new Date(MILLIS - 1000);

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }


    @Test
    public void receiptCategoryFilterTest() throws JSONException {
        final Receipt receipt1 = ReceiptUtils.newDefaultReceiptBuilderFactory(context).build();
        final Receipt receipt2 = ReceiptUtils.newDefaultReceiptBuilderFactory(context)
                .setCategory(new CategoryBuilderFactory().setName("BAD Category").build()).build();
        final ReceiptCategoryFilter filter = new ReceiptCategoryFilter(ReceiptUtils.Constants.CATEGORY.getName());
        assertTrue(filter.accept(receipt1));
        assertFalse(filter.accept(receipt2));
        assertEquals(filter, FilterFactory.getReceiptFilter(filter.getJsonRepresentation())); // Confirm we can properly recreate
        assertEquals(filter.getNameResource(), co.smartreceipts.android.R.string.filter_name_receipt_category);
        assertEquals(filter.getType(), FilterType.String);
    }

    @Test
    public void receiptIsReimbursableFilterTest() throws JSONException {
        final Receipt receipt1 = ReceiptUtils.newDefaultReceiptBuilderFactory(context).setIsReimbursable(true).build();
        final Receipt receipt2 = ReceiptUtils.newDefaultReceiptBuilderFactory(context).setIsReimbursable(false).build();
        final ReceiptIsReimbursableFilter filter = new ReceiptIsReimbursableFilter();

        assertTrue(filter.accept(receipt1));
        assertFalse(filter.accept(receipt2));
        assertEquals(filter, FilterFactory.getReceiptFilter(filter.getJsonRepresentation()));
        assertEquals(filter.getNameResource(), co.smartreceipts.android.R.string.filter_name_receipt_reimbursable);
        assertEquals(filter.getType(), FilterType.Boolean);
    }

    @Test
    public void receiptIsSelectedFilterTest() throws JSONException {
        final Receipt receipt1 = ReceiptUtils.newDefaultReceiptBuilderFactory(context).setIsSelected(true).build();
        final Receipt receipt2 = ReceiptUtils.newDefaultReceiptBuilderFactory(context).setIsSelected(false).build();
        final ReceiptSelectedFilter filter = new ReceiptSelectedFilter();

        assertTrue(filter.accept(receipt1));
        assertFalse(filter.accept(receipt2));
        assertEquals(filter, FilterFactory.getReceiptFilter(filter.getJsonRepresentation()));
        assertEquals(filter.getNameResource(), co.smartreceipts.android.R.string.filter_name_receipt_selected);
        assertEquals(filter.getType(), FilterType.Boolean);
    }

    @Test
    public void receiptMinimumPriceFilterTest() throws JSONException {
        final Receipt receiptNormal = ReceiptUtils.newDefaultReceiptBuilderFactory(context).setPrice(PRICE_NORMAL).build();
        final Receipt receiptHigh = ReceiptUtils.newDefaultReceiptBuilderFactory(context).setPrice(PRICE_HIGH).build();
        final Receipt receiptLow = ReceiptUtils.newDefaultReceiptBuilderFactory(context).setPrice(PRICE_LOW).build();

        final ReceiptMinimumPriceFilter filter = new ReceiptMinimumPriceFilter(
                Float.parseFloat(PRICE_NORMAL), ReceiptUtils.Constants.CURRENCY_CODE);

        assertTrue(filter.accept(receiptNormal));
        assertTrue(filter.accept(receiptHigh));
        assertFalse(filter.accept(receiptLow));
        assertEquals(filter, FilterFactory.getReceiptFilter(filter.getJsonRepresentation()));
        assertEquals(filter.getNameResource(), co.smartreceipts.android.R.string.filter_name_receipt_min_price);
        assertEquals(filter.getType(), FilterType.Float);
    }

    @Test
    public void receiptMaximumPriceFilterTest() throws JSONException {
        final Receipt receiptNormal = ReceiptUtils.newDefaultReceiptBuilderFactory(context).setPrice(PRICE_NORMAL).build();
        final Receipt receiptHigh = ReceiptUtils.newDefaultReceiptBuilderFactory(context).setPrice(PRICE_HIGH).build();
        final Receipt receiptLow = ReceiptUtils.newDefaultReceiptBuilderFactory(context).setPrice(PRICE_LOW).build();

        final ReceiptMaximumPriceFilter filter = new ReceiptMaximumPriceFilter(
                Float.parseFloat(PRICE_NORMAL), ReceiptUtils.Constants.CURRENCY_CODE);

        assertTrue(filter.accept(receiptNormal));
        assertFalse(filter.accept(receiptHigh));
        assertTrue(filter.accept(receiptLow));
        assertEquals(filter, FilterFactory.getReceiptFilter(filter.getJsonRepresentation()));
        assertEquals(filter.getNameResource(), co.smartreceipts.android.R.string.filter_name_receipt_max_price);
        assertEquals(filter.getType(), FilterType.Float);
    }

    @Test
    public void receiptOnOrAfterDayFilterTest() throws JSONException {
        final Receipt receiptNow = ReceiptUtils.newDefaultReceiptBuilderFactory(context).setDate(NOW).build();
        final Receipt receiptFuture = ReceiptUtils.newDefaultReceiptBuilderFactory(context).setDate(FUTURE).build();
        final Receipt receiptPast = ReceiptUtils.newDefaultReceiptBuilderFactory(context).setDate(PAST).build();
        final ReceiptOnOrAfterDayFilter filter = new ReceiptOnOrAfterDayFilter(NOW, TZ);

        assertTrue(filter.accept(receiptNow));
        assertTrue(filter.accept(receiptFuture));
        assertFalse(filter.accept(receiptPast));
        assertEquals(filter, FilterFactory.getReceiptFilter(filter.getJsonRepresentation()));
        assertEquals(filter.getNameResource(), co.smartreceipts.android.R.string.filter_name_receipt_on_or_after);
        assertEquals(filter.getType(), FilterType.Date);
    }

    @Test
    public void receiptOnOrBeforeDayFilterTest() throws JSONException {
        final Receipt receiptNow = ReceiptUtils.newDefaultReceiptBuilderFactory(context).setDate(NOW).build();
        final Receipt receiptFuture = ReceiptUtils.newDefaultReceiptBuilderFactory(context).setDate(FUTURE).build();
        final Receipt receiptPast = ReceiptUtils.newDefaultReceiptBuilderFactory(context).setDate(PAST).build();
        final ReceiptOnOrBeforeDayFilter filter = new ReceiptOnOrBeforeDayFilter(NOW, TZ);

        assertTrue(filter.accept(receiptNow));
        assertFalse(filter.accept(receiptFuture));
        assertTrue(filter.accept(receiptPast));
        assertEquals(filter, FilterFactory.getReceiptFilter(filter.getJsonRepresentation()));
        assertEquals(filter.getNameResource(), co.smartreceipts.android.R.string.filter_name_receipt_on_or_before);
        assertEquals(filter.getType(), FilterType.Date);
    }

    @Test
    public void receiptOrFilterTest() throws JSONException {
        final String category2 = "cat2";
        final Receipt receipt1 = ReceiptUtils.newDefaultReceiptBuilderFactory(context).build();
        final Receipt receipt2 = ReceiptUtils.newDefaultReceiptBuilderFactory(context)
                .setCategory(new CategoryBuilderFactory().setName(category2).build()).build();
        final Receipt receipt3 = ReceiptUtils.newDefaultReceiptBuilderFactory(context)
                .setCategory(new CategoryBuilderFactory().setName("BAD Category").build()).build();
        final ReceiptCategoryFilter filter1 = new ReceiptCategoryFilter(ReceiptUtils.Constants.CATEGORY.getName());
        final ReceiptCategoryFilter filter2 = new ReceiptCategoryFilter(category2);
        final ReceiptOrFilter orFilter = new ReceiptOrFilter();
        orFilter.or(filter1);
        orFilter.or(filter2);
        assertTrue(orFilter.accept(receipt1));
        assertTrue(orFilter.accept(receipt2));
        assertFalse(orFilter.accept(receipt3));
        assertEquals(orFilter, FilterFactory.getReceiptFilter(orFilter.getJsonRepresentation())); // Confirm we can properly recreate
        assertEquals(orFilter.getNameResource(), co.smartreceipts.android.R.string.filter_name_or);
        assertEquals(orFilter.getType(), FilterType.Composite);
    }

    @Test
    public void receiptAndFilterTest() throws JSONException {
        final Receipt receipt = ReceiptUtils.newDefaultReceiptBuilderFactory(context).build();

        final ReceiptIsReimbursableFilter trueFilter1 = new ReceiptIsReimbursableFilter();
        final ReceiptCategoryFilter trueFilter2 = new ReceiptCategoryFilter(ReceiptUtils.Constants.CATEGORY.getName());
        final ReceiptCategoryFilter falseFilter = new ReceiptCategoryFilter("BAD Category");

        final ReceiptAndFilter andFilterGood = new ReceiptAndFilter();
        final ReceiptAndFilter andFilterBad = new ReceiptAndFilter();
        andFilterGood.and(trueFilter1).and(trueFilter2);
        andFilterBad.and(trueFilter1).and(trueFilter2).and(falseFilter);

        assertTrue(andFilterGood.accept(receipt));
        assertFalse(andFilterBad.accept(receipt));
        assertEquals(andFilterGood, FilterFactory.getReceiptFilter(andFilterGood.getJsonRepresentation()));
        assertEquals(andFilterBad, FilterFactory.getReceiptFilter(andFilterBad.getJsonRepresentation()));
        assertEquals(andFilterGood.getNameResource(), co.smartreceipts.android.R.string.filter_name_and);
        assertEquals(andFilterGood.getType(), FilterType.Composite);
    }

    @Test
    public void receiptNotFilterTest() throws JSONException {
        // in this test scenario, we will only accept receiptHigh
        // accept rule: NOT (price <= normal)
        // equivalent to: (price > normal)

        final Receipt receiptNormal = ReceiptUtils.newDefaultReceiptBuilderFactory(context).setPrice(PRICE_NORMAL).build();
        final Receipt receiptHigh = ReceiptUtils.newDefaultReceiptBuilderFactory(context).setPrice(PRICE_HIGH).build();
        final Receipt receiptLow = ReceiptUtils.newDefaultReceiptBuilderFactory(context).setPrice(PRICE_LOW).build();

        final ReceiptMaximumPriceFilter priceFilter = new ReceiptMaximumPriceFilter(Float.parseFloat(PRICE_NORMAL), ReceiptUtils.Constants.CURRENCY_CODE);
        final ReceiptNotFilter notFilter = new ReceiptNotFilter(priceFilter);

        assertFalse(notFilter.accept(receiptNormal));
        assertTrue(notFilter.accept(receiptHigh)); // accepted
        assertFalse(notFilter.accept(receiptLow));

        assertEquals(notFilter, FilterFactory.getReceiptFilter(notFilter.getJsonRepresentation()));
        assertEquals(notFilter.getNameResource(), co.smartreceipts.android.R.string.filter_name_not);
        assertEquals(notFilter.getType(), FilterType.Composite);
    }

    @Test
    public void receiptOrFilterConstructorTest() throws JSONException {
        // in this test scenario,
        // filters constructed with same data but different method should be equal

        final ReceiptCategoryFilter filter1 = new ReceiptCategoryFilter(ReceiptUtils.Constants.CATEGORY.getName());
        final ReceiptCategoryFilter filter2 = new ReceiptCategoryFilter("Just another category");

        // filter 1 -- composited filters added in object instantiation (i.e. constructor)
        final ArrayList<Filter<Receipt>> filters = new ArrayList<>();
        filters.add(filter1);
        filters.add(filter2);
        final ReceiptOrFilter orFilter1 = new ReceiptOrFilter(filters);

        // filter 2 -- composited filters added after object instantiation
        final ReceiptOrFilter orFilter2 = new ReceiptOrFilter();
        orFilter2.or(filter1);
        orFilter2.or(filter2);

        assertEquals(orFilter1, orFilter2);
        assertEquals(orFilter1, FilterFactory.getReceiptFilter(orFilter1.getJsonRepresentation()));
        assertEquals(orFilter2, FilterFactory.getReceiptFilter(orFilter2.getJsonRepresentation()));
    }

    @Test
    public void receiptAndFilterConstructorTest() throws JSONException {
        // in this test scenario,
        // filters constructed with same data but different method should be equal

        final ReceiptCategoryFilter filter1 = new ReceiptCategoryFilter(ReceiptUtils.Constants.CATEGORY.getName());
        final ReceiptCategoryFilter filter2 = new ReceiptCategoryFilter("Just another category");

        // filter 1 -- composited filters added in object instantiation (i.e. constructor)
        final ArrayList<Filter<Receipt>> filters = new ArrayList<>();
        filters.add(filter1);
        filters.add(filter2);
        final ReceiptAndFilter andFilter1 = new ReceiptAndFilter(filters);

        // filter 2 -- composited filters added after object instantiation
        final ReceiptAndFilter andFilter2 = new ReceiptAndFilter();
        andFilter2.and(filter1);
        andFilter2.and(filter2);

        assertEquals(andFilter1, andFilter2);
        assertEquals(andFilter1, FilterFactory.getReceiptFilter(andFilter1.getJsonRepresentation()));
        assertEquals(andFilter2, FilterFactory.getReceiptFilter(andFilter2.getJsonRepresentation()));
    }
}
