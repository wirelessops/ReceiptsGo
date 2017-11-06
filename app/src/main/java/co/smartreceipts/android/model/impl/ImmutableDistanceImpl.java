package co.smartreceipts.android.model.impl;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.TimeZone;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;

/**
 * An immutable {@link co.smartreceipts.android.model.Distance} implementation to track distance.
 */
public final class ImmutableDistanceImpl implements Distance {

    private static final int DEFAULT_RATE_DECIMAL_PRECISION = 3;
    private static final int ROUNDING_PRECISION = RATE_PRECISION + 2;

    private final int mId;
    private final Trip mTrip;
    private final String mLocation;
    private final BigDecimal mDistance;
    private final Date mDate;
    private final TimeZone mTimezone;
    private final BigDecimal mRate;
    private final Price mPrice;
    private final String mComment;
    private final SyncState mSyncState;

    public ImmutableDistanceImpl(int id, @NonNull Trip trip, String location, BigDecimal distance, BigDecimal rate, PriceCurrency currency, Date date, TimeZone timeZone, String comment) {
        this(id, trip, location, distance, rate, currency, date, timeZone, comment, new DefaultSyncState());
    }

    public ImmutableDistanceImpl(int id, @NonNull Trip trip, String location, BigDecimal distance, BigDecimal rate, PriceCurrency currency, Date date, TimeZone timeZone, String comment, @NonNull SyncState syncState) {
        mId = id;
        mTrip = Preconditions.checkNotNull(trip);
        mLocation = location;
        mDistance = distance.setScale(ROUNDING_PRECISION, RoundingMode.HALF_UP);
        mRate = rate.setScale(ROUNDING_PRECISION, RoundingMode.HALF_UP);

        final int precision = ModelUtils.getDecimalFormattedValue(distance.multiply(rate), DEFAULT_RATE_DECIMAL_PRECISION).endsWith("0") ? Price.DEFAULT_DECIMAL_PRECISION : DEFAULT_RATE_DECIMAL_PRECISION;
        mPrice = new PriceBuilderFactory().setCurrency(currency).setPrice(distance.multiply(rate)).setDecimalPrecision(precision).build();
        mDate = date;
        mTimezone = timeZone;
        mComment = comment;
        mSyncState = Preconditions.checkNotNull(syncState);
    }

    private ImmutableDistanceImpl(Parcel in) {
        mId = in.readInt();
        mTrip = in.readParcelable(Trip.class.getClassLoader());
        mLocation = in.readString();
        mDistance = (BigDecimal) in.readValue(BigDecimal.class.getClassLoader());
        long tmpDate = in.readLong();
        mDate = tmpDate != -1 ? new Date(tmpDate) : null;
        mTimezone = TimeZone.getTimeZone(in.readString());
        mRate = (BigDecimal) in.readValue(BigDecimal.class.getClassLoader());
        mPrice = in.readParcelable(Price.class.getClassLoader());
        mComment = in.readString();
        mSyncState = in.readParcelable(SyncState.class.getClassLoader());
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
    @NonNull
    public Trip getTrip() {
        return mTrip;
    }

    @Override
    public String getLocation() {
        return mLocation;
    }

    @Override
    public BigDecimal getDistance() {
        return mDistance;
    }

    @Override
    public String getDecimalFormattedDistance() {
        return ModelUtils.getDecimalFormattedValue(mDistance);
    }

    @Override
    public Date getDate() {
        return mDate;
    }

    @Override
    public String getFormattedDate(Context context, String separator) {
        return ModelUtils.getFormattedDate(mDate, mTimezone, context, separator);
    }

    @Override
    public TimeZone getTimeZone() {
        return mTimezone;
    }

    @Override
    public BigDecimal getRate() {
        return mRate;
    }

    @Override
    public String getDecimalFormattedRate() {
        return ModelUtils.getDecimalFormattedValue(mRate, DEFAULT_RATE_DECIMAL_PRECISION);
    }

    @Override
    public String getCurrencyFormattedRate() {
        final int precision = getDecimalFormattedRate().endsWith("0") ? Price.DEFAULT_DECIMAL_PRECISION : DEFAULT_RATE_DECIMAL_PRECISION;
        return ModelUtils.getCurrencyFormattedValue(mRate, mPrice.getCurrency(), precision);
    }

    @NonNull
    @Override
    public Price getPrice() {
        return mPrice;
    }

    @NonNull
    @Override
    public SyncState getSyncState() {
        return mSyncState;
    }

    @Override
    public String getComment() {
        return mComment;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeParcelable(mTrip, flags);
        dest.writeString(mLocation);
        dest.writeValue(mDistance);
        dest.writeLong(mDate != null ? mDate.getTime() : -1L);
        dest.writeString(mTimezone.getID());
        dest.writeValue(mRate);
        dest.writeParcelable(mPrice, flags);
        dest.writeString(mComment);
        dest.writeParcelable(mSyncState, flags);
    }

    public static final Creator<ImmutableDistanceImpl> CREATOR = new Creator<ImmutableDistanceImpl>() {
        @Override
        public ImmutableDistanceImpl createFromParcel(Parcel in) {
            return new ImmutableDistanceImpl(in);
        }

        @Override
        public ImmutableDistanceImpl[] newArray(int size) {
            return new ImmutableDistanceImpl[size];
        }
    };

    @Override
    public String toString() {
        return "Distance [" + "mLocation=" + mLocation + ", mDistance=" + mDistance + ", mDate=" + mDate + ", mTimezone=" + mTimezone + ", mRate=" + mRate + ", mPrice= " + mPrice + ", mComment=" + mComment + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutableDistanceImpl)) return false;

        ImmutableDistanceImpl that = (ImmutableDistanceImpl) o;

        if (mId != that.mId) return false;
        if (!mTrip.equals(that.mTrip)) return false;
        if (mLocation != null ? !mLocation.equals(that.mLocation) : that.mLocation != null)
            return false;
        if (mDistance != null ? !mDistance.equals(that.mDistance) : that.mDistance != null)
            return false;
        if (mDate != null ? !mDate.equals(that.mDate) : that.mDate != null) return false;
        if (mTimezone != null ? !mTimezone.equals(that.mTimezone) : that.mTimezone != null)
            return false;
        if (mRate != null ? !mRate.equals(that.mRate) : that.mRate != null) return false;
        if (mPrice != null ? !mPrice.equals(that.mPrice) : that.mPrice != null) return false;
        if (mComment != null ? !mComment.equals(that.mComment) : that.mComment != null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public int hashCode() {
        int result = mId;
        result = 31 * result + mTrip.hashCode();
        result = 31 * result + (mLocation != null ? mLocation.hashCode() : 0);
        result = 31 * result + (mDistance != null ? mDistance.hashCode() : 0);
        result = 31 * result + (mDate != null ? mDate.hashCode() : 0);
        result = 31 * result + (mTimezone != null ? mTimezone.hashCode() : 0);
        result = 31 * result + (mRate != null ? mRate.hashCode() : 0);
        result = 31 * result + (mPrice != null ? mPrice.hashCode() : 0);
        result = 31 * result + (mComment != null ? mComment.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(@NonNull Distance distance) {
        if (distance.getDate() != null) {
            return distance.getDate().compareTo(mDate);
        } else {
            if (mDate != null) {
                return mDate.compareTo(null);
            } else {
                return 0;
            }
        }
    }
}
