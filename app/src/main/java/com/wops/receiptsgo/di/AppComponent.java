package com.wops.receiptsgo.di;

import com.wops.receiptsgo.ReceiptsGoApplication;
import com.wops.push.PushManager;
import com.wops.core.di.scopes.ApplicationScope;
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

    ReceiptsGoApplication inject(ReceiptsGoApplication application);

    PushManager providePushManager();
}
