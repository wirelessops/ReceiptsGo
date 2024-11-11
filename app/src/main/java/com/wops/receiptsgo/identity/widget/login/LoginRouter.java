package com.wops.receiptsgo.identity.widget.login;

import javax.inject.Inject;

import com.wops.receiptsgo.activities.NavigationHandler;
import com.wops.core.di.scopes.FragmentScope;

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
