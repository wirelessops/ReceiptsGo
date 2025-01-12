package com.wops.receiptsgo.permissions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import io.reactivex.Single;

import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@SuppressWarnings("unchecked")
public class ActivityPermissionsRequesterTest {

    ActivityPermissionsRequester permissionsRequester;

    @Mock
    HeadlessFragmentPermissionRequesterFactory permissionRequesterFactory;

    @Mock
    PermissionRequesterHeadlessFragment permissionRequesterFragment;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(permissionRequesterFactory.get()).thenReturn(permissionRequesterFragment);
        permissionsRequester = new ActivityPermissionsRequester(permissionRequesterFactory);
    }

    @Test
    public void request() throws Exception {
        final String permission = "permission";
        final PermissionAuthorizationResponse response = new PermissionAuthorizationResponse(permission, true);
        when(permissionRequesterFragment.request(permission)).thenReturn(Single.just(response));
        permissionsRequester.request(permission).test()
                .assertValue(response)
                .assertComplete()
                .assertNoErrors();
    }


    @Test
    public void requestThrowsIllegalStateException() throws Exception {
        final String permission = "permission";
        when(permissionRequesterFragment.request(permission)).thenThrow(new IllegalStateException());
        permissionsRequester.request(permission).test()
                .assertNoValues()
                .assertNotComplete()
                .assertError(IllegalStateException.class);
    }

}