package co.smartreceipts.android.model.impl;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.io.File;
import java.sql.Date;
import java.util.Calendar;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;

import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.sync.model.SyncState;

public class DefaultTripImpl implements Trip {

    private final File mReportDirectory;
    private final String mComment;
    private final Date mStartDate;
    private final TimeZone mStartTimeZone;
    private final Date mEndDate;
    private final TimeZone mEndTimeZone;
    private final PriceCurrency mDefaultCurrency;
    private final String mCostCenter;
    private final SyncState mSyncState;

    private Price mPrice;
    private Price mDailySubTotal;


    private final Source mSource;
    private final Filter<Receipt> mFilter = null;


    public DefaultTripImpl(@NonNull File directory, @NonNull Date startDate, @NonNull TimeZone startTimeZone, @NonNull Date endDate, @NonNull TimeZone endTimeZone,
                           @NonNull PriceCurrency defaultCurrency, @NonNull String comment, @NonNull String costCenter, @Nullable Source source, @NonNull SyncState syncState) {
        mReportDirectory = Preconditions.checkNotNull(directory);
        mStartDate = Preconditions.checkNotNull(startDate);
        mStartTimeZone = Preconditions.checkNotNull(startTimeZone);
        mEndDate = Preconditions.checkNotNull(endDate);
        mEndTimeZone = Preconditions.checkNotNull(endTimeZone);
        mDefaultCurrency = Preconditions.checkNotNull(defaultCurrency);
        mComment = Preconditions.checkNotNull(comment);
        mCostCenter = Preconditions.checkNotNull(costCenter);
        mSource = source != null ? source : Source.Undefined;
        mSyncState = Preconditions.checkNotNull(syncState);

        // Sets a simple default for price and daily of 0
        mPrice = new PriceBuilderFactory().setPrice(0).setCurrency(defaultCurrency).build();
        mDailySubTotal = new PriceBuilderFactory().setPrice(0).setCurrency(defaultCurrency).build();
    }

    private DefaultTripImpl(Parcel in) {
        mReportDirectory = new File(in.readString());
        mPrice = in.readParcelable(Price.class.getClassLoader());
        mStartDate = new Date(in.readLong());
        mEndDate = new Date(in.readLong());
        mStartTimeZone = TimeZone.getTimeZone(in.readString());
        mEndTimeZone = TimeZone.getTimeZone(in.readString());
        mDailySubTotal = in.readParcelable(Price.class.getClassLoader());
        mComment = in.readString();
        mCostCenter = in.readString();
        mDefaultCurrency = PriceCurrency.getInstance(in.readString());
        mSyncState = in.readParcelable(SyncState.class.getClassLoader());
        mSource = Source.Parcel;
    }

    @Override
    @NonNull
    public String getName() {
        return mReportDirectory.getName();
    }

    @Override
    @NonNull
    public File getDirectory() {
        return mReportDirectory;
    }

    @NonNull
    @Override
    public String getDirectoryPath() {
        return mReportDirectory.getAbsolutePath();
    }

    @NonNull
    @Override
    public Date getStartDate() {
        return mStartDate;
    }

    @NonNull
    @Override
    public String getFormattedStartDate(Context context, String separator) {
        return ModelUtils.getFormattedDate(mStartDate, getStartTimeZone(), context, separator);
    }

    @NonNull
    @Override
    public TimeZone getStartTimeZone() {
        return (mStartTimeZone != null) ? mStartTimeZone : TimeZone.getDefault();
    }

    @NonNull
    @Override
    public Date getEndDate() {
        return mEndDate;
    }

    @NonNull
    @Override
    public String getFormattedEndDate(Context context, String separator) {
        return ModelUtils.getFormattedDate(mEndDate, getEndTimeZone(), context, separator);
    }

    @NonNull
    @Override
    public TimeZone getEndTimeZone() {
        return (mEndTimeZone != null) ? mEndTimeZone : TimeZone.getDefault();
    }

    /**
     * Tests if a particular date is included with the bounds of this particular trip When performing the test, it uses
     * the local time zone for the date, and the defined time zones for the start and end date bounds. The start date
     * time is assumed to occur at 00:01 of the start day and the end date is assumed to occur at 23:59 of the end day.
     * The reasoning behind this is to ensure that it appears properly from a UI perspective. Since the initial date
     * only shows the day itself, it may include an arbitrary time that is never shown to the user. Setting the time
     * aspect manually accounts for this. This returns false if the date is null.
     *
     * @param date - the date to test
     * @return true if it is contained within. false otherwise
     */
    @Override
    public boolean isDateInsideTripBounds(@Nullable Date date) {
        if (date == null) {
            return false;
        }

        // Build a calendar for the date we intend to test
        Calendar testCalendar = Calendar.getInstance();
        testCalendar.setTime(date);
        testCalendar.setTimeZone(TimeZone.getDefault());

        // Build a calendar for the start date
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(mStartDate);
        startCalendar.setTimeZone((mStartTimeZone != null) ? mStartTimeZone : TimeZone.getDefault());
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);

        // Build a calendar for the end date
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(mEndDate);
        endCalendar.setTimeZone((mEndTimeZone != null) ? mEndTimeZone : TimeZone.getDefault());
        endCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endCalendar.set(Calendar.MINUTE, 59);
        endCalendar.set(Calendar.SECOND, 59);
        endCalendar.set(Calendar.MILLISECOND, 999);

        if (testCalendar.compareTo(startCalendar) >= 0 && testCalendar.compareTo(endCalendar) <= 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    @NonNull
    public PriceCurrency getTripCurrency() {
        return mDefaultCurrency;
    }

    @Override
    @NonNull
    public String getDefaultCurrencyCode() {
        if (mDefaultCurrency != null) {
            return mDefaultCurrency.getCurrencyCode();
        } else {
            return Currency.getInstance(Locale.getDefault()).getCurrencyCode();
        }
    }

    @NonNull
    @Override
    public String getComment() {
        return mComment;
    }

    @NonNull
    @Override
    public String getCostCenter() {
        return mCostCenter;
    }

    @NonNull
    @Override
    public Source getSource() {
        return mSource;
    }

    @NonNull
    @Override
    public Price getPrice() {
        return mPrice;
    }

    @Override
    public void setPrice(@NonNull Price price) {
        mPrice = price;
    }

    @NonNull
    @Override
    public Price getDailySubTotal() {
        return mDailySubTotal;
    }

    @Override
    public void setDailySubTotal(@NonNull Price dailyTotal) {
        mDailySubTotal = dailyTotal;
    }

    @Nullable
    @Override
    public Filter<Receipt> getFilter() {
        return mFilter;
    }

    @NonNull
    @Override
    public SyncState getSyncState() {
        return mSyncState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultTripImpl)) return false;

        DefaultTripImpl that = (DefaultTripImpl) o;

        if (!mReportDirectory.equals(that.mReportDirectory)) return false;
        if (!mComment.equals(that.mComment)) return false;
        if (!mStartDate.equals(that.mStartDate)) return false;
        if (!mStartTimeZone.equals(that.mStartTimeZone)) return false;
        if (!mEndDate.equals(that.mEndDate)) return false;
        if (!mEndTimeZone.equals(that.mEndTimeZone)) return false;
        if (!mDefaultCurrency.equals(that.mDefaultCurrency)) return false;
        return (mCostCenter.equals(that.mCostCenter));

    }

    @Override
    public int hashCode() {
        int result = mReportDirectory.hashCode();
        result = 31 * result + mComment.hashCode();
        result = 31 * result + mStartDate.hashCode();
        result = 31 * result + mStartTimeZone.hashCode();
        result = 31 * result + mEndDate.hashCode();
        result = 31 * result + mEndTimeZone.hashCode();
        result = 31 * result + mDefaultCurrency.hashCode();
        result = 31 * result + mCostCenter.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DefaultTripImpl{" +
                "mReportDirectory=" + mReportDirectory +
                ", mComment='" + mComment + '\'' +
                ", mCostCenter='" + mCostCenter + '\'' +
                ", mPrice=" + mPrice +
                ", mDailySubTotal=" + mDailySubTotal +
                ", mStartDate=" + mStartDate +
                ", mEndDate=" + mEndDate +
                ", mStartTimeZone=" + mStartTimeZone +
                ", mEndTimeZone=" + mEndTimeZone +
                ", mDefaultCurrency=" + mDefaultCurrency +
                ", mSource=" + mSource +
                ", mFilter=" + mFilter +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getDirectoryPath());
        dest.writeParcelable(getPrice(), flags);
        dest.writeLong(getStartDate().getTime());
        dest.writeLong(getEndDate().getTime());
        dest.writeString(getStartTimeZone().getID());
        dest.writeString(getEndTimeZone().getID());
        dest.writeParcelable(getDailySubTotal(), flags);
        dest.writeString(getComment());
        dest.writeString(getCostCenter());
        dest.writeString(getDefaultCurrencyCode());
        dest.writeParcelable(getSyncState(), flags);
    }

    public static final Creator<DefaultTripImpl> CREATOR = new Creator<DefaultTripImpl>() {

        @Override
        public DefaultTripImpl createFromParcel(Parcel source) {
            return new DefaultTripImpl(source);
        }

        @Override
        public DefaultTripImpl[] newArray(int size) {
            return new DefaultTripImpl[size];
        }

    };

    @Override
    public int compareTo(@NonNull Trip trip) {
        return trip.getEndDate().compareTo(mEndDate);
    }

}
