package co.smartreceipts.android.widget.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

public class UiIndicator implements Parcelable {

    public enum State {
        Idle, Loading, Error, Success
    }

    private final State state;
    private final Optional<String> message;

    private UiIndicator(@NonNull State state, @Nullable String message) {
        this.state = Preconditions.checkNotNull(state);
        this.message = Optional.ofNullable(message);
    }

    @NonNull
    public static UiIndicator idle() {
        return new UiIndicator(State.Idle, null);
    }

    @NonNull
    public static UiIndicator loading() {
        return new UiIndicator(State.Loading, null);
    }

    @NonNull
    public static UiIndicator error() {
        return new UiIndicator(State.Error, null);
    }

    @NonNull
    public static UiIndicator error(@NonNull String message) {
        return new UiIndicator(State.Error, message);
    }

    @NonNull
    public static UiIndicator success() {
        return new UiIndicator(State.Success, null);
    }

    @NonNull
    public static UiIndicator success(@NonNull String message) {
        return new UiIndicator(State.Success, message);
    }

    @NonNull
    public State getState() {
        return state;
    }

    @NonNull
    public Optional<String> getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UiIndicator)) return false;

        UiIndicator that = (UiIndicator) o;

        if (state != that.state) return false;
        return message.equals(that.message);

    }

    @Override
    public int hashCode() {
        int result = state.hashCode();
        result = 31 * result + message.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "UiIndicator{" +
                "state=" + state +
                ", message='" + message + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.state.ordinal());
        dest.writeString(this.message.orNull());
    }

    public static final Creator<UiIndicator> CREATOR = new Creator<UiIndicator>() {
        @Override
        public UiIndicator createFromParcel(Parcel in) {
            return new UiIndicator(State.values()[in.readInt()], in.readString());
        }

        @Override
        public UiIndicator[] newArray(int size) {
            return new UiIndicator[size];
        }
    };
}
