package co.smartreceipts.push;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.events.ErrorEvent;
import co.smartreceipts.analytics.events.Events;
import co.smartreceipts.analytics.log.Logger;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import co.smartreceipts.core.identity.IdentityManager;
import co.smartreceipts.core.identity.apis.push.UpdatePushTokensRequest;
import co.smartreceipts.core.identity.apis.push.UpdateUserPushTokens;
import co.smartreceipts.push.internal.FcmTokenRetriever;
import co.smartreceipts.push.store.PushDataStore;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;


@ApplicationScope
public class PushManagerImpl implements PushManager {

    private final IdentityManager identityManager;
    private final Analytics analytics;
    private final FcmTokenRetriever fcmTokenRetriever;
    private final PushDataStore pushDataStore;
    private final Scheduler subscribeOnScheduler;
    private final CopyOnWriteArrayList<PushMessageReceiver> pushMessageReceivers = new CopyOnWriteArrayList<>();

    @Inject
    public PushManagerImpl(@NonNull IdentityManager identityManager,
                           @NonNull Analytics analytics,
                           @NonNull PushDataStore pushDataStore) {
        this(identityManager, analytics, new FcmTokenRetriever(), pushDataStore, Schedulers.io());
    }

    @VisibleForTesting
    public PushManagerImpl(@NonNull IdentityManager identityManager,
                           @NonNull Analytics analytics,
                           @NonNull FcmTokenRetriever fcmTokenRetriever,
                           @NonNull PushDataStore pushDataStore,
                           @NonNull Scheduler subscribeOnScheduler) {
        this.identityManager = Preconditions.checkNotNull(identityManager);
        this.analytics = Preconditions.checkNotNull(analytics);
        this.fcmTokenRetriever = Preconditions.checkNotNull(fcmTokenRetriever);
        this.pushDataStore = Preconditions.checkNotNull(pushDataStore);
        this.subscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
    }

    @Override
    public void initialize() {
        identityManager.isLoggedInStream()
                .subscribeOn(subscribeOnScheduler)
                .flatMapSingle(isLoggedIn -> pushDataStore.isRemoteRefreshRequiredSingle()
                        .map(isRefreshRequired -> isRefreshRequired && isLoggedIn))
                .filter(shouldPushTokenBeUploaded -> {
                    Logger.debug(PushManagerImpl.this, "Is a push token update required? {}.", shouldPushTokenBeUploaded);
                    return shouldPushTokenBeUploaded;
                })
                .doOnNext(ignore -> analytics.record(Events.Identity.PushTokenUploadRequired))
                .flatMap(ignore -> fcmTokenRetriever.getFcmTokenObservable())
                .flatMap(token -> {
                    final UpdatePushTokensRequest request = new UpdatePushTokensRequest(new UpdateUserPushTokens(Collections.singletonList(Preconditions.checkNotNull(token))));
                    return identityManager.updateMe(request);
                })
                .subscribe(meResponse -> {
                    Logger.info(PushManagerImpl.this, "Successfully uploaded our push notification token");
                    pushDataStore.setRemoteRefreshRequired(false);
                    analytics.record(Events.Identity.PushTokenSucceeded);
                }, throwable -> {
                    analytics.record(Events.Identity.PushTokenFailed);
                    analytics.record(new ErrorEvent(PushManagerImpl.this, throwable));
                    Logger.error(PushManagerImpl.this, "Failed to upload our push notification token", throwable);
                });
    }

    public void registerReceiver(@NonNull PushMessageReceiver receiver) {
        pushMessageReceivers.add(Preconditions.checkNotNull(receiver));
    }

    public void unregisterReceiver(@NonNull PushMessageReceiver receiver) {
        pushMessageReceivers.remove(Preconditions.checkNotNull(receiver));
    }

    public void onTokenRefresh() {
        pushDataStore.setRemoteRefreshRequired(true);
        initialize();
    }

    public void onMessageReceived(@NonNull Object remoteMessage) {
        for (final PushMessageReceiver pushMessageReceiver : pushMessageReceivers) {
            pushMessageReceiver.onMessageReceived(remoteMessage);
        }
    }

}
