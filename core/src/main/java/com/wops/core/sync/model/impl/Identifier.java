package com.wops.core.sync.model.impl;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * An implementation of the interface for unique ids for sync data.
 */
public class Identifier implements Parcelable, Serializable, CharSequence {

    @SerializedName("id")
    private final String mId;

    public Identifier(@NonNull String id) {
        mId = Preconditions.checkNotNull(id);
    }

    private Identifier(Parcel in) {
        mId = in.readString();
    }

    /**
     * @return - the {@link String} representation of this id
     */
    @NonNull
    public String getId() {
        return mId;
    }

    @Override
    public int length() {
        return mId.length();
    }

    @Override
    public char charAt(int i) {
        return mId.charAt(i);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return mId.subSequence(start, end);
    }

    @Override
    @NonNull
    public String toString() {
        return mId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mId);
    }

    public static final Creator<Identifier> CREATOR = new Creator<Identifier>() {
        public Identifier createFromParcel(Parcel source) {
            return new Identifier(source);
        }

        public Identifier[] newArray(int size) {
            return new Identifier[size];
        }
    };

    @Override
    public int hashCode() {
        return mId.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifier identifier = (Identifier) o;

        return mId.equals(identifier.mId);
    }

}
