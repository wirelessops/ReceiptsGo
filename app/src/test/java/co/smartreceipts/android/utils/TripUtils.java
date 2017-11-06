package co.smartreceipts.android.utils;

import android.os.Environment;

import junit.framework.Assert;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.TimeZone;

import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.factory.TripBuilderFactory;
import co.smartreceipts.android.model.impl.ImmutablePriceImpl;

public class TripUtils {

    public static class Constants {
        public static final String CURRENCY_CODE = "USD";
        public static final PriceCurrency CURRENCY = PriceCurrency.getInstance(CURRENCY_CODE); // 1
        public static final String DEFAULT_CURRENCY_CODE = "USD";
        public static final PriceCurrency DEFAULT_CURRENCY = PriceCurrency.getInstance(DEFAULT_CURRENCY_CODE); // 1
        public static final long START_DATE_MILLIS = 1409703721000L; // 09/02/2014 @ 8:22EDT
        public static final Date START_DATE = new Date(START_DATE_MILLIS);
        public static final String SLASH_FORMATTED_START_DATE = "Sep/02/2014";
        public static final String DASH_FORMATTED_START_DATE = "Sep-02-2014";
        public static final long END_DATE_MILLIS = 1409703794000L; // 09/02/2014 @ 8:23EDT
        public static final Date END_DATE = new Date(END_DATE_MILLIS);
        public static final String SLASH_FORMATTED_END_DATE = "Sep/02/2014";
        public static final String DASH_FORMATTED_END_DATE = "Sep-02-2014";
        public static final File DIRECTORY = new File(Environment.getExternalStorageDirectory(), "Report");
        public static final String DIRECTORY_NAME = "Report";
        public static final TimeZone START_TIMEZONE = TimeZone.getTimeZone("America/New_York");
        public static final String START_TIMEZONE_CODE = START_TIMEZONE.getID();
        public static final TimeZone END_TIMEZONE = TimeZone.getTimeZone("America/New_York");
        public static final String END_TIMEZONE_CODE = END_TIMEZONE.getID();
        public static final String COMMENT = "Comment";
        public static final String COST_CENTER = "Cost Center";
        public static final double PRICE = 12.55d;
        public static final String DECIMAL_FORMATTED_PRICE = "12.55";
        public static final String CURRENCY_FORMATTED_PRICE = "$12.55";
        public static final double DAILY_SUBTOTAL = 1.25d;
        public static final String DECIMAL_FORMATTED_SUBTOTAL = "1.25";
        public static final String CURRENCY_FORMATTED_SUBTOTAL = "$1.25";
        public static final float MILEAGE = 40.3121f;
    }

    public static TripBuilderFactory newDefaultTripBuilderFactory() {
        final TripBuilderFactory factory = new TripBuilderFactory().setDirectory(Constants.DIRECTORY)
                .setComment(Constants.COMMENT)
                .setCostCenter(Constants.COST_CENTER)
                .setDefaultCurrency(Constants.DEFAULT_CURRENCY)
                .setStartDate(Constants.START_DATE)
                .setStartTimeZone(Constants.START_TIMEZONE)
                .setEndDate(Constants.END_DATE)
                .setEndTimeZone(Constants.END_TIMEZONE);
        return factory;
    }

    public static Trip newDefaultTrip() {
        final Trip trip = newDefaultTripBuilderFactory().build();
        trip.setPrice(new ImmutablePriceImpl(new BigDecimal(Constants.PRICE), Constants.CURRENCY, new ExchangeRateBuilderFactory().setBaseCurrency(Constants.CURRENCY).build()));
        trip.setDailySubTotal(new ImmutablePriceImpl(new BigDecimal(Constants.DAILY_SUBTOTAL), Constants.CURRENCY, new ExchangeRateBuilderFactory().setBaseCurrency(Constants.CURRENCY).build()));
        return trip;
    }

    public static void assertFieldEquality(Trip trip1, Trip trip2) {
        Assert.assertEquals(trip1.getDirectory(), trip2.getDirectory());
        Assert.assertEquals(trip1.getStartDate(), trip2.getStartDate());
        Assert.assertEquals(trip1.getStartTimeZone(), trip2.getStartTimeZone());
        Assert.assertEquals(trip1.getEndDate(), trip2.getEndDate());
        Assert.assertEquals(trip1.getEndTimeZone(), trip2.getEndTimeZone());
        Assert.assertEquals(trip1.getPrice(), trip2.getPrice());
        Assert.assertEquals(trip1.getDailySubTotal(), trip2.getDailySubTotal());
        Assert.assertEquals(trip1.getComment(), trip2.getComment());
        Assert.assertEquals(trip1.getCostCenter(), trip2.getCostCenter());
    }

}
