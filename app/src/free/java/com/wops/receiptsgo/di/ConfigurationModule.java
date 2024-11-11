package com.wops.receiptsgo.di;

import android.content.Context;

import com.wops.receiptsgo.config.ConfigurationManager;
import com.wops.receiptsgo.config.DefaultConfigurationManager;
import com.wops.receiptsgo.model.impl.columns.receipts.ReceiptColumnDefinitions;
import com.wops.receiptsgo.persistence.database.defaults.DefaultTableDefaultCustomizerImpl;
import com.wops.receiptsgo.persistence.database.defaults.TableDefaultsCustomizer;
import com.wops.receiptsgo.persistence.database.defaults.WhiteLabelFriendlyTableDefaultsCustomizer;
import com.wops.core.di.scopes.ApplicationScope;
import dagger.Module;
import dagger.Provides;
import wb.android.flex.Flex;
import wb.android.flex.Flexable;

/**
 * <b>PLEASE NOTE:</b> Unlike the other modules, this one has been copied between both the free and
 * paid versions of the app to better support client-specific white-labelling features. Should any
 * other configurations be required, it is critical that we copy them to both components.
 */
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

    ///////////////////////////////////////////////////////
    // Remember to copy changes to the free/plus module
    ///////////////////////////////////////////////////////
}
