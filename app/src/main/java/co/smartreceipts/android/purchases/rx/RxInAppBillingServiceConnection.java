package co.smartreceipts.android.purchases.rx;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import com.android.vending.billing.IInAppBillingService;
import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.util.concurrent.atomic.AtomicBoolean;

import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Cancellable;
import io.reactivex.subjects.BehaviorSubject;


public class RxInAppBillingServiceConnection {

    private final Context context;
    private final Scheduler subscribeOnScheduler;

    public RxInAppBillingServiceConnection(@NonNull Context context, @NonNull Scheduler subscribeOnScheduler) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.subscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
    }

    @NonNull
    public Single<IInAppBillingService> bindToInAppBillingService() {
        return Single.create((SingleOnSubscribe<IInAppBillingService>) emitter -> {
            final ServiceConnection serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    if (!emitter.isDisposed()) {
                        emitter.onSuccess(IInAppBillingService.Stub.asInterface(service));
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    // Intentional no-op
                }
            };
            final Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
            serviceIntent.setPackage("com.android.vending");
            try {
                final boolean wasBound = context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                if (wasBound) {
                    emitter.setCancellable(() -> context.unbindService(serviceConnection));
                } else {
                    // As per the Android docs, we should always unbind to release the connection, even if the expected object was not returned
                    context.unbindService(serviceConnection);
                    if (!emitter.isDisposed()) {
                        emitter.onError(new RemoteException("Failed to bind to the InAppBillingService"));
                    }
                }
            } catch (SecurityException e) {
                if (!emitter.isDisposed()) {
                    emitter.onError(e);
                }
            }
        }).subscribeOn(subscribeOnScheduler);
    }
}
