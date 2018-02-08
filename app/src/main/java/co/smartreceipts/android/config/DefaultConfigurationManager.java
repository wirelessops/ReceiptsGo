package co.smartreceipts.android.config;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.di.scopes.ApplicationScope;

/**
 * The default implementation of the Smart Receipts {@link ConfigurationManager} to enable/disable all standard
 * components within the app.
 */
@ApplicationScope
public final class DefaultConfigurationManager implements ConfigurationManager {

    private final Context context;

    @Inject
    public DefaultConfigurationManager(@NonNull Context context) {
        this.context = Preconditions.checkNotNull(context);
    }

    @Override
    public boolean isSettingsMenuAvailable() {
        return context.getResources().getBoolean(R.bool.config_is_settings_menu_available);
    }


    @Override
    public boolean isTextReceiptsOptionAvailable() {
        return context.getResources().getBoolean(R.bool.config_is_settings_menu_available);
    }


    @Override
    public boolean isDistanceTrackingOptionAvailable() {
        return context.getResources().getBoolean(R.bool.config_is_settings_menu_available);
    }
}
