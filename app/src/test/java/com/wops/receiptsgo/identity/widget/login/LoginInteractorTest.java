package com.wops.receiptsgo.identity.widget.login;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.identity.IdentityManagerImpl;
import com.wops.receiptsgo.widget.model.UiIndicator;
import co.smartreceipts.core.identity.apis.login.LoginResponse;
import co.smartreceipts.core.identity.apis.login.SmartReceiptsUserLogin;
import co.smartreceipts.core.identity.apis.login.SmartReceiptsUserSignUp;
import co.smartreceipts.core.identity.apis.login.UserCredentialsPayload;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@RunWith(RobolectricTestRunner.class)
public class LoginInteractorTest {

    // Class under test
    LoginInteractor interactor;

    Context context = ApplicationProvider.getApplicationContext();

    @Mock
    IdentityManagerImpl identityManager;

    SmartReceiptsUserLogin loginPayload = new SmartReceiptsUserLogin("email", "password");

    SmartReceiptsUserSignUp signUpPayload = new SmartReceiptsUserSignUp("email", "password");

    @Mock
    LoginResponse loginResponse;

    @Mock
    ResponseBody responseBody;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        interactor = new LoginInteractor(context, identityManager, Schedulers.trampoline());
    }

    @Test
    public void getLastUserCredentialsPayloadDefaultsAsEmpty() {
        final TestObserver<UserCredentialsPayload> testObserver = interactor.getLastUserCredentialsPayload().test();
        testObserver.assertNoValues();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void loginSuccess() {
        when(identityManager.logInOrSignUp(loginPayload)).thenReturn(Observable.just(loginResponse));
        final TestObserver<UiIndicator<String>> testObserver = interactor.loginOrSignUp(loginPayload).test();
        testObserver.assertValues(UiIndicator.loading(), UiIndicator.success(context.getString(R.string.login_success_toast)));
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void signUpSuccess() {
        when(identityManager.logInOrSignUp(signUpPayload)).thenReturn(Observable.just(loginResponse));
        final TestObserver<UiIndicator<String>> testObserver = interactor.loginOrSignUp(signUpPayload).test();
        testObserver.assertValues(UiIndicator.loading(), UiIndicator.success(context.getString(R.string.sign_up_success_toast)));
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void loginErrorGeneric() {
        when(identityManager.logInOrSignUp(loginPayload)).thenReturn(Observable.error(new Exception()));
        final TestObserver<UiIndicator<String>> testObserver = interactor.loginOrSignUp(loginPayload).test();
        testObserver.assertValues(UiIndicator.loading(), UiIndicator.error(context.getString(R.string.login_failure_toast)));
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void signUpErrorGeneric() {
        when(identityManager.logInOrSignUp(signUpPayload)).thenReturn(Observable.error(new Exception()));
        final TestObserver<UiIndicator<String>> testObserver = interactor.loginOrSignUp(signUpPayload).test();
        testObserver.assertValues(UiIndicator.loading(), UiIndicator.error(context.getString(R.string.sign_up_failure_toast)));
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void loginErrorBadCredentials() {
        Response response = Response.error(401, responseBody);
        when(identityManager.logInOrSignUp(loginPayload)).thenReturn(Observable.error(new HttpException(response)));
        final TestObserver<UiIndicator<String>> testObserver = interactor.loginOrSignUp(loginPayload).test();
        testObserver.assertValues(UiIndicator.loading(), UiIndicator.error(context.getString(R.string.login_failure_credentials_toast)));
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void signUpErrorExistingAccount() {
        Response response = Response.error(420, responseBody);
        when(identityManager.logInOrSignUp(signUpPayload)).thenReturn(Observable.error(new HttpException(response)));
        final TestObserver<UiIndicator<String>> testObserver = interactor.loginOrSignUp(signUpPayload).test();
        testObserver.assertValues(UiIndicator.loading(), UiIndicator.error(context.getString(R.string.sign_up_failure_account_exists_toast)));
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void requestsGetCachedUntilConsumed() {
        when(identityManager.logInOrSignUp(loginPayload)).thenReturn(Observable.just(loginResponse));
        final TestObserver<UiIndicator<String>> preTestObserver = interactor.loginOrSignUp(loginPayload).test();
        preTestObserver.assertValues(UiIndicator.loading(), UiIndicator.success(context.getString(R.string.login_success_toast)));
        preTestObserver.assertComplete();
        preTestObserver.assertNoErrors();

        final TestObserver<UserCredentialsPayload> preMaybeTestObserver = interactor.getLastUserCredentialsPayload().test();
        preMaybeTestObserver.assertValues(loginPayload);
        preMaybeTestObserver.assertComplete();
        preMaybeTestObserver.assertNoErrors();

        final TestObserver<UiIndicator<String>> postTestObserver = interactor.getLastUserCredentialsPayload().flatMapObservable(interactor::loginOrSignUp).test();
        postTestObserver.assertValues(UiIndicator.loading(), UiIndicator.success(context.getString(R.string.login_success_toast)));
        postTestObserver.assertComplete();
        postTestObserver.assertNoErrors();

        // Consume
        interactor.onLoginResultsConsumed();
        final TestObserver<UserCredentialsPayload> postMaybeTestObserver = interactor.getLastUserCredentialsPayload().test();
        postMaybeTestObserver.assertNoValues();
        postMaybeTestObserver.assertComplete();
        postMaybeTestObserver.assertNoErrors();

        // And confirm we only made the actual API call once
        verify(identityManager).logInOrSignUp(loginPayload);
    }

}