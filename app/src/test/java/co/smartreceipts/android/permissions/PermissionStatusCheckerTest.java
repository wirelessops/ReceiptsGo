package co.smartreceipts.android.permissions;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import io.reactivex.Single;

import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class PermissionStatusCheckerTest {

    @Mock
    Context context;

    @InjectMocks
    PermissionStatusChecker permissionStatusChecker;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @SuppressWarnings("all")
    public void isPermissionGranted() throws Exception {
        final String permission = "permission";
        final Single<Boolean> result = permissionStatusChecker.isPermissionGranted(permission);
        verify(context).checkPermission(eq(permission), anyInt(), anyInt());
    }
}