package co.smartreceipts.aws.cognito;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.hadisatrio.optional.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.core.identity.IdentityManager;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class CognitoManagerTest {

    // Class under test
    CognitoManagerImpl cognitoManager;

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
        Mockito.when(cognitoIdentityProvider.prefetchCognitoTokenIfNeeded()).thenReturn(Single.just(Optional.of(cognito)));
        Mockito.when(cognitoIdentityProvider.getCachedCognitoToken()).thenReturn(Optional.of(cognito));
        Mockito.when(identityManager.isLoggedInStream()).thenReturn(Observable.just(true));
        cognitoManager = new CognitoManagerImpl(context, identityManager, cognitoIdentityProvider, Schedulers.trampoline());
    }

    @Test
    public void initializeAndGetCognitoCachingCredentialsProviderWhenNotLoggedIn() {
        final BehaviorSubject<Boolean> isLoggedInStream = BehaviorSubject.createDefault(false);
        Mockito.when(identityManager.isLoggedInStream()).thenReturn(isLoggedInStream);
        cognitoManager.initialize();

        cognitoManager.getCognitoCachingCredentialsProvider().test()
                .assertValue(Optional.<CognitoCachingCredentialsProvider>absent())
                .assertNotComplete()
                .assertNoErrors();
    }

    @Test
    public void initializeAndGetCognitoCachingCredentialsProviderWhenLoggedIn() {
        final BehaviorSubject<Boolean> isLoggedInStream = BehaviorSubject.createDefault(true);
        Mockito.when(identityManager.isLoggedInStream()).thenReturn(isLoggedInStream);
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
        Mockito.when(identityManager.isLoggedInStream()).thenReturn(isLoggedInStream);
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
        Mockito.verify(cognitoIdentityProvider).prefetchCognitoTokenIfNeeded();
    }

    @Test
    public void getCognitoCachingCredentialsProviderReDrivesCallsPrefetchCognitoTokenOnFailure() {
        cognitoManager.initialize();
        Mockito.when(cognitoIdentityProvider.prefetchCognitoTokenIfNeeded()).thenReturn(Single.error(new Exception("Test")));
        Mockito.verify(cognitoIdentityProvider).prefetchCognitoTokenIfNeeded();

        Mockito.when(cognitoIdentityProvider.prefetchCognitoTokenIfNeeded()).thenReturn(Single.just(Optional.of(cognito)));
        cognitoManager.getCognitoCachingCredentialsProvider().test();
        Mockito.verify(cognitoIdentityProvider).prefetchCognitoTokenIfNeeded();
    }

}