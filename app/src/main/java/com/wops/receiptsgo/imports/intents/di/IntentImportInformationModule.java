package com.wops.receiptsgo.imports.intents.di;

import com.wops.receiptsgo.activities.SmartReceiptsActivity;
import com.wops.receiptsgo.imports.intents.widget.info.IntentImportInformationView;
import com.wops.receiptsgo.imports.intents.widget.IntentImportProvider;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class IntentImportInformationModule {

    @Binds
    abstract IntentImportInformationView provideIntentImportInformationView(SmartReceiptsActivity activity);

    @Binds
    abstract IntentImportProvider provideIntentImportProvider(SmartReceiptsActivity activity);

}
