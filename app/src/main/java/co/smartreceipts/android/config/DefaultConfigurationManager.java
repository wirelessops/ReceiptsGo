package co.smartreceipts.android.config;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.core.di.scopes.ApplicationScope;
import co.smartreceipts.android.utils.Feature;

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
    public boolean isEnabled(@NonNull Feature feature) {
        return feature.isEnabled(context);
    }
}
