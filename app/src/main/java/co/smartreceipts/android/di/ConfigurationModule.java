package co.smartreceipts.android.di;

import android.content.Context;

import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.config.DefaultConfigurationManager;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.persistence.database.defaults.DefaultTableDefaultCustomizerImpl;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.defaults.WhiteLabelFriendlyTableDefaultsCustomizer;
import dagger.Module;
import dagger.Provides;
import wb.android.flex.Flex;
import wb.android.flex.Flexable;

@Module
public class ConfigurationModule {

    @Provides
    @ApplicationScope
    public static Flex provideFlex(Context context) {
        return new Flex(context, () -> Flexable.UNDEFINED);
    }

    @Provides
    @ApplicationScope
    public static ConfigurationManager provideConfigurationManager(DefaultConfigurationManager manager) {
        return manager;
    }

    @Provides
    @ApplicationScope
    public static TableDefaultsCustomizer provideTableDefaultsCustomizer(Context context, ReceiptColumnDefinitions receiptColumnDefinitions) {
        return new WhiteLabelFriendlyTableDefaultsCustomizer(new DefaultTableDefaultCustomizerImpl(context, receiptColumnDefinitions));
    }
}
