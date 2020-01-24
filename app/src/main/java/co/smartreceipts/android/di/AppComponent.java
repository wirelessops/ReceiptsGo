package co.smartreceipts.android.di;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.push.PushManager;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

@ApplicationScope
@Component(modules = {
        AndroidInjectionModule.class,
        FlavorModule.class,
        GlobalBindingModule.class,
        BaseAppModule.class
})
public interface AppComponent {

    SmartReceiptsApplication inject(SmartReceiptsApplication application);

    PushManager providePushManager();
}
