package com.wops.receiptsgo.sync.network;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;


/**
 * This class provides a simple mechanism to determine whether or not we're currently connected to one or more network
 * types via a composite check
 * 
 * @author Will Baumann
 */
public class CompositeNetworkProviderImpl implements NetworkProvider, NetworkStateChangeListener {

	protected final List<NetworkProvider> mNetworkProviders;
    private final NetworkStateChangeListenerTracker mNetworkStateChangeListenerTracker = new NetworkStateChangeListenerTracker();
    private final AtomicBoolean mIsConnected = new AtomicBoolean(false);

	public CompositeNetworkProviderImpl(@NonNull NetworkProvider... networkProviders) {
		mNetworkProviders = new CopyOnWriteArrayList<>(Arrays.asList(Preconditions.checkNotNull(networkProviders)));
	}

    @Override
    public synchronized void initialize() {
        mIsConnected.set(isNetworkAvailable());
        for (final NetworkProvider networkProvider : mNetworkProviders) {
            networkProvider.initialize();
            networkProvider.registerListener(this);
        }
    }

    @Override
    public synchronized void deinitialize() {
        for (final NetworkProvider networkProvider : mNetworkProviders) {
            networkProvider.deinitialize();
            networkProvider.unregisterListener(this);
        }
        mIsConnected.set(false);
        mNetworkStateChangeListenerTracker.clear();
    }

    @Override
	public synchronized boolean isNetworkAvailable() {
        boolean isNetworkAvailable = false;
        for (final NetworkProvider networkProvider : mNetworkProviders) {
            isNetworkAvailable = isNetworkAvailable || networkProvider.isNetworkAvailable();
        }
        return isNetworkAvailable;
	}

    @Override
    public synchronized void registerListener(@NonNull NetworkStateChangeListener listener) {
        mNetworkStateChangeListenerTracker.registerListener(listener);
    }

    @Override
    public synchronized void unregisterListener(@NonNull NetworkStateChangeListener listener) {
        mNetworkStateChangeListenerTracker.unregisterListener(listener);
    }

    @Override
    public synchronized void onNetworkConnectivityLost() {
        checkForConnectionChange();
    }

    @Override
    public synchronized void onNetworkConnectivityGained() {
        checkForConnectionChange();
    }

    @NonNull
    public Observable<Boolean> getNetworkStateChangeObservable() {
        return mNetworkStateChangeListenerTracker.getNetworkStateChangeObservable();
    }

    protected synchronized void checkForConnectionChange() {
        final boolean hasConnection = isNetworkAvailable();
        if (mIsConnected.compareAndSet(!hasConnection, hasConnection)) {
            if (hasConnection) {
                mNetworkStateChangeListenerTracker.notifyNetworkConnectivityGained();
            } else {
                mNetworkStateChangeListenerTracker.notifyNetworkConnectivityLost();
            }
        }
    }
}
