package co.smartreceipts.android.identity;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.google.common.base.Preconditions;

import org.reactivestreams.Subscriber;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.apis.ApiValidationException;
import co.smartreceipts.android.apis.WebServiceManager;
import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.identity.apis.login.LoginPayload;
import co.smartreceipts.android.identity.apis.login.LoginResponse;
import co.smartreceipts.android.identity.apis.login.LoginService;
import co.smartreceipts.android.identity.apis.login.LoginType;
import co.smartreceipts.android.identity.apis.login.UserCredentialsPayload;
import co.smartreceipts.android.identity.apis.logout.LogoutResponse;
import co.smartreceipts.android.identity.apis.logout.LogoutService;
import co.smartreceipts.android.identity.apis.me.MeResponse;
import co.smartreceipts.android.identity.apis.me.MeService;
import co.smartreceipts.android.identity.apis.organizations.OrganizationsResponseKt;
import co.smartreceipts.android.identity.apis.signup.SignUpPayload;
import co.smartreceipts.android.identity.apis.signup.SignUpService;
import co.smartreceipts.android.identity.store.EmailAddress;
import co.smartreceipts.android.identity.store.IdentityStore;
import co.smartreceipts.android.identity.store.MutableIdentityStore;
import co.smartreceipts.android.identity.store.Token;
import co.smartreceipts.android.identity.store.UserId;
import co.smartreceipts.android.push.apis.me.UpdatePushTokensRequest;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;


@ApplicationScope
public class IdentityManager implements IdentityStore {

    private final WebServiceManager webServiceManager;
    private final Analytics analytics;
    private final MutableIdentityStore mutableIdentityStore;
    private final OrganizationManager organizationManager;
    private final BehaviorSubject<Boolean> isLoggedInBehaviorSubject;
    private final Scheduler initializationScheduler;

    @Inject
    public IdentityManager(@NonNull Analytics analytics,
                           @NonNull UserPreferenceManager userPreferenceManager,
                           @NonNull MutableIdentityStore mutableIdentityStore,
                           @NonNull WebServiceManager webServiceManager,
                           @NonNull ConfigurationManager configurationManager) {
        this(analytics, mutableIdentityStore, webServiceManager, new OrganizationManager(webServiceManager, mutableIdentityStore, userPreferenceManager, configurationManager), Schedulers.io());

    }

    public IdentityManager(@NonNull Analytics analytics,
                           @NonNull MutableIdentityStore mutableIdentityStore,
                           @NonNull WebServiceManager webServiceManager,
                           @NonNull OrganizationManager organizationManager,
                           @NonNull Scheduler initializationScheduler) {
        this.webServiceManager = webServiceManager;
        this.analytics = analytics;
        this.mutableIdentityStore = mutableIdentityStore;
        this.organizationManager = organizationManager;
        this.initializationScheduler = initializationScheduler;
        this.isLoggedInBehaviorSubject = BehaviorSubject.create();
    }

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
                .flatMap(loginResponse -> organizationManager.getOrganizations()
                        .flatMap(response -> Observable.just(loginResponse)))
                .doOnError(throwable -> {
                        if (credentials.getLoginType() == LoginType.LogIn) {
                            Logger.error(this, "Failed to complete the log in request", throwable);
                            analytics.record(Events.Identity.UserLoginFailure);
                        } else if (credentials.getLoginType() == LoginType.SignUp) {
                            Logger.error(this, "Failed to complete the sign up request", throwable);
                            analytics.record(Events.Identity.UserSignUpFailure);
                        }
                    analytics.record(new ErrorEvent(IdentityManager.this, throwable));
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

    public synchronized Observable<LogoutResponse> logOut() {
        Logger.info(this, "Initiating user log-out");
        this.analytics.record(Events.Identity.UserLogout);

        return webServiceManager.getService(LogoutService.class).logOut()
                .doOnNext(logoutResponse -> mutableIdentityStore.setCredentials(null, null, null))
                .doOnError(throwable -> {
                    Logger.error(this, "Failed to complete the log-out request", throwable);
                    analytics.record(Events.Identity.UserLogoutFailure);
                })
                .doOnComplete(() -> {
                    Logger.info(this, "Successfully completed the log-out request");
                    isLoggedInBehaviorSubject.onNext(false);
                    analytics.record(Events.Identity.UserLogoutSuccess);
                });
    }

    @NonNull
    public Observable<MeResponse> getMe() {
        if (isLoggedIn()) {
            return webServiceManager.getService(MeService.class).me();
        } else {
            return Observable.error(new IllegalStateException("Cannot fetch the user's account until we're logged in"));
        }
    }

    @NonNull
    public Observable<MeResponse> updateMe(@NonNull UpdatePushTokensRequest request) {
        if (isLoggedIn()) {
            return webServiceManager.getService(MeService.class).me(request);
        } else {
            return Observable.error(new IllegalStateException("Cannot fetch the user's account until we're logged in"));
        }
    }

    @NonNull
    public Observable<OrganizationsResponseKt> getOrganizations() {
        return organizationManager.getOrganizations();
    }
}
