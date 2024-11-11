package com.wops.receiptsgo.identity.widget.login;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import com.wops.receiptsgo.R;
import co.smartreceipts.core.di.scopes.FragmentScope;
import co.smartreceipts.core.identity.apis.login.SmartReceiptsUserLogin;
import co.smartreceipts.core.identity.apis.login.SmartReceiptsUserSignUp;
import co.smartreceipts.core.identity.apis.login.UserCredentialsPayload;
import com.wops.receiptsgo.identity.widget.login.model.UiInputValidationIndicator;
import com.wops.receiptsgo.widget.model.UiIndicator;
import com.wops.receiptsgo.widget.viper.BaseViperPresenter;
import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;

@FragmentScope
public class LoginPresenter extends BaseViperPresenter<LoginView, LoginInteractor> {

    private static final int MINIMUM_EMAIL_LENGTH = 6;
    private static final int MINIMUM_PASSWORD_LENGTH = 8;

    private final Context context;

    @Inject
    public LoginPresenter(@NonNull Context context, @NonNull LoginView view, @NonNull LoginInteractor interactor) {
        super(view, interactor);
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
    }

    @Override
    public void subscribe() {
        compositeDisposable.add(Observable.concat(
                interactor.getLastUserCredentialsPayload().toObservable(), // Start by emitting our previous request
                Observable.merge( // Next, get the stream of clicks as payloads for the ongoing stream
                        Observable.combineLatest(
                                view.getEmailTextChanges(),
                                view.getPasswordTextChanges(),
                                (BiFunction<CharSequence, CharSequence, UserCredentialsPayload>) SmartReceiptsUserLogin::new)
                                .flatMap(userCredentialsPayload -> view.getLoginButtonClicks().map(ignored -> userCredentialsPayload)),

                        Observable.combineLatest(
                                view.getEmailTextChanges(),
                                view.getPasswordTextChanges(),
                                (BiFunction<CharSequence, CharSequence, UserCredentialsPayload>) SmartReceiptsUserSignUp::new)
                                .flatMap(userCredentialsPayload -> view.getSignUpButtonClicks().map(ignored -> userCredentialsPayload)))
        )
                .flatMap(interactor::loginOrSignUp)
                .startWith(UiIndicator.idle())
                .subscribe(uiIndicator -> {
                    view.present(uiIndicator);
                    if (uiIndicator.getState() == UiIndicator.State.Success || uiIndicator.getState() == UiIndicator.State.Error) {
                        interactor.onLoginResultsConsumed();
                    }
                }));

        compositeDisposable.add(Observable.combineLatest(simpleEmailFieldValidator(), simplePasswordFieldValidator(),
                (isEmailValid, isPasswordValid) -> {
                    final String message;
                    if (!isEmailValid) {
                        message = context.getString(R.string.login_fields_hint_email);
                    } else if (!isPasswordValid) {
                        message = context.getString(R.string.login_fields_hint_password);
                    } else {
                        message = context.getString(R.string.login_fields_hint_valid);
                    }
                    return new UiInputValidationIndicator(message, isEmailValid, isPasswordValid);
                })
                .distinctUntilChanged()
                .subscribe(view::present));
    }

    @NonNull
    private Observable<Boolean> simpleEmailFieldValidator() {
        return view.getEmailTextChanges()
                .map(emailCharSequence -> {
                    if (emailCharSequence != null && emailCharSequence.length() >= MINIMUM_EMAIL_LENGTH) {
                        final String email = emailCharSequence.toString();
                        return email.contains("@") && email.contains(".");
                    } else {
                        return false;
                    }
                });
    }

    @NonNull
    private Observable<Boolean> simplePasswordFieldValidator() {
        return view.getPasswordTextChanges()
                .map(password -> password != null && password.length() >= MINIMUM_PASSWORD_LENGTH);
    }

}
