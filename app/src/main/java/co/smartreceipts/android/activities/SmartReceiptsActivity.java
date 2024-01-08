package co.smartreceipts.android.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

import javax.inject.Inject;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.events.DataPoint;
import co.smartreceipts.analytics.events.DefaultDataPointEvent;
import co.smartreceipts.analytics.events.Events;
import co.smartreceipts.analytics.log.Logger;
import co.smartreceipts.android.R;
import co.smartreceipts.android.ad.AdPresenter;
import co.smartreceipts.android.ad.InterstitialAdPresenter;
import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.fragments.PermissionAlertDialogFragment;
import co.smartreceipts.android.imports.RequestCodes;
import co.smartreceipts.android.imports.intents.model.FileType;
import co.smartreceipts.android.imports.intents.widget.IntentImportProvider;
import co.smartreceipts.android.imports.intents.widget.info.IntentImportInformationPresenter;
import co.smartreceipts.android.imports.intents.widget.info.IntentImportInformationView;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.purchases.PurchaseEventsListener;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.search.SearchActivity;
import co.smartreceipts.android.search.SearchResultKeeper;
import co.smartreceipts.android.search.Searchable;
import co.smartreceipts.android.settings.ThemeProvider;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.subscriptions.SubscriptionsActivity;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.utils.ConfigurableResourceFeature;
import co.smartreceipts.core.identity.IdentityManager;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import wb.android.flex.Flex;

public class SmartReceiptsActivity extends AppCompatActivity implements HasAndroidInjector,
        PurchaseEventsListener, IntentImportInformationView, IntentImportProvider, SearchResultKeeper {

    @Inject
    AdPresenter adPresenter;

    @Inject
    InterstitialAdPresenter interstitialAdPresenter;

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
    DispatchingAndroidInjector<Object> fragmentInjector;

    @Inject
    NavigationHandler<SmartReceiptsActivity> navigationHandler;

    @Inject
    IntentImportInformationPresenter intentImportInformationPresenter;

    @Inject
    UserPreferenceManager userPreferenceManager;

    @Inject
    ThemeProvider themeProvider;

    @Inject
    IdentityManager identityManager;

    private volatile Set<InAppPurchase> availablePurchases;
    private CompositeDisposable compositeDisposable;

    @Nullable
    private Searchable searchResult = null;

    private boolean loginRequested = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);
        Logger.debug(this, "onCreate");

        int theme = themeProvider.getTheme(userPreferenceManager.get(UserPreference.General.Theme));
        AppCompatDelegate.setDefaultNightMode(theme);

        purchaseManager.addEventListener(this);

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            Logger.debug(this, "savedInstanceState == null");
            navigationHandler.navigateToHomeTripsFragment();
        }

        adPresenter.onActivityCreated(this);

        backupProvidersManager.initialize(this);
        intentImportInformationPresenter.subscribe();

        setMainMenuListener();
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
        compositeDisposable.add(purchaseManager.getAllAvailablePurchases()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(inAppPurchases -> {
                    availablePurchases = inAppPurchases;
                    Logger.info(this, "The following purchases are available: {}", availablePurchases);
                    invalidateOptionsMenu(); // To show the subscription option
                }, throwable -> Logger.warn(SmartReceiptsActivity.this, "Failed to retrieve purchases for this session.")));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RequestCodes.SEARCH_REQUEST) { // result from Search Activity
            if (data != null) {
                final Parcelable extra = data.getParcelableExtra(SearchActivity.EXTRA_RESULT);
                switch (resultCode) {
                    case SearchActivity.RESULT_RECEIPT:
                        searchResult = (Receipt) extra;
                        break;
                    case SearchActivity.RESULT_TRIP:
                        searchResult = (Trip) extra;
                        break;
                }
            }
        } else if (requestCode == RequestCodes.SUBSCRIPTIONS_REQUEST) {
            switch (resultCode) {
                case SubscriptionsActivity.RESULT_NEED_LOGIN:
                    loginRequested = true;
                    break;
                case SubscriptionsActivity.RESULT_OK:
                    break;
            }
        } else if (!backupProvidersManager.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        //we need to handle search results here
        // according to https://stackoverflow.com/questions/16265733/failure-delivering-result-onactivityforresult/18345899#18345899
        if (searchResult != null) {
            if (searchResult instanceof Receipt) {
                final Receipt receipt = (Receipt) this.searchResult;
                navigationHandler.navigateToReportInfoFragment(receipt.getTrip());
            } else if (searchResult instanceof Trip) {
                navigationHandler.navigateToHomeTripsFragment();
            } else {
                throw new IllegalStateException("Unexpected search result type: " + searchResult.getClass());
            }
        }

        if (loginRequested) {
            loginRequested = false;
            navigationHandler.navigateToLoginScreen();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_main_more) {
            final boolean haveProSubscription = purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus);
            final boolean proSubscriptionIsAvailable = availablePurchases != null && availablePurchases.contains(InAppPurchase.SmartReceiptsPlus);

            // If the pro sub is either unavailable or we already have it, don't show the purchase menu option
            final boolean hideProSubscription = !proSubscriptionIsAvailable || haveProSubscription
                    || configurationManager.isEnabled(ConfigurableResourceFeature.SubscriptionModel);

            // If we disabled settings in our config, let's remove it
            final boolean hideSettings = !configurationManager.isEnabled(ConfigurableResourceFeature.SettingsMenu);

            // Check OCR availability before enabling this menu item
            final boolean hideOcr = !configurationManager.isEnabled(ConfigurableResourceFeature.Ocr);

            // Check "My Account" availability before enabling this menu item
            final boolean hideMyAccount = !configurationManager.isEnabled(ConfigurableResourceFeature.MyAccount);

            // Check "Subscriptions" availability before enabling this menu item
            final boolean hideSubscriptions = !configurationManager.isEnabled(ConfigurableResourceFeature.SubscriptionModel);

            MainMenuDialog dialog = MainMenuDialog.newInstance(
                    new MainMenuDialogConfig(
                            hideProSubscription, hideSettings, hideOcr, hideMyAccount, hideSubscriptions
                    )
            );
            dialog.show(getSupportFragmentManager(), MainMenuDialog.TAG);
            return true;
        } else if (item.getItemId() == R.id.menu_main_search) {
            navigationHandler.navigateToSearchActivity();
            analytics.record(Events.Navigation.Search);
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
    public void onPurchasePending() {
        runOnUiThread(() -> Toast.makeText(this, R.string.purchase_pending, Toast.LENGTH_LONG).show());
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            navigationHandler.showDialog(PermissionAlertDialogFragment.newInstance(this));
        } else {
            Toast.makeText(this, R.string.toast_attachment_error, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Nullable
    @Override
    public Searchable getSearchResult() {
        return searchResult;
    }

    @Override
    public void markSearchResultAsProcessed() {
        searchResult = null;
    }

    void setMainMenuListener() {
        getSupportFragmentManager().setFragmentResultListener(MainMenuDialog.REQUEST_KEY, this,
                (requestKey, result) -> {
                    String creationOption = result.getString(MainMenuDialog.RESULT_KEY);
                    if (creationOption == null) return;

                    if (creationOption.equals(MainMenuOption.SUBSCRIPTIONS.name())) {
                        navigationHandler.navigateToSubscriptionsActivity();
                    } else if (creationOption.equals(MainMenuOption.SETTINGS.name())) {
                        navigationHandler.navigateToSettings();
                        analytics.record(Events.Navigation.SettingsOverflow);
                    } else if (creationOption.equals(MainMenuOption.OCR_CONFIGURATION.name())) {
                        if (identityManager.isLoggedIn()) {
                            navigationHandler.navigateToOcrConfigurationFragment();
                            analytics.record(Events.Navigation.OcrConfiguration);
                        } else {
                            navigationHandler.navigateToLoginScreen(true);
                        }
                    } else if (creationOption.equals(MainMenuOption.BACKUP.name())) {
                        navigationHandler.navigateToBackupMenu();
                        analytics.record(Events.Navigation.BackupOverflow);
                    } else if (creationOption.equals(MainMenuOption.USAGE_GUIDE.name())) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://smartreceipts.co/guide")));
                        analytics.record(Events.Navigation.UsageGuideOverflow);
                    } else if (creationOption.equals(MainMenuOption.MY_ACCOUNT.name())) {
                        navigationHandler.navigateToAccountScreen();
                        analytics.record(Events.Navigation.MyAccountOverflow);
                    }
                });
    }
}
