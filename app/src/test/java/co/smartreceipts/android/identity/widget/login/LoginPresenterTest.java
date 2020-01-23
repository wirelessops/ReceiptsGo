package co.smartreceipts.android.identity.widget.login;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.R;
import co.smartreceipts.core.identity.apis.login.SmartReceiptsUserLogin;
import co.smartreceipts.core.identity.apis.login.SmartReceiptsUserSignUp;
import co.smartreceipts.core.identity.apis.login.UserCredentialsPayload;
import co.smartreceipts.android.identity.widget.login.model.UiInputValidationIndicator;
import co.smartreceipts.android.widget.model.UiIndicator;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import kotlin.Unit;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class LoginPresenterTest {

    private static final String EMAIL = "email@email.com";
    private static final String PASSWORD = "password";

    // Class under test
    LoginPresenter presenter;

    Context context = ApplicationProvider.getApplicationContext();

    @Mock
    LoginView view;

    @Mock
    LoginInteractor interactor;

    @Mock
    UiIndicator uiIndicator;

    @Mock
    UserCredentialsPayload userCredentialsPayload;

    @Captor
    ArgumentCaptor<UserCredentialsPayload> userCredentialsPayloadCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(view.getLoginButtonClicks()).thenReturn(Observable.never());
        when(view.getSignUpButtonClicks()).thenReturn(Observable.never());
        when(view.getEmailTextChanges()).thenReturn(Observable.just(EMAIL));
        when(view.getPasswordTextChanges()).thenReturn(Observable.just(PASSWORD));
        when(interactor.loginOrSignUp(any(UserCredentialsPayload.class))).thenReturn(Observable.just(uiIndicator));
        when(interactor.getLastUserCredentialsPayload()).thenReturn(Maybe.empty());
        presenter = new LoginPresenter(context, view, interactor);
    }

    @Test
    public void onResumeRestoresCachedPayload() {
        when(interactor.getLastUserCredentialsPayload()).thenReturn(Maybe.just(userCredentialsPayload));
        presenter.subscribe();

        verify(view).present(UiIndicator.idle());
        verify(view).present(uiIndicator);
        verify(interactor, never()).onLoginResultsConsumed();
    }

    @Test
    public void onResumeRestoresCachedPayloadAndConsumesResultsOnSuccess() {
        when(uiIndicator.getState()).thenReturn(UiIndicator.State.Success);
        when(interactor.getLastUserCredentialsPayload()).thenReturn(Maybe.just(userCredentialsPayload));
        presenter.subscribe();

        verify(view).present(UiIndicator.idle());
        verify(view).present(uiIndicator);
        verify(interactor).onLoginResultsConsumed();
    }

    @Test
    public void onResumeRestoresCachedPayloadAndConsumesResultsOnError() {
        when(uiIndicator.getState()).thenReturn(UiIndicator.State.Error);
        when(interactor.getLastUserCredentialsPayload()).thenReturn(Maybe.just(userCredentialsPayload));
        presenter.subscribe();

        verify(view).present(UiIndicator.idle());
        verify(view).present(uiIndicator);
        verify(interactor).onLoginResultsConsumed();
    }

    @Test
    public void loginClickStartsLogin() {
        when(view.getLoginButtonClicks()).thenReturn(Observable.just(Unit.INSTANCE));
        presenter.subscribe();

        verify(interactor).loginOrSignUp(userCredentialsPayloadCaptor.capture());
        verify(view).present(UiIndicator.idle());
        verify(view).present(uiIndicator);
        verify(interactor, never()).onLoginResultsConsumed();
        assertEquals(userCredentialsPayloadCaptor.getValue(), new SmartReceiptsUserLogin(EMAIL, PASSWORD));
    }

    @Test
    public void signUpClickStartsSignUp() {
        when(view.getSignUpButtonClicks()).thenReturn(Observable.just(Unit.INSTANCE));
        presenter.subscribe();

        verify(interactor).loginOrSignUp(userCredentialsPayloadCaptor.capture());
        verify(view).present(UiIndicator.idle());
        verify(view).present(uiIndicator);
        verify(interactor, never()).onLoginResultsConsumed();
        assertEquals(userCredentialsPayloadCaptor.getValue(), new SmartReceiptsUserSignUp(EMAIL, PASSWORD));
    }

    @Test
    public void shortPasswordsAreInvalid() {
        when(view.getPasswordTextChanges()).thenReturn(Observable.just("*"));
        presenter.subscribe();
        verify(view).present(new UiInputValidationIndicator(context.getString(R.string.login_fields_hint_password), true, false));
    }

    @Test
    public void emailsWithoutAtSymbolAreInvalid() {
        when(view.getEmailTextChanges()).thenReturn(Observable.just(".email."));
        presenter.subscribe();
        verify(view).present(new UiInputValidationIndicator(context.getString(R.string.login_fields_hint_email), false, true));
    }

    @Test
    public void emailsWithoutDotSymbolAreInvalid() {
        when(view.getEmailTextChanges()).thenReturn(Observable.just("email@@@email"));
        presenter.subscribe();
        verify(view).present(new UiInputValidationIndicator(context.getString(R.string.login_fields_hint_email), false, true));
    }

    @Test
    public void validCredentialsAreHintingForLogin() {
        presenter.subscribe();
        verify(view).present(new UiInputValidationIndicator(context.getString(R.string.login_fields_hint_valid), true, true));
    }

}