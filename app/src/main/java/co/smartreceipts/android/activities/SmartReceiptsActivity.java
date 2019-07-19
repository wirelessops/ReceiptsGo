package co.smartreceipts.android.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Set;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.ad.AdPresenter;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.DefaultDataPointEvent;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.imports.intents.model.FileType;
import co.smartreceipts.android.imports.intents.widget.IntentImportProvider;
import co.smartreceipts.android.imports.intents.widget.info.IntentImportInformationPresenter;
import co.smartreceipts.android.imports.intents.widget.info.IntentImportInformationView;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.purchases.PurchaseEventsListener;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.utils.ConfigurableResourceFeature;
import co.smartreceipts.android.utils.log.Logger;
import dagger.android.AndroidInjection;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import wb.android.flex.Flex;

public class SmartReceiptsActivity extends AppCompatActivity implements HasSupportFragmentInjector,
        PurchaseEventsListener, IntentImportInformationView, IntentImportProvider {

    @Inject
    AdPresenter adPresenter;

    @Inject
    Flex flex;

    @Inject
    PersistenceManager persistenceManager;

    @Inject
    PurchaseWallet purchaseWallet;

    @Inject
    ConfigurationManager configurationManager;

    @Inject
    Analytics analytics;

    @Inject
    PurchaseManager purchaseManager;

    @Inject
    BackupProvidersManager backupProvidersManager;

    @Inject
    DispatchingAndroidInjector<Fragment> fragmentInjector;

    @Inject
    NavigationHandler<SmartReceiptsActivity> navigationHandler;

    @Inject
    IntentImportInformationPresenter intentImportInformationPresenter;

    private volatile Set<InAppPurchase> availablePurchases;
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);
        Logger.debug(this, "onCreate");

        purchaseManager.addEventListener(this);

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            Logger.debug(this, "savedInstanceState == null");
            navigationHandler.navigateToHomeTripsFragment();
        }

        adPresenter.onActivityCreated(this);

        backupProvidersManager.initialize(this);
        intentImportInformationPresenter.subscribe();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        intentImportInformationPresenter.subscribe();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.debug(this, "onStart");

        if (persistenceManager.getStorageManager().getRoot() == null) {
            Toast.makeText(SmartReceiptsActivity.this, flex.getString(this, R.string.SD_WARNING), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        Logger.debug(this, "onResumeFragments");

        adPresenter.onResume();
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(purchaseManager.getAllAvailablePurchaseSkus()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(inAppPurchases -> {
                    Logger.info(this, "The following purchases are available: {}", availablePurchases);
                    availablePurchases = inAppPurchases;
                    invalidateOptionsMenu(); // To show the subscription option
                }, throwable -> Logger.warn(SmartReceiptsActivity.this, "Failed to retrieve purchases for this session.")));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!purchaseManager.onActivityResult(requestCode, resultCode, data)) {
            if (!backupProvidersManager.onActivityResult(requestCode, resultCode, data)) {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final boolean haveProSubscription = purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus);
        final boolean proSubscriptionIsAvailable = availablePurchases != null && availablePurchases.contains(InAppPurchase.SmartReceiptsPlus);

        // If the pro sub is either unavailable or we already have it, don't show the purchase menu option
        if (!proSubscriptionIsAvailable || haveProSubscription) {
            menu.removeItem(R.id.menu_main_pro_subscription);
        }

        // If we disabled settings in our config, let's remove it
        if (!configurationManager.isEnabled(ConfigurableResourceFeature.SettingsMenu)) {
            menu.removeItem(R.id.menu_main_settings);
        }

        // Check OCR availability before enabling this menu item
        if (!configurationManager.isEnabled(ConfigurableResourceFeature.Ocr)) {
            menu.removeItem(R.id.menu_main_ocr_configuration);
        }

        // Check "My Account" availability before enabling this menu item
        if (!configurationManager.isEnabled(ConfigurableResourceFeature.MyAccount)) {
            menu.removeItem(R.id.menu_main_my_account);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_main_settings) {
            navigationHandler.navigateToSettings();
            analytics.record(Events.Navigation.SettingsOverflow);
            return true;
        } else if (item.getItemId() == R.id.menu_main_export) {
            navigationHandler.navigateToBackupMenu();
            analytics.record(Events.Navigation.BackupOverflow);
            return true;
        } else if (item.getItemId() == R.id.menu_main_pro_subscription) {
            purchaseManager.initiatePurchase(InAppPurchase.SmartReceiptsPlus, PurchaseSource.OverflowMenu);
            analytics.record(Events.Navigation.SmartReceiptsPlusOverflow);
            return true;
        } else if (item.getItemId() == R.id.menu_main_ocr_configuration) {
            navigationHandler.navigateToOcrConfigurationFragment();
            analytics.record(Events.Navigation.OcrConfiguration);
            return true;
        } else if (item.getItemId() == R.id.menu_main_usage_guide) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.smartreceipts.co/guide")));
            analytics.record(Events.Navigation.UsageGuideOverflow);
            return true;
        } else if (item.getItemId() == R.id.menu_main_my_account) {
            navigationHandler.navigateToAccountScreen();
            analytics.record(Events.Navigation.MyAccountOverflow);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (navigationHandler.shouldFinishOnBackNavigation()) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        Logger.info(this, "onPause");
        adPresenter.onPause();
        compositeDisposable.clear();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Logger.debug(this, "pre-onSaveInstanceState");
        super.onSaveInstanceState(outState);
        Logger.debug(this, "post-onSaveInstanceState");
    }

    @Override
    protected void onDestroy() {
        Logger.info(this, "onDestroy");
        intentImportInformationPresenter.unsubscribe();
        adPresenter.onDestroy();
        purchaseManager.removeEventListener(this);
        persistenceManager.getDatabase().onDestroy();
        super.onDestroy();
    }

    @Override
    public void onPurchaseSuccess(@NonNull final InAppPurchase inAppPurchase, @NonNull final PurchaseSource purchaseSource) {
        analytics.record(new DefaultDataPointEvent(Events.Purchases.PurchaseSuccess).addDataPoint(new DataPoint("sku", inAppPurchase.getSku())).addDataPoint(new DataPoint("source", purchaseSource)));
        runOnUiThread(() -> {
            invalidateOptionsMenu(); // To hide the subscription option
            if (purchaseSource != PurchaseSource.Remote) {
                // Don't show this for remote purchases
                Toast.makeText(SmartReceiptsActivity.this, R.string.purchase_succeeded, Toast.LENGTH_LONG).show();
            }

            if (InAppPurchase.SmartReceiptsPlus == inAppPurchase) {
                adPresenter.onSuccessPlusPurchase();
            }
        });
    }

    @Override
    public void onPurchaseFailed(@NonNull final PurchaseSource purchaseSource) {
        analytics.record(new DefaultDataPointEvent(Events.Purchases.PurchaseFailed).addDataPoint(new DataPoint("source", purchaseSource)));
        runOnUiThread(() -> Toast.makeText(SmartReceiptsActivity.this, R.string.purchase_failed, Toast.LENGTH_LONG).show());
    }

    @Override
    public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentInjector;
    }

    @NonNull
    @Override
    public Maybe<Intent> getIntentMaybe() {
        return Maybe.just(getIntent());
    }

    @Override
    public void presentIntentImportInformation(@NonNull FileType fileType) {
        final int stringId = fileType == FileType.Pdf ? R.string.pdf : R.string.image;
        Toast.makeText(this, getString(R.string.dialog_attachment_text, getString(stringId)), Toast.LENGTH_LONG).show();
    }

    @Override
    public void presentIntentImportFatalError() {
        Toast.makeText(this, R.string.toast_attachment_error, Toast.LENGTH_SHORT).show();
        finish();
    }

}
