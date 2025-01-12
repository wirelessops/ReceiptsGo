package com.wops.push;

import android.os.Bundle;

import com.google.firebase.messaging.RemoteMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.Constructor;

import com.wops.analytics.Analytics;
import com.wops.core.identity.IdentityManager;
import com.wops.core.identity.apis.me.MeResponse;
import com.wops.core.identity.apis.push.UpdatePushTokensRequest;
import com.wops.push.internal.FcmTokenRetriever;
import com.wops.push.store.PushDataStore;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PushManagerTest {

    private static final String TOKEN = "token";

    // Class under test
    PushManagerImpl pushManager;

    @Mock
    IdentityManager identityManager;

    @Mock
    Analytics analytics;

    @Mock
    FcmTokenRetriever fcmTokenRetriever;

    @Mock
    PushDataStore pushDataStore;

    @Mock
    PushMessageReceiver receiver1, receiver2, receiver3;

    @Mock
    MeResponse meResponse;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        pushManager = new PushManagerImpl(identityManager, analytics, fcmTokenRetriever, pushDataStore, Schedulers.trampoline());
    }

    @Test
    public void initializeWhenNotRequired() throws Exception {
        when(pushDataStore.isRemoteRefreshRequiredSingle()).thenReturn(Single.just(false));
        when(identityManager.isLoggedInStream()).thenReturn(Observable.just(true));
        when(fcmTokenRetriever.getFcmTokenObservable()).thenReturn(Observable.just(TOKEN));
        when(identityManager.updateMe(any(UpdatePushTokensRequest.class))).thenReturn(Observable.just(meResponse));

        pushManager.initialize();

        verify(identityManager, never()).updateMe(any(UpdatePushTokensRequest.class));
        verify(pushDataStore, never()).setRemoteRefreshRequired(false);
    }

    @Test
    public void initializeWhenNotLoggedIn() throws Exception {
        when(pushDataStore.isRemoteRefreshRequiredSingle()).thenReturn(Single.just(true));
        when(identityManager.isLoggedInStream()).thenReturn(Observable.just(false));
        when(fcmTokenRetriever.getFcmTokenObservable()).thenReturn(Observable.just(TOKEN));
        when(identityManager.updateMe(any(UpdatePushTokensRequest.class))).thenReturn(Observable.just(meResponse));

        pushManager.initialize();

        verify(identityManager, never()).updateMe(any(UpdatePushTokensRequest.class));
        verify(pushDataStore, never()).setRemoteRefreshRequired(false);
    }

    @Test
    public void initializeWithNetworkError() throws Exception {
        when(pushDataStore.isRemoteRefreshRequiredSingle()).thenReturn(Single.just(true));
        when(identityManager.isLoggedInStream()).thenReturn(Observable.<Boolean>error(new Exception("test")));
        when(fcmTokenRetriever.getFcmTokenObservable()).thenReturn(Observable.just(TOKEN));
        when(identityManager.updateMe(any(UpdatePushTokensRequest.class))).thenReturn(Observable.just(meResponse));

        pushManager.initialize();

        verify(pushDataStore, never()).setRemoteRefreshRequired(false);
    }

    @Test
    public void initialize() throws Exception {
        when(pushDataStore.isRemoteRefreshRequiredSingle()).thenReturn(Single.just(true));
        when(identityManager.isLoggedInStream()).thenReturn(Observable.just(true));
        when(fcmTokenRetriever.getFcmTokenObservable()).thenReturn(Observable.just(TOKEN));
        when(identityManager.updateMe(any(UpdatePushTokensRequest.class))).thenReturn(Observable.just(meResponse));

        pushManager.initialize();

        verify(pushDataStore).setRemoteRefreshRequired(false);
        verify(identityManager).updateMe(any(UpdatePushTokensRequest.class));
    }

    @Test
    public void onTokenRefreshWhenNotLoggedIn() throws Exception {
        when(pushDataStore.isRemoteRefreshRequiredSingle()).thenReturn(Single.just(true));
        when(identityManager.isLoggedInStream()).thenReturn(Observable.just(false));
        when(fcmTokenRetriever.getFcmTokenObservable()).thenReturn(Observable.just(TOKEN));
        when(identityManager.updateMe(any(UpdatePushTokensRequest.class))).thenReturn(Observable.just(meResponse));

        pushManager.onTokenRefresh();

        verify(pushDataStore).setRemoteRefreshRequired(true);
        verify(identityManager, never()).updateMe(any(UpdatePushTokensRequest.class));
    }

    @Test
    public void onTokenRefresh() throws Exception {
        when(pushDataStore.isRemoteRefreshRequiredSingle()).thenReturn(Single.just(true));
        when(identityManager.isLoggedInStream()).thenReturn(Observable.just(true));
        when(fcmTokenRetriever.getFcmTokenObservable()).thenReturn(Observable.just(TOKEN));
        when(identityManager.updateMe(any(UpdatePushTokensRequest.class))).thenReturn(Observable.just(meResponse));

        pushManager.onTokenRefresh();

        verify(pushDataStore).setRemoteRefreshRequired(true);
        verify(pushDataStore).setRemoteRefreshRequired(false);
        verify(identityManager).updateMe(any(UpdatePushTokensRequest.class));
    }

    @Test
    public void onMessageReceived() throws Exception {
        pushManager.registerReceiver(receiver1);
        pushManager.registerReceiver(receiver2);
        pushManager.registerReceiver(receiver3);
        pushManager.unregisterReceiver(receiver3);

        final Constructor<RemoteMessage> constructor = RemoteMessage.class.getDeclaredConstructor(Bundle.class);
        constructor.setAccessible(true);
        final RemoteMessage message = constructor.newInstance(new Bundle());
        pushManager.onMessageReceived(message);

        verify(receiver1).onMessageReceived(message);
        verify(receiver2).onMessageReceived(message);
        verify(receiver3, never()).onMessageReceived(message);
    }
}