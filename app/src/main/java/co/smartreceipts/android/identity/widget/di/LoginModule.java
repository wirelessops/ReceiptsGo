package co.smartreceipts.android.identity.widget.di;

import co.smartreceipts.android.identity.widget.login.LoginFragment;
import co.smartreceipts.android.identity.widget.login.LoginView;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class LoginModule {

    @Binds
    abstract LoginView provideLoginView(LoginFragment fragment);

}
