package co.smartreceipts.android.model.impl;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.common.base.Preconditions;

import java.io.File;
import java.sql.Date;
import java.util.TimeZone;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;
import wb.android.storage.StorageManager;

/**
 * A mostly immutable implementation of the {@link co.smartreceipts.android.model.Receipt} interface that
 * serves as the default implementation.
 */
public final class DefaultReceiptImpl implements Receipt {

    private final int mId;
    private final Trip mTrip;
    private final PaymentMethod mPaymentMethod;
    private final int mIndex; // Tracks the index in the list (if specified)
    private final String mName;
    private final String mComment;
    private final Category mCategory;
    private final Price mPrice, mTax;
    private final Date mDate;
    private final TimeZone mTimeZone;
    private final boolean mIsReimbursable;
    private final boolean mIsFullPage;
    private final Source mSource;
    private final String mExtraEditText1;
    private final String mExtraEditText2;
    private final String mExtraEditText3;
    private final SyncState mSyncState;
    private boolean mIsSelected;
    private File mFile;
    private long mFileLastModifiedTime;
    private final long customOrderId;

    public DefaultReceiptImpl(int id, int index, @NonNull Trip trip, @Nullable File file, @NonNull PaymentMethod paymentMethod, @NonNull String name,
                              @NonNull Category category, @NonNull String comment, @NonNull Price price, @NonNull Price tax, @NonNull Date date,
                              @NonNull TimeZone timeZone, boolean isReimbursable, boolean isFullPage, boolean isSelected,
                              @NonNull Source source, @Nullable String extraEditText1, @Nullable String extraEditText2, @Nullable String extraEditText3) {
        this(id, index, trip, file, paymentMethod, name, category, comment, price, tax, date, timeZone, isReimbursable, isFullPage, isSelected, source, extraEditText1, extraEditText2, extraEditText3, new DefaultSyncState(), 0);

    }

    public DefaultReceiptImpl(int id, int index, @NonNull Trip trip, @Nullable File file, @NonNull PaymentMethod paymentMethod, @NonNull String name,
                              @NonNull Category category, @NonNull String comment, @NonNull Price price, @NonNull Price tax, @NonNull Date date,
                              @NonNull TimeZone timeZone, boolean isReimbursable, boolean isFullPage, boolean isSelected,
                              @NonNull Source source, @Nullable String extraEditText1, @Nullable String extraEditText2, @Nullable String extraEditText3,
                              @NonNull SyncState syncState, long customOrderId) {

        mTrip = Preconditions.checkNotNull(trip);
        mName = Preconditions.checkNotNull(name);
        mCategory = Preconditions.checkNotNull(category);
        mComment = Preconditions.checkNotNull(comment);
        mSource = Preconditions.checkNotNull(source);
        mPrice = Preconditions.checkNotNull(price);
        mTax = Preconditions.checkNotNull(tax);
        mDate = Preconditions.checkNotNull(date);
        mTimeZone = Preconditions.checkNotNull(timeZone);
        mSyncState = Preconditions.checkNotNull(syncState);
        mPaymentMethod = Preconditions.checkNotNull(paymentMethod);

        mId = id;
        mIndex = index;
        mFile = file;
        mFileLastModifiedTime = file != null ? file.lastModified() : -1;
        mIsReimbursable = isReimbursable;
        mIsFullPage = isFullPage;
        mExtraEditText1 = extraEditText1;
        mExtraEditText2 = extraEditText2;
        mExtraEditText3 = extraEditText3;
        mIsSelected = isSelected;
        this.customOrderId = customOrderId;
    }

    private DefaultReceiptImpl(@NonNull Parcel in) {
        mTrip = in.readParcelable(Trip.class.getClassLoader());
        mPaymentMethod = in.readParcelable(PaymentMethod.class.getClassLoader());
        mId = in.readInt();
        mName = in.readString();
        mCategory = in.readParcelable(Category.class.getClassLoader());
        mComment = in.readString();
        mPrice = in.readParcelable(Price.class.getClassLoader());
        mTax = in.readParcelable(Price.class.getClassLoader());
        final String fileName = in.readString();
        mFile = TextUtils.isEmpty(fileName) ? null : new File(fileName);
        mFileLastModifiedTime = in.readLong();
        mDate = new Date(in.readLong());
        mIsReimbursable = (in.readByte() != 0);
        mIsFullPage = (in.readByte() != 0);
        mIsSelected = (in.readByte() != 0);
        mExtraEditText1 = in.readString();
        mExtraEditText2 = in.readString();
        mExtraEditText3 = in.readString();
        mIndex = in.readInt();
        mTimeZone = TimeZone.getTimeZone(in.readString());
        mSyncState = in.readParcelable(SyncState.class.getClassLoader());
        mSource = Source.Parcel;
        customOrderId = in.readLong();
    }

    @Override
    public int getId() {
        return mId;
    }

    @NonNull
    @Override
    public Trip getTrip() {
        return mTrip;
    }

    @NonNull
    @Override
    public PaymentMethod getPaymentMethod() {
        return mPaymentMethod;
    }

    @NonNull
    @Override
    public String getName() {
        return mName;
    }

    @Override
    public boolean hasFile() {
        return (mFile != null && mFile.exists());
    }

    @Override
    public boolean hasImage() {
        if (mFile != null && mFile.exists()) {
            final String extension = StorageManager.getExtension(mFile);
            if (extension != null
                    && (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg") || extension
                    .equalsIgnoreCase("png"))) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean hasPDF() {
        if (mFile != null && mFile.exists()) {
            final String extension = StorageManager.getExtension(mFile);
            if (extension != null && extension.equalsIgnoreCase("pdf")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @NonNull
    @Override
    public String getMarkerAsString(@NonNull Context context) {
        if (hasImage()) {
            return context.getString(R.string.image);
        } else if (hasPDF()) {
            return context.getString(R.string.pdf);
        } else {
            return "";
        }
    }

    @Nullable
    @Override
    public File getImage() {
        return mFile;
    }

    @Nullable
    @Override
    public File getPDF() {
        return mFile;
    }

    @Nullable
    @Override
    public File getFile() {
        return mFile;
    }

    @NonNull
    @Override
    public String getFilePath() {
        if (hasFile()) {
            return mFile.getAbsolutePath();
        } else {
            return "";
        }
    }

    @NonNull
    @Override
    public String getFileName() {
        if (hasFile()) {
            return mFile.getName();
        } else {
            return "";
        }
    }

    @Override
    public long getFileLastModifiedTime() {
        return mFileLastModifiedTime;
    }

    @NonNull
    @Override
    public Source getSource() {
        return mSource;
    }

    @NonNull
    @Override
    public Category getCategory() {
        return mCategory;
    }

    @NonNull
    @Override
    public String getComment() {
        return mComment;
    }

    @NonNull
    @Override
    public Price getPrice() {
        return mPrice;
    }

    @NonNull
    @Override
    public Price getTax() {
        return mTax;
    }

    @NonNull
    @Override
    public Date getDate() {
        return mDate;
    }

    @NonNull
    @Override
    public String getFormattedDate(@NonNull Context context, @NonNull String separator) {
        return ModelUtils.getFormattedDate(mDate, (mTimeZone != null) ? mTimeZone : TimeZone.getDefault(), context, separator);
    }

    @NonNull
    @Override
    public TimeZone getTimeZone() {
        return (mTimeZone != null) ? mTimeZone : TimeZone.getDefault();
    }

    @Override
    public boolean isReimbursable() {
        return mIsReimbursable;
    }

    @Override
    public boolean isFullPage() {
        return mIsFullPage;
    }

    @Override
    public boolean isSelected() {
        return mIsSelected;
    }

    @Override
    public int getIndex() {
        return mIndex;
    }

    @Override
    public long getCustomOrderId() {
        return customOrderId;
    }

    @Nullable
    @Override
    public String getExtraEditText1() {
        if (DatabaseHelper.NO_DATA.equals(mExtraEditText1)) {
            return null;
        } else {
            return mExtraEditText1;
        }
    }

    @Nullable
    @Override
    public String getExtraEditText2() {
        if (DatabaseHelper.NO_DATA.equals(mExtraEditText2)) {
            return null;
        } else {
            return mExtraEditText2;
        }
    }

    @Nullable
    @Override
    public String getExtraEditText3() {
        if (DatabaseHelper.NO_DATA.equals(mExtraEditText3)) {
            return null;
        } else {
            return mExtraEditText3;
        }
    }

    @Override
    public boolean hasExtraEditText1() {
        return (mExtraEditText1 != null) && !mExtraEditText1.equals(DatabaseHelper.NO_DATA);
    }

    @Override
    public boolean hasExtraEditText2() {
        return (mExtraEditText2 != null) && !mExtraEditText2.equals(DatabaseHelper.NO_DATA);
    }

    @Override
    public boolean hasExtraEditText3() {
        return (mExtraEditText3 != null) && !mExtraEditText3.equals(DatabaseHelper.NO_DATA);
    }

    @NonNull
    @Override
    public SyncState getSyncState() {
        return mSyncState;
    }

    @Override
    public String toString() {
        return "DefaultReceiptImpl{" +
                "mId=" + mId +
                ", mName='" + mName + '\'' +
                ", trip=" + mTrip.getName() +
                ", mPaymentMethod=" + mPaymentMethod +
                ", mIndex=" + mIndex +
                ", mComment='" + mComment + '\'' +
                ", mCategory=" + mCategory +
                ", mPrice=" + mPrice.getCurrencyFormattedPrice() +
                ", mTax=" + mTax +
                ", mDate=" + mDate +
                ", mTimeZone=" + mTimeZone +
                ", mIsReimbursable=" + mIsReimbursable +
                ", mIsFullPage=" + mIsFullPage +
                ", mSource=" + mSource +
                ", mExtraEditText1='" + mExtraEditText1 + '\'' +
                ", mExtraEditText2='" + mExtraEditText2 + '\'' +
                ", mExtraEditText3='" + mExtraEditText3 + '\'' +
                ", mIsSelected=" + mIsSelected +
                ", mFile=" + mFile +
                ", mFileLastModifiedTime=" + mFileLastModifiedTime +
                ", customOrderId=" + customOrderId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultReceiptImpl)) return false;

        DefaultReceiptImpl that = (DefaultReceiptImpl) o;

        if (mId != that.mId) return false;
        if (mIsReimbursable != that.mIsReimbursable) return false;
        if (mIsFullPage != that.mIsFullPage) return false;
        if (!mTrip.equals(that.mTrip)) return false;
        if (!mPaymentMethod.equals(that.mPaymentMethod)) return false;
        if (mIndex != that.mIndex) return false;
        if (!mName.equals(that.mName)) return false;
        if (!mComment.equals(that.mComment)) return false;
        if (!mCategory.equals(that.mCategory)) return false;
        if (!mPrice.equals(that.mPrice)) return false;
        if (!mTax.equals(that.mTax)) return false;
        if (!mDate.equals(that.mDate)) return false;
        if (!mTimeZone.equals(that.mTimeZone)) return false;
        if (mExtraEditText1 != null ? !mExtraEditText1.equals(that.mExtraEditText1) : that.mExtraEditText1 != null)
            return false;
        if (mExtraEditText2 != null ? !mExtraEditText2.equals(that.mExtraEditText2) : that.mExtraEditText2 != null)
            return false;
        if (mExtraEditText3 != null ? !mExtraEditText3.equals(that.mExtraEditText3) : that.mExtraEditText3 != null)
            return false;
        if (mFileLastModifiedTime != that.mFileLastModifiedTime) return false;
        if (customOrderId != that.customOrderId) return false;
        return mFile != null ? mFile.equals(that.mFile) : that.mFile == null;

    }

    @Override
    public int hashCode() {
        int result = mId;
        result = 31 * result + mTrip.hashCode();
        result = 31 * result + mPaymentMethod.hashCode();
        result = 31 * result + mIndex;
        result = 31 * result + mName.hashCode();
        result = 31 * result + mComment.hashCode();
        result = 31 * result + mCategory.hashCode();
        result = 31 * result + mPrice.hashCode();
        result = 31 * result + mTax.hashCode();
        result = 31 * result + mDate.hashCode();
        result = 31 * result + mTimeZone.hashCode();
        result = 31 * result + (mIsReimbursable ? 1 : 0);
        result = 31 * result + (mIsFullPage ? 1 : 0);
        result = 31 * result + (mExtraEditText1 != null ? mExtraEditText1.hashCode() : 0);
        result = 31 * result + (mExtraEditText2 != null ? mExtraEditText2.hashCode() : 0);
        result = 31 * result + (mExtraEditText3 != null ? mExtraEditText3.hashCode() : 0);
        result = 31 * result + (mFile != null ? mFile.hashCode() : 0);
        result = 31 * result + (int) mFileLastModifiedTime;
        result = 31 * result + (int) (customOrderId ^ (customOrderId >>> 32));
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(getTrip(), flags);
        dest.writeParcelable(getPaymentMethod(), flags);
        dest.writeInt(getId());
        dest.writeString(getName());
        dest.writeParcelable(getCategory(), flags);
        dest.writeString(getComment());
        dest.writeParcelable(getPrice(), flags);
        dest.writeParcelable(getTax(), flags);
        dest.writeString(getFilePath());
        dest.writeLong(getFileLastModifiedTime());
        dest.writeLong(getDate().getTime());
        dest.writeByte((byte) (isReimbursable() ? 1 : 0));
        dest.writeByte((byte) (isFullPage() ? 1 : 0));
        dest.writeByte((byte) (isSelected() ? 1 : 0));
        dest.writeString(getExtraEditText1());
        dest.writeString(getExtraEditText2());
        dest.writeString(getExtraEditText3());
        dest.writeInt(getIndex());
        dest.writeString(mTimeZone.getID());
        dest.writeParcelable(getSyncState(), flags);
        dest.writeLong(getCustomOrderId());
    }

    public static Creator<DefaultReceiptImpl> CREATOR = new Creator<DefaultReceiptImpl>() {

        @Override
        public DefaultReceiptImpl createFromParcel(Parcel source) {
            return new DefaultReceiptImpl(source);
        }

        @Override
        public DefaultReceiptImpl[] newArray(int size) {
            return new DefaultReceiptImpl[size];
        }

    };

    @Override
    public int compareTo(@NonNull Receipt receipt) {
        if (customOrderId == receipt.getCustomOrderId()) {
            return receipt.getDate().compareTo(mDate);
        } else {
            return -Long.compare(customOrderId, receipt.getCustomOrderId());
        }
    }
}