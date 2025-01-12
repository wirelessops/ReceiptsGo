package com.wops.push.ocr;

import android.os.Bundle;

import com.google.firebase.messaging.RemoteMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.Constructor;

import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;


@RunWith(RobolectricTestRunner.class)
public class OcrPushMessageReceiverTest {

    // Class under test
    OcrPushMessageReceiver ocrPushMessageReceiver;

    @Before
    public void setUp() throws Exception {
        ocrPushMessageReceiver = new OcrPushMessageReceiver(Schedulers.trampoline());
    }

    @Test
    public void onMessageReceivedTriggersObservable() throws Exception {
        TestObserver<Object> testObserver = ocrPushMessageReceiver.getPushResponse().test();

        final Constructor<RemoteMessage> constructor = RemoteMessage.class.getDeclaredConstructor(Bundle.class);
        constructor.setAccessible(true);
        final RemoteMessage message = constructor.newInstance(new Bundle());
        ocrPushMessageReceiver.onMessageReceived(message);

        testObserver.assertValueCount(1);
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

}