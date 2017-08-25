package co.smartreceipts.android.imports.locator;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.FileNotFoundException;

import co.smartreceipts.android.imports.RequestCodes;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

@RunWith(RobolectricTestRunner.class)
public class ActivityFileResultLocatorTest {

    // Class under test
    ActivityFileResultLocator locator;

    @Mock
    Intent intent;

    @Mock
    Uri uri;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        locator = new ActivityFileResultLocator(Schedulers.trampoline(), Schedulers.trampoline());
    }

    @Test
    public void onActivityResultCancelled() {
        final TestObserver<ActivityFileResultLocatorResponse> testObserver = locator.getUriStream().test();

        locator.onActivityResult(RequestCodes.IMPORT_GALLERY_IMAGE, Activity.RESULT_CANCELED, null, null);

        testObserver.assertNoValues()
                .assertNotComplete()
                .assertNoErrors()
                .assertSubscribed();
    }

    @Test
    public void onActivityResultWithNullLocation() {
        final TestObserver<ActivityFileResultLocatorResponse> testObserver = locator.getUriStream().test();

        locator.onActivityResult(RequestCodes.IMPORT_GALLERY_IMAGE, Activity.RESULT_OK, intent, null);

        testObserver.assertValue(activityFileResultLocatorResponse -> activityFileResultLocatorResponse.getThrowable().isPresent() &&
                activityFileResultLocatorResponse.getThrowable().get() instanceof FileNotFoundException)
                .assertNotComplete()
                .assertNoErrors()
                .assertSubscribed();
    }

    @Test
    public void onActivityResultResponse() {
        final TestObserver<ActivityFileResultLocatorResponse> testObserver = locator.getUriStream().test();

        locator.onActivityResult(RequestCodes.IMPORT_GALLERY_IMAGE, Activity.RESULT_OK, intent, uri);

        testObserver.assertValue(activityFileResultLocatorResponse -> !activityFileResultLocatorResponse.getThrowable().isPresent() &&
                activityFileResultLocatorResponse.getRequestCode() == RequestCodes.IMPORT_GALLERY_IMAGE &&
                activityFileResultLocatorResponse.getResultCode() == Activity.RESULT_OK &&
                activityFileResultLocatorResponse.getUri().equals(uri))
                .assertNotComplete()
                .assertNoErrors()
                .assertSubscribed();
    }
}
