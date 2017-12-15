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

    private final int id;
    private final String method;
    private final SyncState syncState;
    private final long customOrderId;

    public ImmutablePaymentMethodImpl(int id, @NonNull String method) {
        this(id, method, new DefaultSyncState(), 0);
    }

    public ImmutablePaymentMethodImpl(int id, @NonNull String method, @NonNull SyncState syncState, long customOrderId) {
        this.id = id;
        this.method = Preconditions.checkNotNull(method);
        this.syncState = Preconditions.checkNotNull(syncState);
        this.customOrderId = customOrderId;
    }

    private ImmutablePaymentMethodImpl(final Parcel in) {
        id = in.readInt();
        method = in.readString();
        syncState = in.readParcelable(getClass().getClassLoader());
        customOrderId = in.readLong();
    }

    /**
     * @return - the database primary key id for this method
     */
    @Override
    public int getId() {
        return id;
    }

    @Override
    @NonNull
    public String getMethod() {
        return method;
    }

    @NonNull
    @Override
    public SyncState getSyncState() {
        return syncState;
    }

    @Override
    public long getCustomOrderId() {
        return customOrderId;
    }

    @Override
    public String toString() {
        return method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutablePaymentMethodImpl)) return false;

        ImmutablePaymentMethodImpl that = (ImmutablePaymentMethodImpl) o;

        if (id != that.id) return false;
        if (customOrderId != that.customOrderId) return false;
        return (method.equals(that.method));
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + method.hashCode();
        result = 31 * result + (int) (customOrderId ^ (customOrderId >>> 32));
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {
        out.writeInt(id);
        out.writeString(method);
        out.writeParcelable(syncState, flags);
        out.writeLong(customOrderId);
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

    @Override
    public int compareTo(@NonNull PaymentMethod paymentMethod) {
        return Long.compare(customOrderId, paymentMethod.getCustomOrderId());
    }
}
