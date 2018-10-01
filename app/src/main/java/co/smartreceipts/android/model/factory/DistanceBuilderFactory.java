package co.smartreceipts.android.model.factory;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.TimeZone;
import java.util.UUID;

import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Keyed;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;

/**
 * A {@link co.smartreceipts.android.model.Distance} {@link BuilderFactory}
 * implementation, which will be used to generate instances of {@link co.smartreceipts.android.model.Distance} objects
 */
public final class DistanceBuilderFactory implements BuilderFactory<Distance> {

    private static final int ROUNDING_PRECISION = Distance.RATE_PRECISION + 2;

    private int _id;
    private UUID _uuid;
    private Trip _trip;
    private String _location;
    private BigDecimal _distance;
    private Date _date;
    private TimeZone _timezone;
    private BigDecimal _rate;
    private PriceCurrency _currency;
    private String _comment;
    private SyncState _syncState;

    public DistanceBuilderFactory() {
        this(Keyed.MISSING_ID);
    }

    public DistanceBuilderFactory(int id) {
        _id = id;
        _uuid = Keyed.Companion.getMISSING_UUID();
        _location = "";
        _distance = BigDecimal.ZERO;
        _date = new Date(System.currentTimeMillis());
        _timezone = TimeZone.getDefault();
        _rate = BigDecimal.ZERO;
        _comment = "";
        _syncState = new DefaultSyncState();
    }

    public DistanceBuilderFactory(@NonNull Distance distance) {
        this(distance.getId(), distance);
    }

    public DistanceBuilderFactory(int id, @NonNull Distance distance) {
        _id = id;
        _uuid = distance.getUuid();
        _trip = distance.getTrip();
        _location = distance.getLocation();
        _distance = distance.getDistance();
        _date = distance.getDate();
        _timezone = distance.getTimeZone();
        _rate = distance.getRate();
        _currency = distance.getPrice().getCurrency();
        _comment = distance.getComment();
        _syncState = distance.getSyncState();

        // Clean up data here if this is from an import that might break things
        if (_location == null) {
            _location = "";
        }
        if (_comment == null) {
            _comment = "";
        }
    }

    public DistanceBuilderFactory setUuid(@NonNull UUID uuid) {
        _uuid = uuid;
        return this;
    }

    public DistanceBuilderFactory setTrip(final Trip trip) {
        _trip = trip;
        return this;
    }

    public DistanceBuilderFactory setLocation(@NonNull String location) {
        _location = Preconditions.checkNotNull(location);
        return this;
    }

    public DistanceBuilderFactory setDistance(BigDecimal distance) {
        _distance = distance;
        return this;
    }

    public DistanceBuilderFactory setDistance(double distance) {
        _distance = BigDecimal.valueOf(distance);
        return this;
    }

    public DistanceBuilderFactory setDate(Date date) {
        _date = date;
        return this;
    }

    public DistanceBuilderFactory setDate(long date) {
        _date = new Date(date);
        return this;
    }

    public DistanceBuilderFactory setTimezone(@Nullable String timezone) {
        // Our distance table doesn't have a default timezone, so protect for nulls
        if (timezone != null) {
            _timezone = TimeZone.getTimeZone(timezone);
        }
        return this;
    }

    public DistanceBuilderFactory setTimezone(TimeZone timezone) {
        _timezone = timezone;
        return this;
    }

    public DistanceBuilderFactory setRate(BigDecimal rate) {
        _rate = rate;
        return this;
    }

    public DistanceBuilderFactory setRate(double rate) {
        _rate = BigDecimal.valueOf(rate);
        return this;
    }

    public DistanceBuilderFactory setCurrency(PriceCurrency currency) {
        _currency = currency;
        return this;
    }

    public DistanceBuilderFactory setCurrency(@NonNull String currencyCode) {
        if (TextUtils.isEmpty(currencyCode)) {
            throw new IllegalArgumentException("The currency code cannot be null or empty");
        }
        _currency = PriceCurrency.getInstance(currencyCode);
        return this;
    }

    public DistanceBuilderFactory setComment(@Nullable String comment) {
        _comment = comment != null ? comment : "";
        return this;
    }

    public DistanceBuilderFactory setSyncState(@NonNull SyncState syncState) {
        _syncState = Preconditions.checkNotNull(syncState);
        return this;
    }

    @Override
    @NonNull
    public Distance build() {
        final BigDecimal scaledDistance = _distance.setScale(ROUNDING_PRECISION, RoundingMode.HALF_UP);
        final BigDecimal scaledRate = _rate.setScale(ROUNDING_PRECISION, RoundingMode.HALF_UP);

        final int precision = ModelUtils.getDecimalFormattedValue(_distance.multiply(_rate), Distance.RATE_PRECISION).endsWith("0") ? Price.DEFAULT_DECIMAL_PRECISION : Distance.RATE_PRECISION;
        Price price = new PriceBuilderFactory().setCurrency(_currency).setPrice(_distance.multiply(_rate)).setDecimalPrecision(precision).build();

        return new Distance(_id, _uuid, price, _syncState, _trip, _location, scaledDistance, scaledRate, _date, _timezone, _comment);
    }
}
