package co.smartreceipts.android.model.impl;

import android.content.res.Resources;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;

/**
 * An immutable implementation of {@link co.smartreceipts.android.model.PaymentMethod}.
 *
 * @author Will Baumann
 */
public final class ImmutablePaymentMethodImpl implements PaymentMethod {
    
    public static final PaymentMethod NONE = new PaymentMethodBuilderFactory().setMethod(Resources.getSystem().getString(android.R.string.untitled)).build();

    private final int mId;
    private final String mMethod;
    private final SyncState mSyncState;

    public ImmutablePaymentMethodImpl(int id, @NonNull String method) {
        this(id, method, new DefaultSyncState());
    }

    public ImmutablePaymentMethodImpl(int id, @NonNull String method, @NonNull SyncState syncState) {
        mId = id;
        mMethod = Preconditions.checkNotNull(method);
        mSyncState = Preconditions.checkNotNull(syncState);
    }

    private ImmutablePaymentMethodImpl(final Parcel in) {
        mId = in.readInt();
        mMethod = in.readString();
        mSyncState = in.readParcelable(getClass().getClassLoader());
    }

    /**
     * @return - the database primary key id for this method
     */
    @Override
    public int getId() {
        return mId;
    }

    @Override
    @NonNull
    public String getMethod() {
        return mMethod;
    }

    @NonNull
    @Override
    public SyncState getSyncState() {
        return mSyncState;
    }

    @Override
    public String toString() {
        return mMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutablePaymentMethodImpl)) return false;

        ImmutablePaymentMethodImpl that = (ImmutablePaymentMethodImpl) o;

        if (mId != that.mId) return false;
        return (mMethod.equals(that.mMethod));
    }

    @Override
    public int hashCode() {
        int result = mId;
        result = 31 * result + mMethod.hashCode();
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {
        out.writeInt(mId);
        out.writeString(mMethod);
        out.writeParcelable(mSyncState, flags);
    }

    public static Creator<ImmutablePaymentMethodImpl> CREATOR = new Creator<ImmutablePaymentMethodImpl>() {

        @Override
        public ImmutablePaymentMethodImpl createFromParcel(Parcel source) {
            return new ImmutablePaymentMethodImpl(source);
        }

        @Override
        public ImmutablePaymentMethodImpl[] newArray(int size) {
            return new ImmutablePaymentMethodImpl[size];
        }

    };

}
