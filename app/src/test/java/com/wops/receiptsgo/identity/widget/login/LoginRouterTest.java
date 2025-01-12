package com.wops.receiptsgo.identity.widget.login;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import com.wops.receiptsgo.activities.NavigationHandler;

import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class LoginRouterTest {

    @InjectMocks
    LoginRouter router;

    @Mock
    NavigationHandler navigationHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void navigateBack() {
        router.navigateBack();
        verify(navigationHandler).navigateBack();
    }

}