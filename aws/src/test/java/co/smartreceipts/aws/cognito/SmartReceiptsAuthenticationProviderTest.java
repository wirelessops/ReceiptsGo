package co.smartreceipts.aws.cognito;

import com.amazonaws.regions.Regions;
import com.hadisatrio.optional.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class SmartReceiptsAuthenticationProviderTest {

    private static final String TOKEN = "token";
    private static final String IDENTITY_ID = "identityId";

    // Class under testing
    SmartReceiptsAuthenticationProvider authenticationProvider;

    @Mock
    CognitoIdentityProvider cognitoIdentityProvider;

    @Mock
    Cognito cognitoToken;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cognitoIdentityProvider.getCachedCognitoToken()).thenReturn(Optional.of(cognitoToken));
        Mockito.when(cognitoIdentityProvider.synchronouslyRefreshCognitoToken()).thenReturn(cognitoToken);
        Mockito.when(cognitoToken.getCognitoToken()).thenReturn(TOKEN);
        Mockito.when(cognitoToken.getIdentityId()).thenReturn(IDENTITY_ID);
        authenticationProvider = new SmartReceiptsAuthenticationProvider(cognitoIdentityProvider, Regions.US_EAST_1);

        assertTrue(authenticationProvider.isAuthenticated());
    }

    @Test
    public void getProviderName() {
        assertEquals("login.smartreceipts.co", authenticationProvider.getProviderName());
    }

    @Test
    public void getIdentityId() {
        assertEquals(IDENTITY_ID, authenticationProvider.getIdentityId());
    }

    @Test
    public void getNullIdentityId() {
        Mockito.when(cognitoIdentityProvider.getCachedCognitoToken()).thenReturn(Optional.absent());
        assertEquals(null, authenticationProvider.getIdentityId());
    }

    @Test
    public void refresh() {
        assertEquals(TOKEN, authenticationProvider.refresh());
    }

    @Test
    public void refreshReturnsNullToken() {
        Mockito.when(cognitoIdentityProvider.synchronouslyRefreshCognitoToken()).thenReturn(null);
        assertEquals(null, authenticationProvider.refresh());
    }
}