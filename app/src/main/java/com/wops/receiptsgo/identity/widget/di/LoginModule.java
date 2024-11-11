package com.wops.receiptsgo.identity.widget.di;

import com.wops.receiptsgo.identity.widget.login.LoginFragment;
import com.wops.receiptsgo.identity.widget.login.LoginView;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class LoginModule {

    @Binds
    abstract LoginView provideLoginView(LoginFragment fragment);

}
