package co.smartreceipts.android.push;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.RemoteMessage;

public interface PushMessageReceiver {

    /**
     * Called whenever a remote push message is received from Firebase Cloud Messaging
     *
     * @param remoteMessage the {@link RemoteMessage} to handle
     */
    void onMessageReceived(@NonNull RemoteMessage remoteMessage);
}
