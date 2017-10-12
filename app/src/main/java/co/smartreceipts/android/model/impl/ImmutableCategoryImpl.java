package co.smartreceipts.android.model.impl;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;

public class ImmutableCategoryImpl implements Category {

    private final int id;
    private final String name;
    private final String code;
    private final SyncState syncState;
    private final int customOrderId;

    public ImmutableCategoryImpl(int id, @NonNull String name, @NonNull String code) {
        this(id, name, code, new DefaultSyncState(), id);
    }

    public ImmutableCategoryImpl(int id, @NonNull String name, @NonNull String code,
                                 @NonNull SyncState syncState, int customOrderId) {
        this.id = id;
        this.name = Preconditions.checkNotNull(name);
        this.code = Preconditions.checkNotNull(code);
        this.syncState = Preconditions.checkNotNull(syncState);
        this.customOrderId = customOrderId;
    }

    private ImmutableCategoryImpl(final Parcel in) {
        id = in.readInt();
        name = in.readString();
        code = in.readString();
        syncState = in.readParcelable(getClass().getClassLoader());
        customOrderId = in.readInt();
    }

    @Override
    public int getId() {
        return id;
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public String getCode() {
        return code;
    }

    @NonNull
    @Override
    public SyncState getSyncState() {
        return syncState;
    }

    @Override
    public int getCustomOrderId() {
        return customOrderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImmutableCategoryImpl that = (ImmutableCategoryImpl) o;

        if (id != that.id) return false;
        if (customOrderId != that.customOrderId) return false;
        if (!name.equals(that.name)) return false;
        return code.equals(that.code);

    }

        @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        result = 31 * result + code.hashCode();
        result = 31 * result + customOrderId;

        return result;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {
        out.writeInt(id);
        out.writeString(name);
        out.writeString(code);
        out.writeParcelable(syncState, flags);
        out.writeInt(customOrderId);
    }

    public static Creator<ImmutableCategoryImpl> CREATOR = new Creator<ImmutableCategoryImpl>() {

        @Override
        public ImmutableCategoryImpl createFromParcel(Parcel source) {
            return new ImmutableCategoryImpl(source);
        }

        @Override
        public ImmutableCategoryImpl[] newArray(int size) {
            return new ImmutableCategoryImpl[size];
        }

    };

}
