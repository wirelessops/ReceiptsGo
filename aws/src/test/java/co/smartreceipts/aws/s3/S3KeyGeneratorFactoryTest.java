package co.smartreceipts.aws.s3;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class S3KeyGeneratorFactoryTest {

    @Test
    public void getAmazonS3() {
        final S3KeyGeneratorFactory factory = new S3KeyGeneratorFactory();

        factory.get().test()
                .assertComplete()
                .assertNoErrors();

        final S3KeyGenerator generator1 = factory.get().blockingFirst();
        final S3KeyGenerator generator2 = factory.get().blockingFirst();
        assertNotNull(generator1);
        assertNotNull(generator2);
        assertEquals(generator1, generator2);
    }

}