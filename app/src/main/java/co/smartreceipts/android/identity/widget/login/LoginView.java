package co.smartreceipts.android.identity.widget.login;

import android.support.annotation.NonNull;

import co.smartreceipts.android.identity.widget.login.model.UiInputValidationIndicator;
import co.smartreceipts.android.widget.model.UiIndicator;
import io.reactivex.Observable;

public interface LoginView {

    void present(@NonNull UiIndicator<String> uiIndicator);

    void present(@NonNull UiInputValidationIndicator uiInputValidationIndicator);

    @NonNull
    Observable<CharSequence> getEmailTextChanges();

    @NonNull
    Observable<CharSequence> getPasswordTextChanges();

    @NonNull
    Observable<Object> getLoginButtonClicks();

    @NonNull
    Observable<Object> getSignUpButtonClicks();
}
