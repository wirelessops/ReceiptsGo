package co.smartreceipts.android.ocr.widget.configuration;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.utils.log.Logger;

@FragmentScope
public class OcrConfigurationRouter {

    private final NavigationHandler navigationHandler;
    private final IdentityManager identityManager;

    @Inject
    public OcrConfigurationRouter(@NonNull NavigationHandler navigationHandler, @NonNull IdentityManager identityManager) {
        this.navigationHandler = Preconditions.checkNotNull(navigationHandler);
        this.identityManager = Preconditions.checkNotNull(identityManager);
    }

    /**
     * Navigates us to the proper next screen: nowhere if we're logged in, back if the user was previously
     * navigated away and returned here (eg via the backstack), or to the login screen if not logged in
     *
     * @param wasPreviouslyNavigated {@code true} if the user was previously navigated away
     * @return {@code true} if we are sent to the login screen. {@code false} otherwise
     */
    public boolean navigateToProperLocation(boolean wasPreviouslyNavigated) {
        if (!identityManager.isLoggedIn()) {
            if (!wasPreviouslyNavigated) {
                Logger.info(this, "User not logged in. Sending to the log in screen");
                navigationHandler.navigateToLoginScreen();
                return true;
            } else {
                Logger.info(this, "Returning to this fragment after not signing in. Navigating back rather than looping back to the log in screen");
                this.navigationHandler.navigateBackDelayed();
            }
        } else {
            Logger.debug(this, "User is already logged in. Doing nothing and remaining on this screen");
        }
        return false;
    }

    public boolean navigateBack() {
        return this.navigationHandler.navigateBack();
    }
}
