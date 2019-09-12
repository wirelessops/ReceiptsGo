package co.smartreceipts.android.aws.cognito;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.hadisatrio.optional.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.apis.me.Cognito;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CognitoManagerTest {

    // Class under test
    CognitoManager cognitoManager;

    Context context = ApplicationProvider.getApplicationContext();

    @Mock
    IdentityManager identityManager;

    @Mock
    CognitoIdentityProvider cognitoIdentityProvider;

    @Mock
    Cognito cognito;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(cognitoIdentityProvider.prefetchCognitoTokenIfNeeded()).thenReturn(Single.just(Optional.of(cognito)));
        when(cognitoIdentityProvider.getCachedCognitoToken()).thenReturn(Optional.of(cognito));
        when(identityManager.isLoggedInStream()).thenReturn(Observable.just(true));
        cognitoManager = new CognitoManager(context, identityManager, cognitoIdentityProvider, Schedulers.trampoline());
    }

    @Test
    public void initializeAndGetCognitoCachingCredentialsProviderWhenNotLoggedIn() {
        final BehaviorSubject<Boolean> isLoggedInStream = BehaviorSubject.createDefault(false);
        when(identityManager.isLoggedInStream()).thenReturn(isLoggedInStream);
        cognitoManager.initialize();

        cognitoManager.getCognitoCachingCredentialsProvider().test()
                .assertValue(Optional.<CognitoCachingCredentialsProvider>absent())
                .assertNotComplete()
                .assertNoErrors();
    }

    @Test
    public void initializeAndGetCognitoCachingCredentialsProviderWhenLoggedIn() {
        final BehaviorSubject<Boolean> isLoggedInStream = BehaviorSubject.createDefault(true);
        when(identityManager.isLoggedInStream()).thenReturn(isLoggedInStream);
        cognitoManager.initialize();

        cognitoManager.getCognitoCachingCredentialsProvider().test()
                .assertValueCount(1)
                .assertComplete()
                .assertNoErrors();
        assertTrue(cognitoManager.getCognitoCachingCredentialsProvider().blockingFirst().isPresent());
    }

    @Test
    public void initializeAndGetCognitoCachingCredentialsProviderWhenLoggedInAfter() {
        final BehaviorSubject<Boolean> isLoggedInStream = BehaviorSubject.createDefault(false);
        when(identityManager.isLoggedInStream()).thenReturn(isLoggedInStream);
        cognitoManager.initialize();

        TestObserver<Optional<CognitoCachingCredentialsProvider>> testObserver = cognitoManager.getCognitoCachingCredentialsProvider().test();
        testObserver.assertValue(Optional.<CognitoCachingCredentialsProvider>absent());
        testObserver.assertNotComplete();
        testObserver.assertNoErrors();

        isLoggedInStream.onNext(true);

        testObserver.assertValueCount(2);
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertTrue(cognitoManager.getCognitoCachingCredentialsProvider().blockingFirst().isPresent());
    }

    @Test
    public void initializeCallsPrefetchCognitoTokenIfNeeded() {
        cognitoManager.initialize();
        verify(cognitoIdentityProvider).prefetchCognitoTokenIfNeeded();
    }

    @Test
    public void getCognitoCachingCredentialsProviderReDrivesCallsPrefetchCognitoTokenOnFailure() {
        cognitoManager.initialize();
        when(cognitoIdentityProvider.prefetchCognitoTokenIfNeeded()).thenReturn(Single.error(new Exception("Test")));
        verify(cognitoIdentityProvider).prefetchCognitoTokenIfNeeded();

        when(cognitoIdentityProvider.prefetchCognitoTokenIfNeeded()).thenReturn(Single.just(Optional.of(cognito)));
        cognitoManager.getCognitoCachingCredentialsProvider().test();
        verify(cognitoIdentityProvider).prefetchCognitoTokenIfNeeded();
    }

}