package co.smartreceipts.android.identity;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.common.base.Preconditions;

import org.reactivestreams.Subscriber;

import javax.inject.Inject;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.events.ErrorEvent;
import co.smartreceipts.analytics.events.Events;
import co.smartreceipts.android.apis.ApiValidationException;
import co.smartreceipts.android.apis.WebServiceManager;
import co.smartreceipts.core.identity.apis.login.LoginPayload;
import co.smartreceipts.core.identity.apis.login.LoginResponse;
import co.smartreceipts.android.identity.apis.login.LoginService;
import co.smartreceipts.core.identity.apis.login.LoginType;
import co.smartreceipts.core.identity.apis.login.UserCredentialsPayload;
import co.smartreceipts.android.identity.apis.me.MeService;
import co.smartreceipts.android.identity.apis.signup.SignUpPayload;
import co.smartreceipts.android.identity.apis.signup.SignUpService;
import co.smartreceipts.core.identity.store.MutableIdentityStore;
import co.smartreceipts.core.identity.apis.push.UpdatePushTokensRequest;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import co.smartreceipts.core.identity.IdentityManager;
import co.smartreceipts.core.identity.apis.me.MeResponse;
import co.smartreceipts.core.identity.store.EmailAddress;
import co.smartreceipts.core.identity.store.Token;
import co.smartreceipts.core.identity.store.UserId;
import co.smartreceipts.analytics.log.Logger;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;


@ApplicationScope
public class IdentityManagerImpl implements IdentityManager {

    private final WebServiceManager webServiceManager;
    private final Analytics analytics;
    private final MutableIdentityStore mutableIdentityStore;
    private final BehaviorSubject<Boolean> isLoggedInBehaviorSubject;
    private final Scheduler initializationScheduler;

    @Inject
    public IdentityManagerImpl(@NonNull Analytics analytics,
                               @NonNull MutableIdentityStore mutableIdentityStore,
                               @NonNull WebServiceManager webServiceManager) {
        this(analytics, mutableIdentityStore, webServiceManager, Schedulers.io());

    }

    public IdentityManagerImpl(@NonNull Analytics analytics,
                               @NonNull MutableIdentityStore mutableIdentityStore,
                               @NonNull WebServiceManager webServiceManager,
                               @NonNull Scheduler initializationScheduler) {
        this.webServiceManager = webServiceManager;
        this.analytics = analytics;
        this.mutableIdentityStore = mutableIdentityStore;
        this.initializationScheduler = initializationScheduler;
        this.isLoggedInBehaviorSubject = BehaviorSubject.create();
    }

    @Override
    @SuppressLint("CheckResult")
    public void initialize() {
        Observable.fromCallable(mutableIdentityStore::isLoggedIn)
                .subscribeOn(initializationScheduler)
                .subscribe(isLoggedInBehaviorSubject::onNext);
    }

    @Nullable
    @Override
    public EmailAddress getEmail() {
        return mutableIdentityStore.getEmail();
    }

    @Nullable
    @Override
    public UserId getUserId() {
        return mutableIdentityStore.getUserId();
    }

    @Nullable
    @Override
    public Token getToken() {
        return mutableIdentityStore.getToken();
    }

    @Override
    @WorkerThread
    public boolean isLoggedIn() {
        return mutableIdentityStore.isLoggedIn();
    }

    /**
     * @return an {@link Observable} relay that will only emit {@link Subscriber#onNext(Object)} calls
     * (and never {@link Subscriber#onComplete()} or {@link Subscriber#onError(Throwable)} calls) under
     * the following circumstances:
     * <ul>
     * <li>When the app launches, it will emit {@code true} if logged in and {@code false} if not</li>
     * <li>When the user signs in, it will emit  {@code true}</li>
     * <li>When the user signs out, it will emit  {@code false}</li>
     * </ul>
     * <p>
     * Users of this class should expect a {@link BehaviorSubject} type behavior in which the current
     * state will always be emitted as soon as we subscribe
     * </p>
     */
    @Override
    @NonNull
    public Observable<Boolean> isLoggedInStream() {
        return isLoggedInBehaviorSubject;
    }

    public synchronized Observable<LoginResponse> logInOrSignUp(@NonNull final UserCredentialsPayload credentials) {
        Preconditions.checkNotNull(credentials.getEmail(), "A valid email must be provided to log-in");

        final Observable<LoginResponse> loginResponseObservable;
        if (credentials.getLoginType() == LoginType.LogIn) {
            Logger.info(this, "Initiating user log in");
            this.analytics.record(Events.Identity.UserLogin);
            loginResponseObservable = webServiceManager.getService(LoginService.class).logIn(new LoginPayload(credentials));
        } else if (credentials.getLoginType() == LoginType.SignUp) {
            Logger.info(this, "Initiating user sign up");
            this.analytics.record(Events.Identity.UserSignUp);
            loginResponseObservable = webServiceManager.getService(SignUpService.class).signUp(new SignUpPayload(credentials));
        } else {
            throw new IllegalArgumentException("Unsupported log in type");
        }

        return loginResponseObservable
                .flatMap(loginResponse -> {
                        // Note - we should eventually validate and confirm loginResponse.getId() != null when this is pushed to prod
                        if (loginResponse.getToken() != null) {
                            mutableIdentityStore.setCredentials(credentials.getEmail(), loginResponse.getId(), loginResponse.getToken());
                            return Observable.just(loginResponse);
                        } else {
                            return Observable.error(new ApiValidationException("The response did not contain a valid API token"));
                        }
                })
                .doOnError(throwable -> {
                        if (credentials.getLoginType() == LoginType.LogIn) {
                            Logger.error(this, "Failed to complete the log in request", throwable);
                            analytics.record(Events.Identity.UserLoginFailure);
                        } else if (credentials.getLoginType() == LoginType.SignUp) {
                            Logger.error(this, "Failed to complete the sign up request", throwable);
                            analytics.record(Events.Identity.UserSignUpFailure);
                        }
                    analytics.record(new ErrorEvent(IdentityManagerImpl.this, throwable));
                })
                .doOnComplete(() -> {
                        isLoggedInBehaviorSubject.onNext(true);
                        if (credentials.getLoginType() == LoginType.LogIn) {
                            Logger.info(this, "Successfully completed the log in request");
                            analytics.record(Events.Identity.UserLoginSuccess);
                        } else if (credentials.getLoginType() == LoginType.SignUp) {
                            Logger.info(this, "Successfully completed the sign up request");
                            analytics.record(Events.Identity.UserSignUpSuccess);
                        }
                });
    }

    @Override
    public void logOut() {
        mutableIdentityStore.logOut();
        isLoggedInBehaviorSubject.onNext(false);
        analytics.record(Events.Identity.UserLogout);
        Logger.info(this, "User logged out");
    }

    @Override
    @NonNull
    public Observable<MeResponse> getMe() {
        if (isLoggedIn()) {
            return webServiceManager.getService(MeService.class).me();
        } else {
            return Observable.error(new IllegalStateException("Cannot fetch the user's account until we're logged in"));
        }
    }

    @Override
    @NonNull
    public Observable<MeResponse> updateMe(@NonNull UpdatePushTokensRequest request) {
        if (isLoggedIn()) {
            return webServiceManager.getService(MeService.class).me(request);
        } else {
            return Observable.error(new IllegalStateException("Cannot fetch the user's account until we're logged in"));
        }
    }
}
