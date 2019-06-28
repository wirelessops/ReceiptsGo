package co.smartreceipts.android.sync.network;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.concurrent.CopyOnWriteArraySet;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;


class NetworkStateChangeListenerTracker {

    private final PublishSubject<Boolean> mSubject = PublishSubject.create();
    private final CopyOnWriteArraySet<NetworkStateChangeListener> mListeners = new CopyOnWriteArraySet<>();

    @NonNull
    public final Observable<Boolean> getNetworkStateChangeObservable() {
        return mSubject;
    }

    public final void registerListener(@NonNull NetworkStateChangeListener listener) {
        mListeners.add(Preconditions.checkNotNull(listener));
    }

    public final void unregisterListener(@NonNull NetworkStateChangeListener listener) {
        mListeners.remove(Preconditions.checkNotNull(listener));
    }

    public void clear() {
        mListeners.clear();
    }

    public final void notifyNetworkConnectivityGained() {
        mSubject.onNext(true);
        for (final NetworkStateChangeListener listener : mListeners) {
            listener.onNetworkConnectivityGained();
        }
    }

    public final void notifyNetworkConnectivityLost() {
        mSubject.onNext(false);
        for (final NetworkStateChangeListener listener : mListeners) {
            listener.onNetworkConnectivityLost();
        }
    }
}
