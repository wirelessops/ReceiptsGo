package com.wops.receiptsgo.sync.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import co.smartreceipts.analytics.log.Logger;

abstract class AbstractNetworkProvider implements NetworkProvider {


    private final Context mContext;
    private final List<String> mConnectionChangeIntentActions;
    private final ConnectivityChangeBroadcastReceiver mConnectionChangeReceiver;
    private final NetworkStateChangeListenerTracker mNetworkStateChangeListenerTracker = new NetworkStateChangeListenerTracker();
    private final AtomicBoolean mIsConnected = new AtomicBoolean(false);

    public AbstractNetworkProvider(@NonNull Context context, @NonNull String... connectionChangeIntentAction) {
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mConnectionChangeIntentActions = Arrays.asList(Preconditions.checkNotNull(connectionChangeIntentAction));
        mConnectionChangeReceiver = new ConnectivityChangeBroadcastReceiver();
    }

    @Override
    public final synchronized void initialize() {
        mIsConnected.set(isNetworkAvailable());
        final IntentFilter intentFilter = new IntentFilter();
        for (final String connectionChangeIntentAction : mConnectionChangeIntentActions) {
            intentFilter.addAction(connectionChangeIntentAction);
        }
        mContext.registerReceiver(mConnectionChangeReceiver, new IntentFilter(intentFilter));
    }

    @Override
    public final synchronized void deinitialize() {
        mContext.unregisterReceiver(mConnectionChangeReceiver);
        mIsConnected.set(false);
        mNetworkStateChangeListenerTracker.clear();
    }

    @Override
    public final synchronized void registerListener(@NonNull NetworkStateChangeListener listener) {
        mNetworkStateChangeListenerTracker.registerListener(listener);
    }

    @Override
    public final synchronized void unregisterListener(@NonNull NetworkStateChangeListener listener) {
        mNetworkStateChangeListenerTracker.unregisterListener(listener);
    }

    final class ConnectivityChangeBroadcastReceiver extends BroadcastReceiver {

        @Override
        public synchronized void onReceive(Context context, Intent intent) {
            if (mConnectionChangeIntentActions.contains(intent.getAction())) {
                final boolean hasConnection = isNetworkAvailable();
                if (mIsConnected.compareAndSet(!hasConnection, hasConnection)) {
                    Logger.debug(this, "Network connection changed: " + hasConnection);
                    if (hasConnection) {
                        mNetworkStateChangeListenerTracker.notifyNetworkConnectivityGained();
                    } else {
                        mNetworkStateChangeListenerTracker.notifyNetworkConnectivityLost();
                    }
                }
            }
        }

    }

}
