package com.wops.receiptsgo.identity.widget.login;

import androidx.annotation.NonNull;

import com.wops.receiptsgo.identity.widget.login.model.UiInputValidationIndicator;
import com.wops.receiptsgo.widget.model.UiIndicator;
import io.reactivex.Observable;
import kotlin.Unit;

public interface LoginView {

    void present(@NonNull UiIndicator<String> uiIndicator);

    void present(@NonNull UiInputValidationIndicator uiInputValidationIndicator);

    @NonNull
    Observable<CharSequence> getEmailTextChanges();

    @NonNull
    Observable<CharSequence> getPasswordTextChanges();

    @NonNull
    Observable<Unit> getLoginButtonClicks();

    @NonNull
    Observable<Unit> getSignUpButtonClicks();
}
