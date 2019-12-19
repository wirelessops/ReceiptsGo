package co.smartreceipts.android.push.services;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.push.PushManager;
import co.smartreceipts.core.utils.log.Logger;

/**
 * There are two types of messages data messages and notification messages. Data messages are handled
 * here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
 * traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
 * is in the foreground. When the app is in the background an automatically generated notification is displayed.
 * When the user taps on the notification they are returned to the app. Messages containing both notification
 * and data payloads are treated as notification messages. The Firebase console always sends notification
 * messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Logger.info(this, "onMessageReceived");
        final SmartReceiptsApplication application = (SmartReceiptsApplication) getApplication();

        PushManager pushManager = application.getAppComponent().providePushManager();
        pushManager.onMessageReceived(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Logger.info(this, "onTokenRefresh");
        final SmartReceiptsApplication application = (SmartReceiptsApplication) getApplication();

        PushManager pushManager = application.getAppComponent().providePushManager();
        pushManager.onTokenRefresh();
    }
}
