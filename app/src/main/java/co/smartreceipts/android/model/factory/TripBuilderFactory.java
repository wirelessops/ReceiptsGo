package co.smartreceipts.android.model.factory;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.common.base.Preconditions;

import java.io.File;
import java.sql.Date;
import java.util.TimeZone;

import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.impl.DefaultTripImpl;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;

/**
 * A {@link co.smartreceipts.android.model.Trip} {@link BuilderFactory}
 * implementation, which will be used to generate instances of {@link co.smartreceipts.android.model.Trip} objects
 */
public final class TripBuilderFactory implements BuilderFactory<Trip> {

    private File _dir;
    private String _comment, _costCenter;
    private Date _startDate, _endDate;
    private TimeZone _startTimeZone, _endTimeZone;
    private PriceCurrency _defaultCurrency;
    private SyncState _syncState;
    private Source _source;

    public TripBuilderFactory() {
        _dir = new File("");
        _comment = "";
        _costCenter = "";
        _defaultCurrency = PriceCurrency.getDefaultCurrency();
        _startDate = new Date(System.currentTimeMillis());
        _endDate = _startDate;
        _source = Source.Undefined;
        _startTimeZone = TimeZone.getDefault();
        _endTimeZone = TimeZone.getDefault();
        _syncState = new DefaultSyncState();
    }

    public TripBuilderFactory(@NonNull Trip trip) {
        _dir = trip.getDirectory();
        _comment = trip.getComment();
        _costCenter = trip.getCostCenter();
        _defaultCurrency = PriceCurrency.getInstance(trip.getDefaultCurrencyCode());
        _startDate = trip.getStartDate();
        _endDate = trip.getEndDate();
        _source = trip.getSource();
        _startTimeZone = trip.getStartTimeZone();
        _endTimeZone = trip.getEndTimeZone();
        _syncState = trip.getSyncState();
    }

    public TripBuilderFactory setDirectory(@NonNull File directory) {
        _dir = directory;
        return this;
    }

    public TripBuilderFactory setStartDate(@NonNull Date startDate) {
        _startDate = Preconditions.checkNotNull(startDate);
        return this;
    }

    public TripBuilderFactory setStartDate(long startDate) {
        _startDate = new Date(startDate);
        return this;
    }

    public TripBuilderFactory setEndDate(@NonNull Date endDate) {
        _endDate = Preconditions.checkNotNull(endDate);
        return this;
    }

    public TripBuilderFactory setEndDate(long endDate) {
        _endDate = new Date(endDate);
        return this;
    }

    public TripBuilderFactory setStartTimeZone(@NonNull TimeZone startTimeZone) {
        _startTimeZone = Preconditions.checkNotNull(startTimeZone);
        return this;
    }

    public TripBuilderFactory setStartTimeZone(@Nullable String timeZoneId) {
        if (timeZoneId != null) {
            _startTimeZone = TimeZone.getTimeZone(timeZoneId);
        }
        return this;
    }

    public TripBuilderFactory setEndTimeZone(@NonNull TimeZone endTimeZone) {
        _endTimeZone = Preconditions.checkNotNull(endTimeZone);
        return this;
    }

    public TripBuilderFactory setEndTimeZone(@Nullable String timeZoneId) {
        if (timeZoneId != null) {
            _endTimeZone = TimeZone.getTimeZone(timeZoneId);
        }
        return this;
    }

    public TripBuilderFactory setDefaultCurrency(@NonNull PriceCurrency currency) {
        _defaultCurrency = Preconditions.checkNotNull(currency);
        return this;
    }

    public TripBuilderFactory setDefaultCurrency(@NonNull String currencyCode) {
        if (TextUtils.isEmpty(currencyCode)) {
            throw new IllegalArgumentException("The currency code cannot be null or empty");
        }
        _defaultCurrency = PriceCurrency.getInstance(currencyCode);
        return this;
    }

    public TripBuilderFactory setDefaultCurrency(@Nullable String currencyCode, @NonNull String missingCodeDefault) {
        if (TextUtils.isEmpty(currencyCode)) {
            _defaultCurrency = PriceCurrency.getInstance(missingCodeDefault);
        } else {
            _defaultCurrency = PriceCurrency.getInstance(currencyCode);
        }
        return this;
    }

    public TripBuilderFactory setComment(@Nullable String comment) {
        _comment = comment != null ? comment : "";
        return this;
    }

    public TripBuilderFactory setCostCenter(@Nullable String costCenter) {
        _costCenter = costCenter != null ? costCenter : "";
        return this;
    }

    public TripBuilderFactory setSourceAsCache() {
        _source = Source.Cache;
        return this;
    }

    public TripBuilderFactory setSyncState(@NonNull SyncState syncState) {
        _syncState = Preconditions.checkNotNull(syncState);
        return this;
    }

    @Override
    @NonNull
    public Trip build() {
        return new DefaultTripImpl(_dir, _startDate, _startTimeZone, _endDate, _endTimeZone, _defaultCurrency, _comment, _costCenter, _source, _syncState);
    }
}
