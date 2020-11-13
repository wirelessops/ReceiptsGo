package co.smartreceipts.android.model.factory;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import org.joda.money.CurrencyUnit;

import java.io.File;
import java.sql.Date;
import java.util.TimeZone;
import java.util.UUID;

import co.smartreceipts.android.date.DisplayableDate;
import co.smartreceipts.android.model.AutoCompleteMetadata;
import co.smartreceipts.android.model.Keyed;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.utils.CurrencyUtils;
import co.smartreceipts.core.sync.model.SyncState;
import co.smartreceipts.core.sync.model.impl.DefaultSyncState;

/**
 * A {@link co.smartreceipts.android.model.Trip} {@link BuilderFactory}
 * implementation, which will be used to generate instances of {@link co.smartreceipts.android.model.Trip} objects
 */
public final class TripBuilderFactory implements BuilderFactory<Trip> {

    private int id;
    private UUID uuid;
    private File dir;
    private String comment, costCenter;
    private Date startDate, endDate;
    private TimeZone startTimeZone, endTimeZone;
    private CurrencyUnit defaultCurrency;
    private SyncState syncState;
    private AutoCompleteMetadata autoCompleteMetadata;

    public TripBuilderFactory() {
        id = Keyed.MISSING_ID;
        uuid = Keyed.Companion.getMISSING_UUID();
        dir = new File("");
        comment = "";
        costCenter = "";
        defaultCurrency = CurrencyUtils.INSTANCE.getDefaultCurrency();
        startDate = new Date(System.currentTimeMillis());
        endDate = startDate;
        startTimeZone = TimeZone.getDefault();
        endTimeZone = TimeZone.getDefault();
        syncState = new DefaultSyncState();
        autoCompleteMetadata = new AutoCompleteMetadata(false, false, false, false);
    }

    public TripBuilderFactory(@NonNull Trip trip) {
        id = trip.getId();
        uuid = trip.getUuid();
        dir = trip.getDirectory();
        comment = trip.getComment();
        costCenter = trip.getCostCenter();
        defaultCurrency = CurrencyUnit.of(trip.getDefaultCurrencyCode());
        startDate = trip.getStartDate();
        endDate = trip.getEndDate();
        startTimeZone = trip.getStartTimeZone();
        endTimeZone = trip.getEndTimeZone();
        syncState = trip.getSyncState();
        autoCompleteMetadata = trip.getAutoCompleteMetadata();
    }

    public TripBuilderFactory setId(int id) {
        this.id = id;
        return this;
    }

    public TripBuilderFactory setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public TripBuilderFactory setDirectory(@NonNull File directory) {
        dir = directory;
        return this;
    }

    public TripBuilderFactory setStartDate(@NonNull Date startDate) {
        this.startDate = Preconditions.checkNotNull(startDate);
        return this;
    }

    public TripBuilderFactory setStartDate(long startDate) {
        this.startDate = new Date(startDate);
        return this;
    }

    public TripBuilderFactory setEndDate(@NonNull Date endDate) {
        this.endDate = Preconditions.checkNotNull(endDate);
        return this;
    }

    public TripBuilderFactory setEndDate(long endDate) {
        this.endDate = new Date(endDate);
        return this;
    }

    public TripBuilderFactory setStartTimeZone(@NonNull TimeZone startTimeZone) {
        this.startTimeZone = Preconditions.checkNotNull(startTimeZone);
        return this;
    }

    public TripBuilderFactory setStartTimeZone(@Nullable String timeZoneId) {
        if (timeZoneId != null) {
            startTimeZone = TimeZone.getTimeZone(timeZoneId);
        }
        return this;
    }

    public TripBuilderFactory setEndTimeZone(@NonNull TimeZone endTimeZone) {
        this.endTimeZone = Preconditions.checkNotNull(endTimeZone);
        return this;
    }

    public TripBuilderFactory setEndTimeZone(@Nullable String timeZoneId) {
        if (timeZoneId != null) {
            endTimeZone = TimeZone.getTimeZone(timeZoneId);
        }
        return this;
    }

    public TripBuilderFactory setDefaultCurrency(@NonNull CurrencyUnit currency) {
        defaultCurrency = Preconditions.checkNotNull(currency);
        return this;
    }

    public TripBuilderFactory setDefaultCurrency(@NonNull String currencyCode) {
        if (TextUtils.isEmpty(currencyCode)) {
            throw new IllegalArgumentException("The currency code cannot be null or empty");
        }

        if (CurrencyUtils.INSTANCE.isCurrencySupported(currencyCode)) {
            defaultCurrency = CurrencyUnit.of(currencyCode);
        }

        return this;
    }

    public TripBuilderFactory setDefaultCurrency(@Nullable String currencyCode, @NonNull String missingCodeDefault) {
        if (TextUtils.isEmpty(currencyCode) || !CurrencyUtils.INSTANCE.isCurrencySupported(currencyCode)) {
            defaultCurrency = CurrencyUnit.of(missingCodeDefault);
        } else {
            defaultCurrency = CurrencyUnit.of(currencyCode);
        }
        return this;
    }

    public TripBuilderFactory setComment(@Nullable String comment) {
        this.comment = comment != null ? comment : "";
        return this;
    }

    public TripBuilderFactory setCostCenter(@Nullable String costCenter) {
        this.costCenter = costCenter != null ? costCenter : "";
        return this;
    }

    public TripBuilderFactory setSyncState(@NonNull SyncState syncState) {
        this.syncState = Preconditions.checkNotNull(syncState);
        return this;
    }

    public TripBuilderFactory setNameHiddenFromAutoComplete(boolean isHiddenFromAutoComplete) {
        this.autoCompleteMetadata.setNameHiddenFromAutoComplete(isHiddenFromAutoComplete);
        return this;
    }

    public TripBuilderFactory setCommentHiddenFromAutoComplete(boolean isHiddenFromAutoComplete) {
        this.autoCompleteMetadata.setCommentHiddenFromAutoComplete(isHiddenFromAutoComplete);
        return this;
    }

    public TripBuilderFactory setCostCenterHiddenFromAutoComplete(boolean isHiddenFromAutoComplete) {
        this.autoCompleteMetadata.setCostCenterHiddenFromAutoComplete(isHiddenFromAutoComplete);
        return this;
    }

    @Override
    @NonNull
    public Trip build() {
        final DisplayableDate startDisplayableDate = new DisplayableDate(startDate, startTimeZone);
        final DisplayableDate endDisplayableDate = new DisplayableDate(endDate, endTimeZone);
        return new Trip(id, uuid, dir, startDisplayableDate, endDisplayableDate, defaultCurrency,
                comment, costCenter, syncState, autoCompleteMetadata);
    }
}
