package co.smartreceipts.android.permissions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class PermissionAuthorizationResponseTest {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getters() {
        final PermissionAuthorizationResponse response = new PermissionAuthorizationResponse("permission", true);
        assertEquals("permission", response.getPermission());
        assertTrue(response.wasGranted());
    }
}