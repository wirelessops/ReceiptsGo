package com.wops.receiptsgo.sync.provider;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.core.di.scopes.ApplicationScope;
import co.smartreceipts.core.sync.provider.SyncProvider;
import dagger.Lazy;

@ApplicationScope
public class SyncProviderStore {

    private static final String KEY_SYNC_PROVIDER = "key_sync_provider_1";

    private final Lazy<SharedPreferences> sharedPreferences;
    private SyncProvider syncProvider;

    @Inject
    public SyncProviderStore(@NonNull Lazy<SharedPreferences> sharedPreferences) {
        this.sharedPreferences = Preconditions.checkNotNull(sharedPreferences);
    }

    @NonNull
    public SyncProvider getProvider() {
        if (this.syncProvider == null) {
            final String syncProviderName = sharedPreferences.get().getString(KEY_SYNC_PROVIDER, "");
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
            sharedPreferences.get().edit().putString(KEY_SYNC_PROVIDER, syncProvider.name()).apply();
            this.syncProvider = syncProvider;
            return true;
        } else {
            return false;
        }
    }

}
