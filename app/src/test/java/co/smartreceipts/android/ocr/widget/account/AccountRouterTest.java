package co.smartreceipts.android.ocr.widget.account;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.widget.account.AccountRouter;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AccountRouterTest {

    @InjectMocks
    AccountRouter router;

    @Mock
    NavigationHandler navigationHandler;

    @Mock
    IdentityManager identityManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void navigateToProperLocationWhenNotLoggedInForNewSession() {
        when(identityManager.isLoggedIn()).thenReturn(false);
        assertTrue(router.navigateToProperLocation(false));
        verify(navigationHandler).navigateToLoginScreen();
    }

    @Test
    public void navigateToProperLocationWhenNotLoggedInForExistingSession() {
        when(identityManager.isLoggedIn()).thenReturn(false);
        assertFalse(router.navigateToProperLocation(true));
        verify(navigationHandler).navigateBackDelayed();
    }

    @Test
    public void navigateToProperLocationWhenLoggedInForNewSession() {
        when(identityManager.isLoggedIn()).thenReturn(true);
        assertFalse(router.navigateToProperLocation(false));
        verifyZeroInteractions(navigationHandler);
    }

    @Test
    public void navigateToProperLocationWhenLoggedInForExistingSession() {
        when(identityManager.isLoggedIn()).thenReturn(true);
        assertFalse(router.navigateToProperLocation(true));
        verifyZeroInteractions(navigationHandler);
    }

    @Test
    public void navigateBack() {
        router.navigateBack();
        verify(navigationHandler).navigateBack();
    }
    
}