package co.smartreceipts.android.sync.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;

@ApplicationScope
public class SyncProviderStore {

    private static final String KEY_SYNC_PROVIDER = "key_sync_provider_1";

    private final SharedPreferences mSharedPreferences;
    private SyncProvider syncProvider;

    @Inject
    public SyncProviderStore(@NonNull Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    private SyncProviderStore(@NonNull SharedPreferences sharedPreferences) {
        mSharedPreferences = Preconditions.checkNotNull(sharedPreferences);
    }

    @NonNull
    public SyncProvider getProvider() {
        if (this.syncProvider == null) {
            final String syncProviderName = mSharedPreferences.getString(KEY_SYNC_PROVIDER, "");
            try {
                this.syncProvider = SyncProvider.valueOf(syncProviderName);
            } catch (IllegalArgumentException e) {
                this.syncProvider = SyncProvider.None;
            }
        }
        return this.syncProvider;
    }

    public synchronized boolean setSyncProvider(@NonNull SyncProvider syncProvider) {
        final SyncProvider currentValue = getProvider();
        if (currentValue != syncProvider) {
            mSharedPreferences.edit().putString(KEY_SYNC_PROVIDER, syncProvider.name()).apply();
            this.syncProvider = syncProvider;
            return true;
        } else {
            return false;
        }
    }

}
