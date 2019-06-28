package co.smartreceipts.android.identity.widget.login.model;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

public class UiInputValidationIndicator {

    private final String message;
    private final boolean isEmailValid;
    private final boolean isPasswordValid;

    public UiInputValidationIndicator(@NonNull String message, boolean isEmailValid, boolean isPasswordValid) {
        this.message = Preconditions.checkNotNull(message);
        this.isEmailValid = isEmailValid;
        this.isPasswordValid = isPasswordValid;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    public boolean isEmailValid() {
        return isEmailValid;
    }

    public boolean isPasswordValid() {
        return isPasswordValid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UiInputValidationIndicator)) return false;

        UiInputValidationIndicator indicator = (UiInputValidationIndicator) o;

        if (isEmailValid != indicator.isEmailValid) return false;
        if (isPasswordValid != indicator.isPasswordValid) return false;
        return message.equals(indicator.message);

    }

    @Override
    public int hashCode() {
        int result = message.hashCode();
        result = 31 * result + (isEmailValid ? 1 : 0);
        result = 31 * result + (isPasswordValid ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UiInputValidationIndicator{" +
                "message='" + message + '\'' +
                ", isEmailValid=" + isEmailValid +
                ", isPasswordValid=" + isPasswordValid +
                '}';
    }
}
