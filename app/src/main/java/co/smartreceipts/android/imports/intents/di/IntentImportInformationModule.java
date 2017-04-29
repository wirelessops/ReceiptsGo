package co.smartreceipts.android.imports.intents.di;

import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.imports.intents.widget.info.IntentImportInformationView;
import co.smartreceipts.android.imports.intents.widget.IntentImportProvider;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class IntentImportInformationModule {

    @Binds
    abstract IntentImportInformationView provideIntentImportInformationView(SmartReceiptsActivity activity);

    @Binds
    abstract IntentImportProvider provideIntentImportProvider(SmartReceiptsActivity activity);

}
