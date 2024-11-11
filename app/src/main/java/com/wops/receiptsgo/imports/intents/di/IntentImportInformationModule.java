package com.wops.receiptsgo.imports.intents.di;

import com.wops.receiptsgo.activities.ReceiptsGoActivity;
import com.wops.receiptsgo.imports.intents.widget.info.IntentImportInformationView;
import com.wops.receiptsgo.imports.intents.widget.IntentImportProvider;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class IntentImportInformationModule {

    @Binds
    abstract IntentImportInformationView provideIntentImportInformationView(ReceiptsGoActivity activity);

    @Binds
    abstract IntentImportProvider provideIntentImportProvider(ReceiptsGoActivity activity);

}
