package co.smartreceipts.android.aws.cognito;

import androidx.annotation.NonNull;

import com.amazonaws.auth.AWSAbstractCognitoDeveloperIdentityProvider;
import com.amazonaws.regions.Regions;
import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.util.Collections;

import co.smartreceipts.android.identity.apis.me.Cognito;
import co.smartreceipts.android.utils.log.Logger;

public class SmartReceiptsAuthenticationProvider extends AWSAbstractCognitoDeveloperIdentityProvider {

    private final CognitoIdentityProvider cognitoIdentityProvider;

    public SmartReceiptsAuthenticationProvider(@NonNull CognitoIdentityProvider cognitoIdentityProvider, @NonNull Regions regions) {
        super(null, "us-east-1:cdcc971a-b67f-4bc0-9a12-291b5d416518", regions);

        this.cognitoIdentityProvider = Preconditions.checkNotNull(cognitoIdentityProvider);
        this.setLogins(Collections.singletonMap(getProviderName(), getIdentityId()));
    }

    @Override
    public String getProviderName() {
        return "login.smartreceipts.co";
    }

    /**
     * To be used to call the provider back end to get a token and identityId.
     * Once that has returned, a call to the superclass' update(String, Token)
     * method should be called
     *
     * @return token returns the token that was updated in the refresh
     */
    @Override
    public String refresh() {
        Logger.info(this, "Refreshing Cognito Token");
        // Null out our token
        setToken(null);

        final Cognito cognito = cognitoIdentityProvider.synchronouslyRefreshCognitoToken();
        Logger.debug(this, "Refreshed cognito token is null? {}", cognito == null);

        final String identityId = cognito != null ? cognito.getIdentityId() : null;
        final String token = cognito != null ? cognito.getCognitoToken() : null;

        update(identityId, token);

        return token;
    }

    /**
     * Gets a reference to the identityId of the user (sending a new request if
     * it isn't yet set), using the dev accountId, identityPoolId, and the
     * user's loginsMap to identify.
     */
    @Override
    public String getIdentityId() {
        Logger.info(this, "Requesting Identity Id");
        final Optional<Cognito> cognitoOptional = cognitoIdentityProvider.getCachedCognitoToken();

        return cognitoOptional.isPresent() ? cognitoOptional.get().getIdentityId() : null;
    }

}
