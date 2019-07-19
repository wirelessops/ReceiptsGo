package co.smartreceipts.android.imports.locator;

import android.net.Uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class FileResultLocatorResponseTest {

    @Test
    public void errorTest() {
        final ActivityFileResultLocatorResponse response = ActivityFileResultLocatorResponse.locatorError(new Exception());

        assertTrue(response.getThrowable().isPresent());
        assertNull(response.getUri());
        assertEquals(0, response.getRequestCode());
        assertEquals(0, response.getResultCode());
    }

    @Test
    public void responseTest() {
        final ActivityFileResultLocatorResponse response = ActivityFileResultLocatorResponse.locatorResponse(Uri.EMPTY, 1, 1);

        assertFalse(response.getThrowable().isPresent());
        assertEquals(Uri.EMPTY, response.getUri());
        assertEquals(1, response.getRequestCode());
        assertEquals(1, response.getResultCode());
    }
}
