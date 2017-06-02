package co.smartreceipts.android.identity.widget.login;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.identity.apis.login.SmartReceiptsUserLogin;
import co.smartreceipts.android.identity.apis.login.SmartReceiptsUserSignUp;
import co.smartreceipts.android.identity.apis.login.UserCredentialsPayload;
import co.smartreceipts.android.identity.widget.login.model.UiInputValidationIndicator;
import co.smartreceipts.android.widget.model.UiIndicator;
import co.smartreceipts.android.widget.viper.BasePresenter;
import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;

@FragmentScope
public class LoginPresenter extends BasePresenter<LoginView, LoginInteractor> {

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
