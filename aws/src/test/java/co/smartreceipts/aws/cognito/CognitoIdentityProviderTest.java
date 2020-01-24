package co.smartreceipts.aws.cognito;

import com.hadisatrio.optional.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.Date;

import co.smartreceipts.core.identity.IdentityManager;
import co.smartreceipts.core.identity.apis.me.MeResponse;
import co.smartreceipts.core.identity.apis.me.User;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class CognitoIdentityProviderTest {

    private static final String TOKEN = "token";
    private static final String IDENTITY_ID = "identityId";
    private static final Date EXPIRES_AT = new Date(5);

    // Class under test
    CognitoIdentityProvider cognitoIdentityProvider;

    @Mock
    IdentityManager identityManager;

    @Mock
    LocalCognitoTokenStore localCognitoTokenStore;

    @Mock
    MeResponse meResponse;

    @Mock
    User user;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(meResponse.getUser()).thenReturn(user);
        Mockito.when(localCognitoTokenStore.getCognitoToken()).thenReturn(new Cognito(TOKEN, IDENTITY_ID, EXPIRES_AT));

        cognitoIdentityProvider = new CognitoIdentityProvider(identityManager, localCognitoTokenStore);
    }

    @Test
    public void refreshCognitoTokenThrowsException() {
        Mockito.when(identityManager.getMe()).thenReturn(Observable.<MeResponse>error(new IOException()));

        TestObserver<Optional<Cognito>> testObserver = cognitoIdentityProvider.refreshCognitoToken().test();

        Mockito.verify(localCognitoTokenStore).persist(null);

        testObserver.assertNoValues()
                .assertNotComplete()
                .assertError(IOException.class);
    }

    @Test
    public void refreshCognitoTokenReturnsNullUserResponse() {
        Mockito.when(meResponse.getUser()).thenReturn(null);
        Mockito.when(identityManager.getMe()).thenReturn(Observable.just(meResponse));

        TestObserver<Optional<Cognito>> testObserver = cognitoIdentityProvider.refreshCognitoToken().test();

        Mockito.verify(localCognitoTokenStore, Mockito.times(2)).persist(null);

        testObserver.assertValue(Optional.absent())
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void refreshCognitoTokenIsValid() {
        Mockito.when(identityManager.getMe()).thenReturn(Observable.just(meResponse));
        Mockito.when(user.getCognitoToken()).thenReturn(TOKEN);
        Mockito.when(user.getIdentityId()).thenReturn(IDENTITY_ID);
        Mockito.when(user.getCognitoTokenExpiresAt()).thenReturn(EXPIRES_AT);

        final TestObserver<Optional<Cognito>> testObserver = cognitoIdentityProvider.refreshCognitoToken().test();

        final Cognito cognito = new Cognito(TOKEN, IDENTITY_ID, EXPIRES_AT);
        Mockito.verify(localCognitoTokenStore).persist(null);
        Mockito.verify(localCognitoTokenStore).persist(cognito);

        testObserver.assertValue(Optional.of(cognito))
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void prefetchNullToken() {
        final Cognito preCognito = null;
        Mockito.when(localCognitoTokenStore.getCognitoToken()).thenReturn(preCognito);
        Mockito.when(identityManager.getMe()).thenReturn(Observable.just(meResponse));
        Mockito.when(user.getCognitoToken()).thenReturn(TOKEN);
        Mockito.when(user.getIdentityId()).thenReturn(IDENTITY_ID);
        Mockito.when(user.getCognitoTokenExpiresAt()).thenReturn(EXPIRES_AT);

        TestObserver<Optional<Cognito>> testObserver = cognitoIdentityProvider.prefetchCognitoTokenIfNeeded().test();

        final Cognito cognito = new Cognito(TOKEN, IDENTITY_ID, EXPIRES_AT);
        Mockito.verify(localCognitoTokenStore).persist(null);
        Mockito.verify(localCognitoTokenStore).persist(cognito);
        testObserver.assertValue(Optional.of(cognito))
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void prefetchInvalidTokenWillNullToken() {
        final Cognito preCognito = new Cognito(null, IDENTITY_ID, EXPIRES_AT);
        Mockito.when(localCognitoTokenStore.getCognitoToken()).thenReturn(preCognito);
        Mockito.when(identityManager.getMe()).thenReturn(Observable.just(meResponse));
        Mockito.when(user.getCognitoToken()).thenReturn(TOKEN);
        Mockito.when(user.getIdentityId()).thenReturn(IDENTITY_ID);
        Mockito.when(user.getCognitoTokenExpiresAt()).thenReturn(EXPIRES_AT);

        final TestObserver<Optional<Cognito>> testObserver = cognitoIdentityProvider.prefetchCognitoTokenIfNeeded().test();

        final Cognito cognito = new Cognito(TOKEN, IDENTITY_ID, EXPIRES_AT);
        Mockito.verify(localCognitoTokenStore).persist(null);
        Mockito.verify(localCognitoTokenStore).persist(cognito);
        testObserver.assertValue(Optional.of(cognito))
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void prefetchInvalidTokenWillNullIdentityId() {
        final Cognito preCognito = new Cognito(TOKEN, null, EXPIRES_AT);
        Mockito.when(localCognitoTokenStore.getCognitoToken()).thenReturn(preCognito);
        Mockito.when(identityManager.getMe()).thenReturn(Observable.just(meResponse));
        Mockito.when(user.getCognitoToken()).thenReturn(TOKEN);
        Mockito.when(user.getIdentityId()).thenReturn(IDENTITY_ID);
        Mockito.when(user.getCognitoTokenExpiresAt()).thenReturn(EXPIRES_AT);

        final TestObserver<Optional<Cognito>> testObserver = cognitoIdentityProvider.prefetchCognitoTokenIfNeeded().test();

        final Cognito cognito = new Cognito(TOKEN, IDENTITY_ID, EXPIRES_AT);
        Mockito.verify(localCognitoTokenStore).persist(null);
        Mockito.verify(localCognitoTokenStore).persist(cognito);
        testObserver.assertValue(Optional.of(cognito))
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void prefetchValidToken() {
        final Cognito preCognito = new Cognito(TOKEN, IDENTITY_ID, EXPIRES_AT);
        Mockito.when(localCognitoTokenStore.getCognitoToken()).thenReturn(preCognito);

        final TestObserver<Optional<Cognito>> testObserver = cognitoIdentityProvider.prefetchCognitoTokenIfNeeded().test();

        Mockito.verify(localCognitoTokenStore, Mockito.never()).persist(ArgumentMatchers.any(Cognito.class));
        testObserver.assertValue(Optional.of(preCognito))
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void synchronouslyRefreshCognitoTokenThrowsException() {
        Mockito.when(identityManager.getMe()).thenReturn(Observable.<MeResponse>error(new IOException()));

        assertEquals(null, cognitoIdentityProvider.synchronouslyRefreshCognitoToken());
    }

    @Test
    public void synchronouslyRefreshCognitoTokenReturnsNullUserResponse() {
        Mockito.when(meResponse.getUser()).thenReturn(null);
        Mockito.when(identityManager.getMe()).thenReturn(Observable.just(meResponse));

        assertEquals(null, cognitoIdentityProvider.synchronouslyRefreshCognitoToken());
    }

    @Test
    public void synchronouslyRefreshCognitoTokenIsValid() {
        Mockito.when(identityManager.getMe()).thenReturn(Observable.just(meResponse));
        Mockito.when(user.getCognitoToken()).thenReturn(TOKEN);
        Mockito.when(user.getIdentityId()).thenReturn(IDENTITY_ID);
        Mockito.when(user.getCognitoTokenExpiresAt()).thenReturn(EXPIRES_AT);

        final Cognito cognito = new Cognito(TOKEN, IDENTITY_ID, EXPIRES_AT);
        assertEquals(cognito, cognitoIdentityProvider.synchronouslyRefreshCognitoToken());
    }

    @Test
    public void getCachedCognitoToken() {
        assertEquals(new Cognito(TOKEN, IDENTITY_ID, EXPIRES_AT), cognitoIdentityProvider.getCachedCognitoToken().get());
        Mockito.verify(localCognitoTokenStore).getCognitoToken();
    }

}