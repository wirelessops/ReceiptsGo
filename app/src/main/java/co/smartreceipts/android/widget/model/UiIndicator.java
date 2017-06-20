package co.smartreceipts.android.widget.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

public class UiIndicator<T> {

    public enum State {
        Idle, Loading, Error, Success
    }

    private final State state;
    private final Optional<T> data;

    private UiIndicator(@NonNull State state, @Nullable T data) {
        this.state = Preconditions.checkNotNull(state);
        this.data = Optional.ofNullable(data);
    }

    @NonNull
    public static <T> UiIndicator<T> idle() {
        return new UiIndicator<>(State.Idle, null);
    }

    @NonNull
    public static <T> UiIndicator<T> loading() {
        return new UiIndicator<>(State.Loading, null);
    }

    @NonNull
    public static <T> UiIndicator<T> error() {
        return new UiIndicator<>(State.Error, null);
    }

    @NonNull
    public static <T> UiIndicator<T> error(@NonNull T data) {
        return new UiIndicator<>(State.Error, data);
    }

    @NonNull
    public static <T> UiIndicator<T> success() {
        return new UiIndicator<>(State.Success, null);
    }

    @NonNull
    public static <T> UiIndicator<T> success(@NonNull T data) {
        return new UiIndicator<>(State.Success, data);
    }

    @NonNull
    public State getState() {
        return state;
    }

    @NonNull
    public Optional<T> getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UiIndicator)) return false;

        UiIndicator that = (UiIndicator) o;

        if (state != that.state) return false;
        return data.equals(that.data);

    }

    @Override
    public int hashCode() {
        int result = state.hashCode();
        result = 31 * result + data.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "UiIndicator{" +
                "state=" + state +
                ", data='" + data + '\'' +
                '}';
    }

}
