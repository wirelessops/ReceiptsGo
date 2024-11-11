package com.wops.receiptsgo.model.factory;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import org.joda.money.CurrencyUnit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.TimeZone;
import java.util.UUID;

import com.wops.receiptsgo.date.DisplayableDate;
import com.wops.receiptsgo.model.AutoCompleteMetadata;
import com.wops.receiptsgo.model.Distance;
import com.wops.receiptsgo.model.Keyed;
import com.wops.receiptsgo.model.PaymentMethod;
import com.wops.receiptsgo.model.Price;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.model.utils.CurrencyUtils;
import com.wops.core.sync.model.SyncState;
import com.wops.core.sync.model.impl.DefaultSyncState;

/**
 * A {@link com.wops.receiptsgo.model.Distance} {@link BuilderFactory}
 * implementation, which will be used to generate instances of {@link com.wops.receiptsgo.model.Distance} objects
 */
public final class DistanceBuilderFactory implements BuilderFactory<Distance> {

    private static final int ROUNDING_PRECISION = Distance.RATE_PRECISION + 2;

    private int id;
    private UUID uuid;
    private Trip trip;
    private String location;
    private BigDecimal distance;
    private Date date;
    private TimeZone timeZone;
    private BigDecimal rate;
    private CurrencyUnit currency;
    private String comment;
    private PaymentMethod paymentMethod;
    private SyncState syncState;
    private AutoCompleteMetadata autoCompleteMetadata;

    public DistanceBuilderFactory() {
        this(Keyed.MISSING_ID);
    }

    public DistanceBuilderFactory(int id) {
        this.id = id;
        uuid = Keyed.Companion.getMISSING_UUID();
        location = "";
        distance = BigDecimal.ZERO;
        date = new Date(System.currentTimeMillis());
        timeZone = TimeZone.getDefault();
        rate = BigDecimal.ZERO;
        currency = CurrencyUtils.INSTANCE.getDefaultCurrency();
        comment = "";
        syncState = new DefaultSyncState();
        autoCompleteMetadata = new AutoCompleteMetadata(false, false, false, false);
    }

    public DistanceBuilderFactory(@NonNull Distance distance) {
        this(distance.getId(), distance);
    }

    public DistanceBuilderFactory(int id, @NonNull Distance distance) {
        this.id = id;
        uuid = distance.getUuid();
        trip = distance.getTrip();
        location = distance.getLocation();
        this.distance = distance.getDistance();
        date = distance.getDate();
        timeZone = distance.getTimeZone();
        rate = distance.getRate();
        currency = distance.getPrice().getCurrency();
        comment = distance.getComment();
        paymentMethod = distance.getPaymentMethod();
        syncState = distance.getSyncState();
        autoCompleteMetadata = distance.getAutoCompleteMetadata();

        // Clean up data here if this is from an import that might break things
        if (location == null) {
            location = "";
        }
        if (comment == null) {
            comment = "";
        }
    }

    public DistanceBuilderFactory setUuid(@NonNull UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public DistanceBuilderFactory setTrip(final Trip trip) {
        this.trip = trip;
        return this;
    }

    public DistanceBuilderFactory setLocation(@NonNull String location) {
        this.location = Preconditions.checkNotNull(location);
        return this;
    }

    public DistanceBuilderFactory setDistance(BigDecimal distance) {
        this.distance = distance;
        return this;
    }

    public DistanceBuilderFactory setDistance(double distance) {
        this.distance = BigDecimal.valueOf(distance);
        return this;
    }

    public DistanceBuilderFactory setDate(Date date) {
        this.date = date;
        return this;
    }

    public DistanceBuilderFactory setDate(long date) {
        this.date = new Date(date);
        return this;
    }

    public DistanceBuilderFactory setTimezone(@Nullable String timezone) {
        // Our distance table doesn't have a default timezone, so protect for nulls
        if (timezone != null) {
            timeZone = TimeZone.getTimeZone(timezone);
        }
        return this;
    }

    public DistanceBuilderFactory setTimezone(TimeZone timezone) {
        timeZone = timezone;
        return this;
    }

    public DistanceBuilderFactory setRate(BigDecimal rate) {
        this.rate = rate;
        return this;
    }

    public DistanceBuilderFactory setRate(double rate) {
        this.rate = BigDecimal.valueOf(rate);
        return this;
    }

    public DistanceBuilderFactory setCurrency(CurrencyUnit currency) {
        this.currency = currency;
        return this;
    }

    public DistanceBuilderFactory setCurrency(@NonNull String currencyCode) {
        if (TextUtils.isEmpty(currencyCode)) {
            throw new IllegalArgumentException("The currency code cannot be null or empty");
        }
        currency = CurrencyUnit.of(currencyCode);
        return this;
    }

    public DistanceBuilderFactory setComment(@Nullable String comment) {
        this.comment = comment != null ? comment : "";
        return this;
    }

    public DistanceBuilderFactory setPaymentMethod(@Nullable PaymentMethod method) {
        this.paymentMethod = method;
        return this;
    }

    public DistanceBuilderFactory setSyncState(@NonNull SyncState syncState) {
        this.syncState = Preconditions.checkNotNull(syncState);
        return this;
    }

    public DistanceBuilderFactory setLocationHiddenFromAutoComplete(boolean isHiddenFromAutoComplete) {
        this.autoCompleteMetadata.setLocationHiddenFromAutoComplete(isHiddenFromAutoComplete);
        return this;
    }

    public DistanceBuilderFactory setCommentHiddenFromAutoComplete(boolean isHiddenFromAutoComplete) {
        this.autoCompleteMetadata.setCommentHiddenFromAutoComplete(isHiddenFromAutoComplete);
        return this;
    }

    @Override
    @NonNull
    public Distance build() {
        final BigDecimal scaledDistance = distance.setScale(ROUNDING_PRECISION, RoundingMode.HALF_UP);
        final BigDecimal scaledRate = rate.setScale(ROUNDING_PRECISION, RoundingMode.HALF_EVEN);

        Price price = new PriceBuilderFactory()
                .setCurrency(currency)
                .setPrice(distance.multiply(rate))
                .build();

        final DisplayableDate displayableDate = new DisplayableDate(date, timeZone);

        return new Distance(id, uuid, price, syncState, trip, location, scaledDistance, scaledRate, displayableDate, comment,
                paymentMethod == null ? PaymentMethod.Companion.getNONE() : paymentMethod, autoCompleteMetadata);
    }
}
