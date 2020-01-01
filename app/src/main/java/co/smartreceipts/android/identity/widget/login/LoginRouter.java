package co.smartreceipts.android.identity.widget.login;

import javax.inject.Inject;

import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.core.di.scopes.FragmentScope;

@FragmentScope
public class LoginRouter {

    @Inject
    NavigationHandler navigationHandler;

    @Inject
    public LoginRouter() {

    }

    public boolean navigateBack() {
        return this.navigationHandler.navigateBack();
    }
}
