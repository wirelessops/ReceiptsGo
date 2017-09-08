package co.smartreceipts.android.purchases.rx;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;

import com.android.vending.billing.IInAppBillingService;
import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.subjects.BehaviorSubject;


public class RxInAppBillingServiceConnection implements ServiceConnection {

    private final Context context;
    private final Scheduler subscribeOnScheduler;
    private final AtomicBoolean isBound = new AtomicBoolean(false);
    private final BehaviorSubject<Optional<IInAppBillingService>> inAppBillingServiceSubject = BehaviorSubject.create();

    public RxInAppBillingServiceConnection(@NonNull Context context, @NonNull Scheduler subscribeOnScheduler) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.subscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        inAppBillingServiceSubject.onNext(Optional.of(IInAppBillingService.Stub.asInterface(service)));
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        inAppBillingServiceSubject.onNext(Optional.absent());
    }

    @NonNull
    public Observable<IInAppBillingService> bindToInAppBillingService() {
        if (!isBound.getAndSet(true)) {
            final Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
            serviceIntent.setPackage("com.android.vending");
            context.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
        }

        return inAppBillingServiceSubject
                .take(1)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .subscribeOn(subscribeOnScheduler);
    }
}
