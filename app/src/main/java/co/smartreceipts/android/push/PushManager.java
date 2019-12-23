package co.smartreceipts.android.push;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.push.apis.me.UpdatePushTokensRequest;
import co.smartreceipts.android.push.apis.me.UpdateUserPushTokens;
import co.smartreceipts.android.push.internal.FcmTokenRetriever;
import co.smartreceipts.android.push.store.PushDataStore;
import co.smartreceipts.core.utils.log.Logger;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;


@ApplicationScope
public class PushManager {

    private final IdentityManager identityManager;
    private final Analytics analytics;
    private final FcmTokenRetriever fcmTokenRetriever;
    private final PushDataStore pushDataStore;
    private final Scheduler subscribeOnScheduler;
    private final CopyOnWriteArrayList<PushMessageReceiver> pushMessageReceivers = new CopyOnWriteArrayList<>();

    @Inject
    public PushManager(@NonNull Context context,
                       @NonNull IdentityManager identityManager,
                       @NonNull Analytics analytics,
                       @NonNull PushDataStore pushDataStore) {
        this(identityManager, analytics, new FcmTokenRetriever(), pushDataStore, Schedulers.io());
    }

    @VisibleForTesting
    public PushManager(@NonNull IdentityManager identityManager,
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

    public void initialize() {
        identityManager.isLoggedInStream()
                .subscribeOn(subscribeOnScheduler)
                .flatMapSingle(isLoggedIn -> pushDataStore.isRemoteRefreshRequiredSingle()
                        .map(isRefreshRequired -> isRefreshRequired && isLoggedIn))
                .filter(shouldPushTokenBeUploaded -> {
                    Logger.debug(PushManager.this, "Is a push token update required? {}.", shouldPushTokenBeUploaded);
                    return shouldPushTokenBeUploaded;
                })
                .doOnNext(ignore -> analytics.record(Events.Identity.PushTokenUploadRequired))
                .flatMap(ignore -> fcmTokenRetriever.getFcmTokenObservable())
                .flatMap(token -> {
                    final UpdatePushTokensRequest request = new UpdatePushTokensRequest(new UpdateUserPushTokens(Collections.singletonList(Preconditions.checkNotNull(token))));
                    return identityManager.updateMe(request);
                })
                .subscribe(meResponse -> {
                    Logger.info(PushManager.this, "Successfully uploaded our push notification token");
                    pushDataStore.setRemoteRefreshRequired(false);
                    analytics.record(Events.Identity.PushTokenSucceeded);
                }, throwable -> {
                    analytics.record(Events.Identity.PushTokenFailed);
                    analytics.record(new ErrorEvent(PushManager.this, throwable));
                    Logger.error(PushManager.this, "Failed to upload our push notification token", throwable);
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

    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        for (final PushMessageReceiver pushMessageReceiver : pushMessageReceivers) {
            pushMessageReceiver.onMessageReceived(remoteMessage);
        }
    }

}
