package co.smartreceipts.android.purchases;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.DefaultDataPointEvent;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.purchases.lifecycle.PurchaseManagerActivityLifecycleCallbacks;
import co.smartreceipts.android.purchases.model.AvailablePurchase;
import co.smartreceipts.android.purchases.model.ConsumablePurchase;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.model.ManagedProduct;
import co.smartreceipts.android.purchases.model.ManagedProductFactory;
import co.smartreceipts.android.purchases.model.Subscription;
import co.smartreceipts.android.purchases.rx.RxInAppBillingServiceConnection;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;


@ApplicationScope
public class PurchaseManager {

    public static final int REQUEST_CODE = 5435;

    private static final int BILLING_RESPONSE_CODE_OK = 0;
    private static final int API_VERSION = 3;

    // Purchase state codes
    private static final int PURCHASE_STATE_PURCHASED = 0;
    private static final int PURCHASE_STATE_CANCELLED = 1;
    private static final int PURCHASE_STATE_REFUNDED = 2;

    /**
     * Apparently, this has to be the same across all future sessions to recover this information, so we
     * can't use a random value (unless we build in server-side logic). Adding a hard-coded value instead,
     * since anyone can just download the source for this app anyway
     */
    private static final String HARDCODED_DEVELOPER_PAYLOAD = "1234567890";

    private final Context context;
    private final PurchaseWallet purchaseWallet;
    private final Analytics analytics;
    private final CopyOnWriteArrayList<PurchaseEventsListener> listeners;
    private final String sessionDeveloperPayload;
    private final RxInAppBillingServiceConnection rxInAppBillingServiceConnection;
    private final Scheduler subscribeOnScheduler;
    private final Scheduler observeOnScheduler;
    private final AtomicReference<WeakReference<Activity>> activityReference = new AtomicReference<>(new WeakReference<Activity>(null));
    private final Gson gson = new Gson();

    private CompositeDisposable compositeDisposable;
    private volatile PurchaseSource mPurchaseSource;

    @Inject
    public PurchaseManager(Context context, PurchaseWallet purchaseWallet, Analytics analytics) {
        this(context, purchaseWallet, analytics, Schedulers.io(), AndroidSchedulers.mainThread());
    }

    @VisibleForTesting
    PurchaseManager(@NonNull Context context,
                    @NonNull PurchaseWallet purchaseWallet,
                    @NonNull Analytics analytics,
                    @NonNull Scheduler subscribeOnScheduler,
                    @NonNull Scheduler observeOnScheduler) {
        this.context = context.getApplicationContext();
        this.purchaseWallet = purchaseWallet;
        this.analytics = analytics;
        this.subscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
        this.observeOnScheduler = Preconditions.checkNotNull(observeOnScheduler);

        this.rxInAppBillingServiceConnection = new RxInAppBillingServiceConnection(context, subscribeOnScheduler);
        this.listeners = new CopyOnWriteArrayList<>();
        this.sessionDeveloperPayload = HARDCODED_DEVELOPER_PAYLOAD;
    }

    /**
     * Adds an event listener to our stack in order to start receiving callbacks
     *
     * @param listener the listener to register
     * @return {@code true} if it was successfully registered. {@code false} otherwise
     */
    public boolean addEventListener(@NonNull PurchaseEventsListener listener) {
        return listeners.add(listener);
    }

    /**
     * Removes an event listener from our stack in order to stop receiving callbacks
     *
     * @param listener the listener to unregister
     * @return {@code true} if it was successfully unregistered. {@code false} otherwise
     */
    public boolean removeEventListener(@NonNull PurchaseEventsListener listener) {
        return listeners.remove(listener);
    }

    /**
     * Initializes this class by binding to Google's {@link IInAppBillingService}, fetching a complete
     * list of all entities that we own, and finally persisting all changes to our {@link PurchaseWallet}.
     */
    public void initialize(@NonNull Application application) {
        Logger.debug(PurchaseManager.this, "Initializing the purchase manager");
        application.registerActivityLifecycleCallbacks(new PurchaseManagerActivityLifecycleCallbacks(this));
        getAllOwnedPurchases()
                .subscribeOn(subscribeOnScheduler)
                .subscribe(managedProducts -> Logger.debug(PurchaseManager.this, "Successfully initialized all user owned purchases {}.", managedProducts),
                        throwable -> Logger.error(PurchaseManager.this, "Failed to initialize all user owned purchases.", throwable));
    }

    /**
     * Should be called whenever we resume a new activity in order to allow us to use it for initiating
     * purchases
     *
     * @param activity the current {@link Activity}
     */
    public void onActivityResumed(@NonNull Activity activity) {
        final Activity existingActivity = activityReference.get().get();
        if (!activity.equals(existingActivity)) {
            activityReference.set(new WeakReference<>(activity));
        }
        compositeDisposable = new CompositeDisposable();
    }

    /**
     * Called whenever we pause our current {@link Activity}
     */
    public void onActivityPaused() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

    public void initiatePurchase(@NonNull final InAppPurchase inAppPurchase, @NonNull final PurchaseSource purchaseSource) {
        Logger.info(PurchaseManager.this, "Initiating purchase of {} from {}.", inAppPurchase, purchaseSource);
        analytics.record(new DefaultDataPointEvent(Events.Purchases.ShowPurchaseIntent).addDataPoint(new DataPoint("sku", inAppPurchase.getSku())).addDataPoint(new DataPoint("source", purchaseSource)));

        compositeDisposable.add(getPurchaseIntent(inAppPurchase, purchaseSource)
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe(pendingIntent -> {
                    try {
                        final Activity existingActivity = activityReference.get().get();
                        if (existingActivity != null) {
                            existingActivity.startIntentSenderForResult(pendingIntent.getIntentSender(), PurchaseManager.REQUEST_CODE, new Intent(), 0, 0, 0);
                        }
                    } catch (IntentSender.SendIntentException e) {
                        Toast.makeText(context, R.string.purchase_unavailable, Toast.LENGTH_SHORT).show();
                    }
                }, throwable -> Toast.makeText(context, R.string.purchase_unavailable, Toast.LENGTH_SHORT).show()));
    }

    @VisibleForTesting
    public void sendMockPurchaseRequest(@NonNull InAppPurchase inAppPurchase) {
        try {
            final Intent data = new Intent();
            final JSONObject json = new JSONObject();
            json.put("developerPayload", sessionDeveloperPayload);
            json.put("productId", inAppPurchase.getSku());

            data.putExtra("RESPONSE_CODE", BILLING_RESPONSE_CODE_OK);
            data.putExtra("INAPP_PURCHASE_DATA", json.toString());
            data.putExtra("INAPP_DATA_SIGNATURE", "");
            onActivityResult(REQUEST_CODE, Activity.RESULT_OK, data);
        } catch (JSONException e) {
            Logger.error(PurchaseManager.this, e.toString());
        }
    }

    /**
     * Attempts to complete a purchase request as part of the activity results
     *
     * @return {@code true} if we handled the request. {@code false} otherwise
     */
    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        final PurchaseSource purchaseSource = mPurchaseSource != null ? mPurchaseSource : PurchaseSource.Unknown;
        mPurchaseSource = null;
        if (data == null) {
            return false;
        }
        if (requestCode == REQUEST_CODE) {
            final int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            final String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            final String inAppDataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
            if (resultCode == Activity.RESULT_OK && responseCode == BILLING_RESPONSE_CODE_OK) {
                try {
                    final JSONObject json = new JSONObject(purchaseData);
                    final String actualDeveloperPayload = json.getString("developerPayload");
                    if (sessionDeveloperPayload.equals(actualDeveloperPayload)) {
                        final String sku = json.getString("productId");
                        final InAppPurchase inAppPurchase = InAppPurchase.from(sku);
                        if (inAppPurchase != null) {
                            purchaseWallet.addPurchaseToWallet(new ManagedProductFactory(inAppPurchase, purchaseData, inAppDataSignature).get());
                            for (final PurchaseEventsListener listener : listeners) {
                                listener.onPurchaseSuccess(inAppPurchase, purchaseSource);
                            }
                        } else {
                            for (final PurchaseEventsListener listener : listeners) {
                                listener.onPurchaseFailed(mPurchaseSource);
                            }
                            Logger.warn(PurchaseManager.this, "Retrieved an unknown sku following a successful purchase: {}", sku);
                        }
                    }
                } catch (JSONException e) {
                    Logger.error(PurchaseManager.this, "Failed to find purchase information", e);
                    for (final PurchaseEventsListener listener : listeners) {
                        listener.onPurchaseFailed(purchaseSource);
                    }
                }
            } else {
                Logger.warn(PurchaseManager.this, "Unexpected {resultCode, responseCode} pair: {" + resultCode + ", " + responseCode + "}");
                for (final PurchaseEventsListener listener : listeners) {
                    listener.onPurchaseFailed(purchaseSource);
                }
            }
            return true;
        } else {
            return false;
        }
    }
    
    @NonNull
    public Observable<Set<ManagedProduct>> getAllOwnedPurchases() {
        return Observable.combineLatest(getOwnedConsumablePurchases(), getOwnedSubscriptions(),
                (consumablePurchases, subscriptions) -> {
                    final HashSet<ManagedProduct> combinedSet = new HashSet<>();
                    combinedSet.addAll(consumablePurchases);
                    combinedSet.addAll(subscriptions);
                    return combinedSet;
                })
                .map(purchasedProducts -> {
                    purchaseWallet.updatePurchasesInWallet(purchasedProducts);
                    return purchaseWallet.getActivePurchases();
                })
                .subscribeOn(subscribeOnScheduler);
    }

    @NonNull
    public Observable<Set<AvailablePurchase>> getAllAvailablePurchases() {
        return Observable.combineLatest(getAvailableConsumablePurchases(), getAvailableSubscriptions(),
                (BiFunction<Set<AvailablePurchase>, Set<AvailablePurchase>, Set<AvailablePurchase>>)
                        (consumablePurchases, subscriptions) -> {
                    final HashSet<AvailablePurchase> combinedSet = new HashSet<>();
                    combinedSet.addAll(consumablePurchases);
                    combinedSet.addAll(subscriptions);
                    return combinedSet;
                })
                .map(inAppPurchases -> {
                        final Set<AvailablePurchase> trimmedInAppPurchases = new HashSet<>();
                        for (final AvailablePurchase availablePurchase : inAppPurchases) {
                            if (availablePurchase.getInAppPurchase() != null && !purchaseWallet.hasActivePurchase(availablePurchase.getInAppPurchase())) {
                                trimmedInAppPurchases.add(availablePurchase);
                            } else {
                                Logger.debug(PurchaseManager.this, "Omitting {} from available purchases as we're tracking it as owned.", availablePurchase.getInAppPurchase());
                            }
                        }
                    return trimmedInAppPurchases;
                })
                .subscribeOn(subscribeOnScheduler);
    }

    @NonNull
    public Observable<Set<InAppPurchase>> getAllAvailablePurchaseSkus() {
        return getAllAvailablePurchases()
                .map(availablePurchases ->{
                        final Set<InAppPurchase> inAppPurchases = new HashSet<>();
                        for (final AvailablePurchase availablePurchase : availablePurchases) {
                            inAppPurchases.add(availablePurchase.getInAppPurchase());
                        }
                        return inAppPurchases;
                });
    }

    /**
     * Attempts to consume the purchase of a given {@link ConsumablePurchase}
     *
     * @param consumablePurchase the product to consume
     *
     * @return an {@link io.reactivex.Completable} with the success/error result
     */
    @NonNull
    public Completable consumePurchase(@NonNull final ConsumablePurchase consumablePurchase) {
        Logger.info(PurchaseManager.this, "Consuming the purchase of {}", consumablePurchase.getInAppPurchase());

        return rxInAppBillingServiceConnection.bindToInAppBillingService()
                .flatMapCompletable(inAppBillingService -> Completable.fromAction(() -> {
                    try {
                        final int responseCode = inAppBillingService.consumePurchase(API_VERSION, context.getPackageName(), consumablePurchase.getPurchaseToken());
                        if (BILLING_RESPONSE_CODE_OK == responseCode) {
                            Logger.info(PurchaseManager.this, "Successfully consumed the purchase of {}", consumablePurchase.getInAppPurchase());
                        } else {
                            Logger.warn(PurchaseManager.this, "Received an unexpected response code, {}, for the consumption of this product.", responseCode);
                            throw new Exception("Received an unexpected response code for the consumption of this product.");
                        }
                    } catch (RemoteException e) {
                        Logger.error(PurchaseManager.this, "Failed to consume this purchase", e);
                        throw e;
                    }
                }));
    }

    @VisibleForTesting
    Observable<PendingIntent> getPurchaseIntent(@NonNull final InAppPurchase inAppPurchase, @NonNull final PurchaseSource purchaseSource) {
        return rxInAppBillingServiceConnection.bindToInAppBillingService()
                .flatMap(inAppBillingService -> Observable.fromCallable(() -> {
                    try {
                        final Bundle buyIntentBundle = inAppBillingService.getBuyIntent(API_VERSION, context.getPackageName(), inAppPurchase.getSku(), inAppPurchase.getProductType(), sessionDeveloperPayload);
                        final PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                        if (buyIntentBundle.getInt("RESPONSE_CODE") == BILLING_RESPONSE_CODE_OK && pendingIntent != null) {
                            mPurchaseSource = purchaseSource;
                            return pendingIntent;
                        } else {
                            Logger.warn(PurchaseManager.this, "Received an unexpected response code, {}, for the buy intent.", buyIntentBundle.getInt("RESPONSE_CODE"));
                            throw new RemoteException("Received an unexpected response code for the buy intent.");
                        }
                    } catch (RemoteException e) {
                        Logger.error(PurchaseManager.this, "Failed to get purchase intent", e);
                        throw e;
                    }
                }));
    }

    @NonNull
    @VisibleForTesting
    Observable<Set<ManagedProduct>> getOwnedConsumablePurchases() {
        return getOwnedManagedProductType(ConsumablePurchase.GOOGLE_PRODUCT_TYPE);
    }

    @NonNull
    @VisibleForTesting
    Observable<Set<ManagedProduct>> getOwnedSubscriptions() {
        return getOwnedManagedProductType(Subscription.GOOGLE_PRODUCT_TYPE);
    }

    @NonNull
    @VisibleForTesting
    Observable<Set<AvailablePurchase>> getAvailableConsumablePurchases() {
        return getAvailablePurchases(InAppPurchase.getConsumablePurchaseSkus(), ConsumablePurchase.GOOGLE_PRODUCT_TYPE);
    }

    @NonNull
    @VisibleForTesting
    Observable<Set<AvailablePurchase>> getAvailableSubscriptions() {
        return getAvailablePurchases(InAppPurchase.getSubscriptionSkus(), Subscription.GOOGLE_PRODUCT_TYPE);
    }
    
    @NonNull
    private Observable<Set<ManagedProduct>> getOwnedManagedProductType(@NonNull final String googleProductType) {
        return rxInAppBillingServiceConnection.bindToInAppBillingService()
                .flatMap(inAppBillingService -> Observable.fromCallable(() -> {
                    try {
                        final Bundle ownedItems = inAppBillingService.getPurchases(API_VERSION, context.getPackageName(), googleProductType, null);
                        if (ownedItems.getInt("RESPONSE_CODE") == BILLING_RESPONSE_CODE_OK) {
                            final ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                            final ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                            final ArrayList<String> signatureList = ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                            final Set<ManagedProduct> purchasedProducts = new HashSet<ManagedProduct>();
                            if (purchaseDataList != null && signatureList != null && ownedSkus != null) {
                                for (int i = 0; i < purchaseDataList.size(); ++i) {
                                    final String purchaseDataString = purchaseDataList.get(i);
                                    final JSONObject purchaseData = new JSONObject(purchaseDataString);
                                    final String inAppDataSignature = signatureList.get(i);
                                    final String purchaseToken = purchaseData.getString("purchaseToken");

                                    final String sku = ownedSkus.get(i);
                                    final InAppPurchase inAppPurchase = InAppPurchase.from(sku);
                                    final int purchaseState = purchaseData.has("purchaseState") ? purchaseData.getInt("purchaseState") : PURCHASE_STATE_PURCHASED;

                                    if (inAppPurchase != null && purchaseState == PURCHASE_STATE_PURCHASED) {
                                        purchasedProducts.add(new ManagedProductFactory(inAppPurchase, purchaseDataString, inAppDataSignature).get());
                                    } else {
                                        Logger.warn(PurchaseManager.this, "Failed to process {} in purchase state {}.", sku, purchaseState);
                                    }
                                }
                            }

                            return purchasedProducts;
                        } else {
                            Logger.error(PurchaseManager.this, "Failed to retrieve " + googleProductType + " due to response code error");
                            throw new RemoteException("Failed to retrieve " + googleProductType + " due to response code error");
                        }
                    } catch (RemoteException | JSONException e) {
                        Logger.error(PurchaseManager.this, "Failed to retrieve the user's owned InAppPurchases", e);
                        throw e;
                    }
                }));
    }

    @NonNull
    private Observable<Set<AvailablePurchase>> getAvailablePurchases(@NonNull final ArrayList<String> skus, @NonNull final String googleProductType) {
        return rxInAppBillingServiceConnection.bindToInAppBillingService()
                .subscribeOn(subscribeOnScheduler)
                .flatMap(inAppBillingService -> Observable.fromCallable(() -> {
                    try {
                        // Next, let's figure out what is available for purchase
                        final Set<AvailablePurchase> availablePurchases = new HashSet<>();
                        final Bundle subscriptionsQueryBundle = new Bundle();

                        subscriptionsQueryBundle.putStringArrayList("ITEM_ID_LIST", skus);
                        final Bundle skuDetails = inAppBillingService.getSkuDetails(3, context.getPackageName(), googleProductType, subscriptionsQueryBundle);
                        if (skuDetails.getInt("RESPONSE_CODE") == BILLING_RESPONSE_CODE_OK) {
                            final ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                            if (responseList != null) {
                                for (final String response : responseList) {
                                    final AvailablePurchase availablePurchase = gson.fromJson(response, AvailablePurchase.class);
                                    final InAppPurchase inAppPurchase = availablePurchase.getInAppPurchase();
                                    if (inAppPurchase != null && !PurchaseManager.this.purchaseWallet.hasActivePurchase(inAppPurchase)) {
                                        availablePurchases.add(availablePurchase);
                                    } else {
                                        Logger.warn(PurchaseManager.this, "Unknown or already owned sku returned from the available purchases query: {}.", availablePurchase.getInAppPurchase());
                                    }
                                }
                            }
                            return availablePurchases;
                        } else {
                            Logger.error(PurchaseManager.this, "Failed to get available skus for purchase");
                            throw new RemoteException("Failed to get available skus for purchase");
                        }
                    } catch (RemoteException e) {
                        Logger.error(PurchaseManager.this, "Failed to get available skus for purchase", e);
                        throw e;
                    }
                }));
    }

}
