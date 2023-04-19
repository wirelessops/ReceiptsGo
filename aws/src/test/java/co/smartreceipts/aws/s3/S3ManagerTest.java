package co.smartreceipts.aws.s3;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.hadisatrio.optional.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(RobolectricTestRunner.class)
public class S3ManagerTest {

    private static final String PATH = "path/";
    private static final String RANDOM_KEY = "key";
    private static final String FILE_NAME = "img.jpg";
    private static final String FULL_KEY_PATH = PATH + RANDOM_KEY + FILE_NAME;
    private static final String URL = "https://smartreceipts.co/download";

    // Class under test
    S3Manager s3Manager;

    @Mock
    S3ClientFactory s3ClientFactory;

    @Mock
    AmazonS3Client amazonS3Client;

    @Mock
    S3KeyGeneratorFactory keyGeneratorFactory;

    @Mock
    S3KeyGenerator keyGenerator;

    @Captor
    ArgumentCaptor<String> keyCaptor;

    File file = new File(FILE_NAME);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(s3ClientFactory.getAmazonS3()).thenReturn(Observable.just(Optional.of(amazonS3Client)));
        when(keyGeneratorFactory.get()).thenReturn(Observable.just(keyGenerator));
        when(keyGenerator.getS3Key()).thenReturn(RANDOM_KEY);
        when(amazonS3Client.putObject("smartreceipts", FULL_KEY_PATH, file)).thenReturn(mock(PutObjectResult.class));
        when(amazonS3Client.getResourceUrl("smartreceipts", FULL_KEY_PATH)).thenReturn(URL);

        s3Manager = new S3Manager(s3ClientFactory, keyGeneratorFactory);
    }

    @Test
    public void uploadWithoutS3InstanceFails() {
        when(s3ClientFactory.getAmazonS3()).thenReturn(Observable.just(Optional.<AmazonS3Client>absent()));

        TestObserver<String> testObserver = s3Manager.upload(file, PATH).test();

        verify(amazonS3Client, never()).putObject("smartreceipts", FULL_KEY_PATH, file);
        testObserver.assertNoValues();
        testObserver.assertNotComplete();
        testObserver.assertError(Exception.class);
    }

    @Test
    public void uploadFails() {
        final AmazonClientException exception = new AmazonClientException("test");
        when(amazonS3Client.putObject("smartreceipts", FULL_KEY_PATH, file)).thenThrow(exception);

        TestObserver<String> testObserver = s3Manager.upload(file, PATH).test();

        verify(amazonS3Client).putObject("smartreceipts", FULL_KEY_PATH, file);
        testObserver.assertNoValues();
        testObserver.assertNotComplete();
        testObserver.assertError(exception);
    }

    @Test
    public void uploadSuccess() {
        TestObserver<String> testObserver = s3Manager.upload(file, PATH).test();

        verify(amazonS3Client).putObject("smartreceipts", FULL_KEY_PATH, file);
        testObserver.assertValue(URL);
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

}