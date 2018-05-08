package co.smartreceipts.android.settings.widget;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.AppCompatPreferenceActivity;
import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.DefaultDataPointEvent;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.purchases.PurchaseEventsListener;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.IntentUtils;
import co.smartreceipts.android.utils.log.LogConstants;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.workers.EmailAssistant;
import dagger.android.AndroidInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import wb.android.flex.Flex;
import wb.android.preferences.PlusCheckBoxPreference;
import wb.android.preferences.SummaryEditTextPreference;

public class SettingsActivity extends AppCompatPreferenceActivity implements OnPreferenceClickListener, UniversalPreferences, PurchaseEventsListener {

    public static final String EXTRA_GO_TO_CATEGORY = "GO_TO_CATEGORY";

    @Inject
    Flex flex;
    @Inject
    PersistenceManager persistenceManager;
    @Inject
    PurchaseWallet purchaseWallet;
    @Inject
    Analytics analytics;
    @Inject
    PurchaseManager purchaseManager;

    private volatile Set<InAppPurchase> availablePurchases;
    private CompositeDisposable compositeDisposable;
    private boolean isUsingHeaders;

    /**
     * Ugly hack to determine if a fragment header is currently showing or not. See if I can replace by counting the
     * fragment manager entries
     */
    private boolean isFragmentHeaderShowing = false;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);
        isUsingHeaders = getResources().getBoolean(R.bool.isTablet);

        if (!isUsingHeaders) {
            // Load the legacy preferences headers
            getPreferenceManager().setSharedPreferencesName(UserPreferenceManager.PREFERENCES_FILE_NAME);
            addPreferencesFromResource(R.xml.preference_legacy);
            configurePreferencesGeneral(this);
            configurePreferencesReceipts(this);
            configurePreferencesOutput(this);
            configurePreferencesEmail(this);
            configurePreferencesCamera(this);
            configurePreferencesLayoutCustomizations(this);
            configurePreferencesDistance(this);
            configurePlusPreferences(this);
            configurePreferencesHelp(this);
            configurePreferencesAbout(this);
        }

        purchaseManager.addEventListener(this);


        // Scroll to a predefined preference category (if provided). Only for when not using headers -
        // when we are using headers, selecting the appropriate header is handled by the EXTRA_SHOW_FRAGMENT
        if (!isUsingHeaders) {
            // For some reason (http://stackoverflow.com/a/8167755)
            // getListView().setSelection() won't work in onCreate, onResume or even onPostResume
            // Only way I got it to work was by postDelaying it
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    int sectionHeader = getIntent().getIntExtra(EXTRA_GO_TO_CATEGORY, 0);
                    if (sectionHeader > 0) {
                        scrollToCategory(SettingsActivity.this, getString(sectionHeader));
                    }
                }
            }, 10);
        }
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        final LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        if (root != null) {
            final Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, root, false);
            root.addView(toolbar, 0); // insert at top
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.menu_main_settings);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(purchaseManager.getAllAvailablePurchaseSkus()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(inAppPurchases -> {
                    Logger.info(SettingsActivity.this, "The following purchases are available: {}", availablePurchases);
                    availablePurchases = inAppPurchases;
                }, throwable -> Logger.warn(SettingsActivity.this, "Failed to retrieve purchases for this session.", throwable)));
    }

    public void onBuildHeaders(List<Header> target) {
        // Called before onCreate it seems
        isUsingHeaders = getResources().getBoolean(R.bool.isTablet);
        if (isUsingHeaders) {
            loadHeadersFromResource(R.xml.preference_headers, target);
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        try {
            return AbstractPreferenceHeaderFragment.class.isAssignableFrom(
                    Class.forName(fragmentName));
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (isFragmentHeaderShowing) { // If we're actively showing a fragment, let it handle the call
            return super.onOptionsItemSelected(item);
        }
        if (item.getItemId() == android.R.id.home) {
            final Intent upIntent = new Intent(this, SmartReceiptsActivity.class);
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                // This activity is NOT part of this app's task, so create a new task
                // when navigating up, with a synthesized back stack.
                TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent) // Add all of this activity's
                        // parents to the back stack
                        .startActivities(); // Navigate up to the closest parent
            } else {
                NavUtils.navigateUpTo(this, upIntent);
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!purchaseManager.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPause() {
        compositeDisposable.clear();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        purchaseManager.removeEventListener(this);
        super.onDestroy();
    }

    public boolean isUsingHeaders() {
        return isUsingHeaders;
    }

    public void setFragmentHeaderIsShowing(boolean isShowing) {
        isFragmentHeaderShowing = isShowing;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Preference findPreference(int stringId) {
        return findPreference(getString(stringId));
    }

    public void configurePreferencesGeneral(UniversalPreferences universal) {
        // Get the currency list
//        PersistenceManager persistenceManager = ((SmartReceiptsApplication) getApplication()).getPersistenceManager();
        List<CharSequence> currencyList = persistenceManager.getDatabase().getCurrenciesList();
        CharSequence[] currencyArray = new CharSequence[currencyList.size()];
        currencyList.toArray(currencyArray);

        // Get the date separator list
        final String defaultSepartor = persistenceManager.getPreferenceManager().get(UserPreference.General.DateSeparator);
        final CharSequence[] dateSeparators = getDateSeparatorOptions(persistenceManager.getPreferenceManager());

        // Set up the ListPreference data
        ListPreference currencyPreference = (ListPreference) universal.findPreference(R.string.pref_general_default_currency_key);
        currencyPreference.setEntries(currencyArray);
        currencyPreference.setEntryValues(currencyArray);
        currencyPreference.setValue(persistenceManager.getPreferenceManager().get(UserPreference.General.DefaultCurrency));
        ListPreference dateSeparatorPreference = (ListPreference) universal.findPreference(R.string.pref_general_default_date_separator_key);
        dateSeparatorPreference.setEntries(dateSeparators);
        dateSeparatorPreference.setEntryValues(dateSeparators);
        dateSeparatorPreference.setValue(defaultSepartor);
    }

    private CharSequence[] getDateSeparatorOptions(UserPreferenceManager preferences) {
        final int definedDateSeparatorCount = 3;
        CharSequence[] dateSeparators;
        final String defaultSepartor = preferences.get(UserPreference.General.DateSeparator);
        if (!defaultSepartor.equals("-") && !defaultSepartor.equals("/")) {
            dateSeparators = new CharSequence[definedDateSeparatorCount + 1];
            dateSeparators[definedDateSeparatorCount] = defaultSepartor;
        } else {
            dateSeparators = new CharSequence[definedDateSeparatorCount];
        }
        dateSeparators[0] = "/";
        dateSeparators[1] = "-";
        dateSeparators[2] = ".";
        return dateSeparators;
    }

    public void configurePreferencesReceipts(UniversalPreferences universal) {
        // Set on Preference Click Listeners for all that require it
        universal.findPreference(R.string.pref_receipt_customize_categories_key).setOnPreferenceClickListener(this);
        universal.findPreference(R.string.pref_receipt_payment_methods_key).setOnPreferenceClickListener(this);

        // Here we restore our current values (easier than getting the FloatEditText stuff to work)
        UserPreferenceManager preferences = persistenceManager.getPreferenceManager();
        DefaultTaxPercentagePreference taxPercentagePreference = (DefaultTaxPercentagePreference) universal.findPreference(R.string.pref_receipt_tax_percent_key);
        taxPercentagePreference.setText(Float.toString(preferences.get(UserPreference.Receipts.DefaultTaxPercentage)));
        MinimumPriceEditTextPreference minimumPriceEditTextPreference = (MinimumPriceEditTextPreference) universal.findPreference(R.string.pref_receipt_minimum_receipts_price_key);
        minimumPriceEditTextPreference.setText(Float.toString(preferences.get(UserPreference.Receipts.MinimumReceiptPrice)));

    }

    public void configurePreferencesOutput(UniversalPreferences universal) {
        // Set on Preference Click Listeners for all that require it
        universal.findPreference(R.string.pref_output_custom_csv_key).setOnPreferenceClickListener(this);
        universal.findPreference(R.string.pref_output_custom_pdf_key).setOnPreferenceClickListener(this);
    }

    private void scrollToCategory(UniversalPreferences universal, String sectionHeader) {
        PreferenceCategory category = (PreferenceCategory) universal.findPreference(sectionHeader);
        if (category == null) {
            return;
        }

        for (int i = 0; i < getPreferenceScreen().getRootAdapter().getCount(); i++) {
            Object o = getPreferenceScreen().getRootAdapter().getItem(i);
            if (o.equals(category)) {
                getListView().setSelection(i);
                break;
            }
        }
    }

    public void configurePreferencesEmail(UniversalPreferences universal) {
        Preference subjectPreference = universal.findPreference(R.string.pref_email_default_email_subject_key);
        subjectPreference.setDefaultValue(flex.getString(this, R.string.EMAIL_DATA_SUBJECT));
    }

    public void configurePreferencesCamera(UniversalPreferences universal) {

    }

    public void configurePreferencesLayoutCustomizations(UniversalPreferences universal) {

    }

    public void configurePreferencesDistance(UniversalPreferences universal) {

    }

    public void configurePlusPreferences(UniversalPreferences universal) {
        final boolean hasProSubscription = purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus);
        final SummaryEditTextPreference pdfFooterPreference = (SummaryEditTextPreference) universal.findPreference(R.string.pref_pro_pdf_footer_key);
        pdfFooterPreference.setAppearsEnabled(hasProSubscription);
        pdfFooterPreference.setOnPreferenceClickListener(this);

        final PlusCheckBoxPreference separateByCategoryPreference = (PlusCheckBoxPreference) universal.findPreference(R.string.pref_pro_separate_by_category_key);
        separateByCategoryPreference.setAppearsEnabled(hasProSubscription);
        separateByCategoryPreference.setOnPreferenceClickListener(this);

        final PlusCheckBoxPreference categoricalSummationPreference = (PlusCheckBoxPreference) universal.findPreference(R.string.pref_pro_categorical_summation_key);
        categoricalSummationPreference.setAppearsEnabled(hasProSubscription);
        categoricalSummationPreference.setOnPreferenceClickListener(this);

        final PlusCheckBoxPreference omitDefaultTablePreference = (PlusCheckBoxPreference) universal.findPreference(R.string.pref_pro_omit_default_table_key);
        omitDefaultTablePreference.setAppearsEnabled(hasProSubscription);
        omitDefaultTablePreference.setOnPreferenceClickListener(this);
    }

    public void configurePreferencesHelp(UniversalPreferences universal) {
        // Set on Preference Click Listeners for all that require it
        universal.findPreference(R.string.pref_help_send_feedback_key).setOnPreferenceClickListener(this);
        universal.findPreference(R.string.pref_help_send_love_key).setOnPreferenceClickListener(this);
        universal.findPreference(R.string.pref_help_support_email_key).setOnPreferenceClickListener(this);
    }

    public void configurePreferencesAbout(UniversalPreferences universal) {
        // Set up Version Summary
        Preference versionPreference = universal.findPreference(R.string.pref_about_version_key);
        versionPreference.setSummary(getAppVersion());
        universal.findPreference(R.string.pref_about_privacy_policy_key).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        final String key = preference.getKey();
        if (key.equals(getString(R.string.pref_receipt_customize_categories_key)) || key.equals(getString(R.string.pref_output_custom_csv_key)) || key.equals(getString(R.string.pref_output_custom_pdf_key)) || key.equals(getString(R.string.pref_receipt_payment_methods_key))) {
            final Intent intent = new Intent(this, SettingsViewerActivity.class);
            intent.putExtra(SettingsViewerActivity.KEY_FLAG, key);
            startActivity(intent);
            return true;
        } else if (key.equals(getString(R.string.pref_help_send_love_key))) { // Dark Pattern... Send Love => AppStore.
            // Others for email
            startActivity(IntentUtils.getRatingIntent(this));
            return true;
        } else if (key.equals(getString(R.string.pref_help_send_feedback_key)) || key.equals(getString(R.string.pref_help_support_email_key))) {
            final String emailSubject;
            if (key.equals(getString(R.string.pref_help_send_feedback_key))) {
                emailSubject = getString(R.string.feedback, getString(R.string.sr_app_name));
            } else {
                emailSubject = getString(R.string.support, getString(R.string.sr_app_name));
            }

            final List<File> files = new ArrayList<>();
            final File file1 = new File(getFilesDir(), LogConstants.LOG_FILE_NAME_1);
            final File file2 = new File(getFilesDir(), LogConstants.LOG_FILE_NAME_2);
            if (file1.exists()) {
                files.add(file1);
            }
            if (file2.exists()) {
                files.add(file2);
            }
            final Intent intent = EmailAssistant.getEmailDeveloperIntent(this, emailSubject, getDebugScreen(), files);
            startActivity(Intent.createChooser(intent, getResources().getString(R.string.send_email)));
            return true;
        } else if (key.equals(getString(R.string.pref_pro_pdf_footer_key)) ||
                key.equals(getString(R.string.pref_pro_separate_by_category_key)) ||
                key.equals(getString(R.string.pref_pro_categorical_summation_key)) ||
                key.equals(getString(R.string.pref_pro_omit_default_table_key))) {
            tryToMakePurchaseIfNeed();
            return true;
        } else if (key.equals(getString(R.string.pref_about_privacy_policy_key))) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.smartreceipts.co/privacy")));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onPurchaseSuccess(@NonNull InAppPurchase inAppPurchase, @NonNull PurchaseSource purchaseSource) {
        analytics.record(new DefaultDataPointEvent(Events.Purchases.PurchaseSuccess).addDataPoint(new DataPoint("sku", inAppPurchase.getSku())).addDataPoint(new DataPoint("source", purchaseSource)));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu(); // To hide the subscription option
                Toast.makeText(SettingsActivity.this, R.string.purchase_succeeded, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onPurchaseFailed(@NonNull PurchaseSource purchaseSource) {
        analytics.record(new DefaultDataPointEvent(Events.Purchases.PurchaseFailed).addDataPoint(new DataPoint("source", purchaseSource)));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SettingsActivity.this, R.string.purchase_failed, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getAppVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    private String getDebugScreen() {
        final File[] filesDirs = getExternalFilesDirs(null);
        final int directoryCount = filesDirs != null ? filesDirs.length : 0;
        final boolean hasProSubscription = purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus);
        return "Debug-information: \n" +
                "Smart Receipts Version: " + getAppVersion() + "\n" +
                "Package: " + getPackageName() + "\n" +
                "Plus: " + hasProSubscription + "\n" +
                "Brand: " + Build.BRAND + "\n" +
                "OS API Level: " + Build.VERSION.SDK_INT + "\n" +
                "Device: " + Build.DEVICE + "\n" +
                "Manufacturer: " + Build.MANUFACTURER + "\n" +
                "Model (and Product): " + Build.MODEL + " (" + Build.PRODUCT + ")\n" +
                "Locale: " + Locale.getDefault().toString() + "\n" +
                "Directories: " + directoryCount + "\n" +
                "Two-Paned: " + isUsingHeaders;
    }

    private void tryToMakePurchaseIfNeed() {
        // Let's check if we should prompt the user to upgrade for this preference
        final boolean haveProSubscription = purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus);
        final boolean proSubscriptionIsAvailable = availablePurchases != null && availablePurchases.contains(InAppPurchase.SmartReceiptsPlus);

        // If we don't already have the pro subscription and it's available, let's buy it
        if (!haveProSubscription) {
            if (proSubscriptionIsAvailable) {
                purchaseManager.initiatePurchase(InAppPurchase.SmartReceiptsPlus, PurchaseSource.PdfFooterSetting);
            } else {
                Toast.makeText(SettingsActivity.this, R.string.purchase_unavailable, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
