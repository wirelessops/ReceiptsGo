package co.smartreceipts.android.aws.s3;

import androidx.test.core.app.ApplicationProvider;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.hadisatrio.optional.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.aws.cognito.CognitoManager;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class S3ClientFactoryTest {

    // Class under test
    S3ClientFactory s3ClientFactory;

    @Mock
    CognitoManager cognitoManager;

    @Mock
    CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(cognitoManager.getCognitoCachingCredentialsProvider()).thenReturn(Observable.just(Optional.of(cognitoCachingCredentialsProvider)));
        s3ClientFactory = new S3ClientFactory(ApplicationProvider.getApplicationContext(), cognitoManager);
    }

    @Test
    public void getAmazonS3() {
        s3ClientFactory.getAmazonS3().test()
                .assertComplete()
                .assertNoErrors();

        final Optional<AmazonS3Client> s3Client = s3ClientFactory.getAmazonS3().blockingFirst();
        assertTrue(s3Client.isPresent());
    }

    @Test
    public void getAmazonS3WhenInitiallyAbsent() {
        final BehaviorSubject<Optional<CognitoCachingCredentialsProvider>> subject = BehaviorSubject.create();
        when(cognitoManager.getCognitoCachingCredentialsProvider()).thenReturn(subject);

        subject.onNext(Optional.<CognitoCachingCredentialsProvider>absent());
        TestObserver<Optional<AmazonS3Client>> testObserver = s3ClientFactory.getAmazonS3().test();
        testObserver
                .assertValueCount(1)
                .assertNotComplete()
                .assertNoErrors();

        final Optional<AmazonS3Client> s3Client1 = s3ClientFactory.getAmazonS3().blockingFirst();
        assertFalse(s3Client1.isPresent());

        // Now re-drive with an actual value:
        subject.onNext(Optional.of(cognitoCachingCredentialsProvider));

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        final Optional<AmazonS3Client> s3Client2 = s3ClientFactory.getAmazonS3().blockingFirst();
        assertTrue(s3Client2.isPresent());
    }
}