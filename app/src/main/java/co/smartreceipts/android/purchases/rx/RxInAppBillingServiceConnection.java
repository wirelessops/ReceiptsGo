package co.smartreceipts.android.purchases.rx;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import androidx.annotation.NonNull;

import com.android.vending.billing.IInAppBillingService;
import com.google.common.base.Preconditions;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;


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
                    // Note: This callback occurs on the Ui Thread
                    if (!emitter.isDisposed()) {
                        emitter.onSuccess(IInAppBillingService.Stub.asInterface(service));
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    // Note: This callback occurs on the Ui Thread
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
        }).flatMap(service -> {
            // Since our #emitter methods happen on the UiThread, we use this flatMap to switch back to our subscribe on thread
            //noinspection LambdaParameterTypeCanBeSpecified
            return Single.just(service)
                    .subscribeOn(subscribeOnScheduler);
        }).subscribeOn(subscribeOnScheduler);
    }
}
