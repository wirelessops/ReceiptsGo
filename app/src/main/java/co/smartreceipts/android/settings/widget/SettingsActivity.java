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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.app.TaskStackBuilder;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import javax.inject.Inject;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.events.DataPoint;
import co.smartreceipts.analytics.events.DefaultDataPointEvent;
import co.smartreceipts.analytics.events.Events;
import co.smartreceipts.analytics.log.LogConstants;
import co.smartreceipts.analytics.log.Logger;
import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.AppCompatPreferenceActivity;
import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.currency.widget.CurrencyListEditorPresenter;
import co.smartreceipts.android.currency.widget.CurrencyListEditorView;
import co.smartreceipts.android.date.DateFormatter;
import co.smartreceipts.android.date.DisplayableDate;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.purchases.PurchaseEventsListener;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.plus.SmartReceiptsTitle;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.settings.ThemeProvider;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.IntentUtils;
import co.smartreceipts.android.workers.EmailAssistant;
import co.smartreceipts.oss_licenses.LicensesNavigator;
import dagger.android.AndroidInjection;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import wb.android.flex.Flex;
import wb.android.preferences.DeactivatableCheckBoxPreference;
import wb.android.preferences.FloatSummaryEditTextPreference;
import wb.android.preferences.SummaryEditTextPreference;

public class SettingsActivity extends AppCompatPreferenceActivity implements
        OnPreferenceClickListener,
        UniversalPreferences,
        PurchaseEventsListener,
        CurrencyListEditorView {

    public static final String EXTRA_GO_TO_CATEGORY = "GO_TO_CATEGORY";

    @Inject
    SmartReceiptsTitle smartReceiptsTitle;

    @Inject
    Flex flex;

    @Inject
    DatabaseHelper databaseHelper;

    @Inject
    UserPreferenceManager userPreferenceManager;

    @Inject
    PurchaseWallet purchaseWallet;

    @Inject
    Analytics analytics;

    @Inject
    PurchaseManager purchaseManager;

    @Inject
    DateFormatter dateFormatter;

    @Inject
    LicensesNavigator licensesNavigator;

    @Inject
    ThemeProvider themeProvider;

    private volatile Set<InAppPurchase> availablePurchases;
    private CompositeDisposable compositeDisposable;
    private boolean isUsingHeaders;

    // Currency stuff
    private CurrencyListEditorPresenter currencyListEditorPresenter;
    private Bundle savedInstanceState;
    private ListPreference currencyPreference;

    private UniversalPreferences receiptPreferences = null;

    /**
     * Ugly hack to determine if a fragment header is currently showing or not. See if I can replace by counting the
     * fragment manager entries
     */
    private boolean isFragmentHeaderShowing = false;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        int theme = themeProvider.getTheme(userPreferenceManager.get(UserPreference.General.Theme));
        AppCompatDelegate.setDefaultNightMode(theme);

        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        isUsingHeaders = getResources().getBoolean(R.bool.isTablet);

        if (!isUsingHeaders) {
            // Load the legacy preferences headers
            getPreferenceManager().setSharedPreferencesName(UserPreferenceManager.PREFERENCES_FILE_NAME);
            try {
                addPreferencesFromResource(R.xml.preference_legacy);
            } catch (ClassCastException e) {
                Logger.error(this, "Swallowing cast exception due to type mismatch for the PDF Page Size");
                getPreferenceManager().getSharedPreferences().edit().remove(getString(R.string.pref_output_pdf_page_size_key)).apply();

                // Re-load now that we've removed the bad preference
                addPreferencesFromResource(R.xml.preference_legacy);
            }
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
            configurePreferencesPrivacy(this);
            configurePreferencesOSS(this);
        }

        purchaseManager.addEventListener(this);


        // Scroll to a predefined preference category (if provided). Only for when not using headers -
        // when we are using headers, selecting the appropriate header is handled by the EXTRA_SHOW_FRAGMENT
        if (!isUsingHeaders) {
            // For some reason (http://stackoverflow.com/a/8167755)
            // getListView().setSelection() won't work in onCreate, onResume or even onPostResume
            // Only way I got it to work was by postDelaying it
            new Handler().postDelayed(() -> {
                int sectionHeader = getIntent().getIntExtra(EXTRA_GO_TO_CATEGORY, 0);
                if (sectionHeader > 0) {
                    scrollToCategory(SettingsActivity.this, getString(sectionHeader));
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
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(purchaseManager.getAllAvailablePurchaseSkus()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(inAppPurchases -> {
                    availablePurchases = inAppPurchases;
                    Logger.info(SettingsActivity.this, "The following purchases are available: {}", availablePurchases);
                }, throwable -> Logger.warn(SettingsActivity.this, "Failed to retrieve purchases for this session.", throwable)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.menu_main_settings);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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
            return AbstractPreferenceHeaderFragment.class.isAssignableFrom(Class.forName(fragmentName));
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
                TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
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
    protected void onStop() {
        compositeDisposable.clear();
        if (currencyListEditorPresenter != null) {
            currencyListEditorPresenter.unsubscribe();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        purchaseManager.removeEventListener(this);
        super.onDestroy();
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
        // Configure the theme options
        final ListPreference themePreference = (ListPreference) universal.findPreference(R.string.pref_general_theme_key);
        themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue instanceof String) {
                int theme = themeProvider.getTheme((String) newValue);
                AppCompatDelegate.setDefaultNightMode(theme);
            }
            return true;
        });

        // Get the date separator list
        final String defaultSeparator = userPreferenceManager.get(UserPreference.General.DateSeparator);
        final CharSequence[] dateSeparators = getDateSeparatorOptions(userPreferenceManager);

        // Configure out currency list
        currencyPreference = (ListPreference) universal.findPreference(R.string.pref_general_default_currency_key);
        currencyListEditorPresenter = new CurrencyListEditorPresenter(this, databaseHelper, () -> userPreferenceManager.get(UserPreference.General.DefaultCurrency), savedInstanceState);
        currencyListEditorPresenter.subscribe();

        // Configure the date separator list
        final ListPreference dateSeparatorPreference = (ListPreference) universal.findPreference(R.string.pref_general_default_date_separator_key);
        dateSeparatorPreference.setEntries(dateSeparators);
        dateSeparatorPreference.setEntryValues(dateSeparators);
        dateSeparatorPreference.setValue(defaultSeparator);

        // Configure the date format options
        final ListPreference dateFormatPreference = (ListPreference) universal.findPreference(R.string.pref_general_date_format_key);
        final DateFormatter.DateFormatOption[] dateFormatOptions = DateFormatter.DateFormatOption.values();
        final CharSequence[] dateFormatEntries = new CharSequence[dateFormatOptions.length];
        final CharSequence[] dateFormatEntryValues = new CharSequence[dateFormatOptions.length];
        final DisplayableDate today = new DisplayableDate(new Date(Calendar.getInstance().getTimeInMillis()), TimeZone.getDefault());
        for (int i = 0; i < dateFormatOptions.length; i++) {
            final DateFormatter.DateFormatOption dateFormatOption = dateFormatOptions[i];
            dateFormatEntries[i] = dateFormatter.getFormattedDate(today, dateFormatOption);
            dateFormatEntryValues[i] = getString(dateFormatOption.getStringResId());
        }
        final String dateFormatCurrentValue = userPreferenceManager.get(UserPreference.General.DateFormat);
        dateFormatPreference.setEntries(dateFormatEntries);
        dateFormatPreference.setEntryValues(dateFormatEntryValues);
        dateFormatPreference.setValue(dateFormatCurrentValue);
    }

    private CharSequence[] getDateSeparatorOptions(UserPreferenceManager preferences) {
        final int definedDateSeparatorCount = 3;
        CharSequence[] dateSeparators;
        final String defaultSeparator = preferences.get(UserPreference.General.DateSeparator);
        if (!defaultSeparator.equals("-") && !defaultSeparator.equals("/")) {
            dateSeparators = new CharSequence[definedDateSeparatorCount + 1];
            dateSeparators[definedDateSeparatorCount] = defaultSeparator;
        } else {
            dateSeparators = new CharSequence[definedDateSeparatorCount];
        }
        dateSeparators[0] = "/";
        dateSeparators[1] = "-";
        dateSeparators[2] = ".";
        return dateSeparators;
    }

    public void configurePreferencesReceipts(UniversalPreferences universal) {

        if (!this.equals(universal)) {
            receiptPreferences = universal;
        }

        // Set on Preference Click Listeners for all that require it
        universal.findPreference(R.string.pref_receipt_customize_categories_key).setOnPreferenceClickListener(this);
        universal.findPreference(R.string.pref_receipt_payment_methods_key).setOnPreferenceClickListener(this);
        universal.findPreference(R.string.pref_receipt_include_tax2_field_key).setOnPreferenceClickListener(this);
        universal.findPreference(R.string.pref_receipt_include_tax_field_key).setOnPreferenceClickListener(this);

        // Here we restore our current values (easier than getting the FloatEditText stuff to work)
        UserPreferenceManager preferences = userPreferenceManager;
        DefaultTaxPercentagePreference taxPercentagePreference = (DefaultTaxPercentagePreference) universal.findPreference(R.string.pref_receipt_tax_percent_key);
        taxPercentagePreference.setText(Float.toString(preferences.get(UserPreference.Receipts.DefaultTaxPercentage)));
        DefaultTaxPercentagePreference tax2PercentagePreference = (DefaultTaxPercentagePreference) universal.findPreference(R.string.pref_receipt_tax2_percent_key);
        tax2PercentagePreference.setText(Float.toString(preferences.get(UserPreference.Receipts.DefaultTax2Percentage)));
        MinimumPriceEditTextPreference minimumPriceEditTextPreference = (MinimumPriceEditTextPreference) universal.findPreference(R.string.pref_receipt_minimum_receipts_price_key);
        minimumPriceEditTextPreference.setText(Float.toString(preferences.get(UserPreference.Receipts.MinimumReceiptPrice)));

        updateAllTaxesSettings(universal);
    }

    public void configurePreferencesOutput(UniversalPreferences universal) {
        // Set on Preference Click Listeners for all that require it
        universal.findPreference(R.string.pref_output_custom_csv_key).setOnPreferenceClickListener(this);
        universal.findPreference(R.string.pref_output_custom_pdf_key).setOnPreferenceClickListener(this);

        // Set up preferred report language data
        ListPreference preferredLanguagePreference = (ListPreference) universal.findPreference(R.string.pref_output_preferred_language_key);
        final CharSequence[] locales = preferredLanguagePreference.getEntryValues();
        String[] languagesArray = new String[locales.length];
        for (int i = 0; i < locales.length; i++) {
            Locale locale = new Locale(locales[i].toString());
            languagesArray[i] = locale.getDisplayLanguage();
        }
        preferredLanguagePreference.setEntries(languagesArray);
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

    @SuppressWarnings("unused")
    public void configurePreferencesCamera(UniversalPreferences universal) {

    }

    @SuppressWarnings("unused")
    public void configurePreferencesLayoutCustomizations(UniversalPreferences universal) {

    }

    @SuppressWarnings("unused")
    public void configurePreferencesDistance(UniversalPreferences universal) {

    }

    public void configurePlusPreferences(UniversalPreferences universal) {
        final boolean hasProSubscription = purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus);
        final SummaryEditTextPreference pdfFooterPreference = (SummaryEditTextPreference) universal.findPreference(R.string.pref_pro_pdf_footer_key);
        pdfFooterPreference.setAppearsEnabled(hasProSubscription);
        pdfFooterPreference.setOnPreferenceClickListener(this);

        final DeactivatableCheckBoxPreference separateByCategoryPreference = (DeactivatableCheckBoxPreference) universal.findPreference(R.string.pref_pro_separate_by_category_key);
        separateByCategoryPreference.setAppearsEnabled(hasProSubscription);
        separateByCategoryPreference.setOnPreferenceClickListener(this);

        final DeactivatableCheckBoxPreference categoricalSummationPreference = (DeactivatableCheckBoxPreference) universal.findPreference(R.string.pref_pro_categorical_summation_key);
        categoricalSummationPreference.setAppearsEnabled(hasProSubscription);
        categoricalSummationPreference.setOnPreferenceClickListener(this);

        final DeactivatableCheckBoxPreference omitDefaultTablePreference = (DeactivatableCheckBoxPreference) universal.findPreference(R.string.pref_pro_omit_default_table_key);
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
    }

    public void configurePreferencesPrivacy(UniversalPreferences universal) {
        // Set up Privacy Policy
        universal.findPreference(R.string.pref_about_privacy_policy_key).setOnPreferenceClickListener(this);

        final boolean hasProSubscription = purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus);
        if (hasProSubscription) {
            final PreferenceCategory privacyCategory = (PreferenceCategory) universal.findPreference(R.string.pref_privacy_header_key);
            final Preference enableAdPersonalizationPreference = universal.findPreference(R.string.pref_privacy_enable_ad_personalization_key);
            privacyCategory.removePreference(enableAdPersonalizationPreference);
        }
    }

    public void configurePreferencesOSS(UniversalPreferences universal) {
        // Set up OSS
        universal.findPreference(R.string.pref_about_oss_key).setOnPreferenceClickListener(this);
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
                emailSubject = getString(R.string.feedback, smartReceiptsTitle.get());
            } else {
                emailSubject = getString(R.string.support, smartReceiptsTitle.get());
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
        } else if (key.equals(getString(R.string.pref_about_oss_key))) {
            final Intent licensesActivityIntent = licensesNavigator.getLicensesActivityIntent(this, R.string.pref_about_oss_title);
            if (licensesActivityIntent != null) {
                startActivity(licensesActivityIntent);
            }
            return true;
        } else if (key.equals(getString(R.string.pref_receipt_include_tax_field_key))) {
           updateAllTaxesSettings(receiptPreferences == null ? this : receiptPreferences);
            return true;
        } else if (key.equals(getString(R.string.pref_receipt_include_tax2_field_key))) {
            updateTax2SettingsAppearance(receiptPreferences == null ? this : receiptPreferences);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onPurchaseSuccess(@NonNull InAppPurchase inAppPurchase, @NonNull PurchaseSource purchaseSource) {
        analytics.record(new DefaultDataPointEvent(Events.Purchases.PurchaseSuccess).addDataPoint(new DataPoint("sku", inAppPurchase.getSku())).addDataPoint(new DataPoint("source", purchaseSource)));
        runOnUiThread(() -> {
            invalidateOptionsMenu(); // To hide the subscription option
            configurePlusPreferences(this);
            configurePreferencesPrivacy(this); // To remove the ad setting
            Toast.makeText(SettingsActivity.this, R.string.purchase_succeeded, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onPurchaseFailed(@NonNull PurchaseSource purchaseSource) {
        analytics.record(new DefaultDataPointEvent(Events.Purchases.PurchaseFailed).addDataPoint(new DataPoint("source", purchaseSource)));
        runOnUiThread(() -> Toast.makeText(SettingsActivity.this, R.string.purchase_failed, Toast.LENGTH_LONG).show());
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

    private void updateTax1SettingsAppearance(UniversalPreferences universal) {
        final boolean isTaxIncluded = userPreferenceManager.get(UserPreference.Receipts.IncludeTaxField);

        ((SummaryEditTextPreference)universal.findPreference(R.string.pref_receipt_tax1_name_key)).setAppearsEnabled(isTaxIncluded);
        ((FloatSummaryEditTextPreference) universal.findPreference(R.string.pref_receipt_tax_percent_key)).setAppearsEnabled(isTaxIncluded);

        final DeactivatableCheckBoxPreference includeTax2Preference = (DeactivatableCheckBoxPreference) universal.findPreference(R.string.pref_receipt_include_tax2_field_key);
        includeTax2Preference.setAppearsEnabled(isTaxIncluded);

        if (!isTaxIncluded) { // turning off tax2 if tax1 was turned off
            userPreferenceManager.set(UserPreference.Receipts.IncludeTax2Field, false);
            includeTax2Preference.setChecked(false);
        }
    }

    private void updateTax2SettingsAppearance(UniversalPreferences universal) {
        final boolean isTax2Included = userPreferenceManager.get(UserPreference.Receipts.IncludeTax2Field);

        ((SummaryEditTextPreference)universal.findPreference(R.string.pref_receipt_tax2_name_key)).setAppearsEnabled(isTax2Included);
        ((FloatSummaryEditTextPreference) universal.findPreference(R.string.pref_receipt_tax2_percent_key)).setAppearsEnabled(isTax2Included);
    }

    private void updateAllTaxesSettings(UniversalPreferences universal) {
        updateTax1SettingsAppearance(universal);
        updateTax2SettingsAppearance(universal);
    }

    @NonNull
    @Override
    public Consumer<? super List<CharSequence>> displayCurrencies() {
        return currencies -> {
            final CharSequence[] currencyArray = new CharSequence[currencies.size()];
            currencies.toArray(currencyArray);
            currencyPreference.setEntries(currencyArray);
            currencyPreference.setEntryValues(currencyArray);
        };
    }

    @NonNull
    @Override
    public Consumer<? super Integer> displayCurrencySelection() {
        return currencyIndex -> currencyPreference.setValueIndex(currencyIndex);
    }

    @NonNull
    @Override
    public Observable<Integer> currencyClicks() {
        // Intentionally empty as interactions are handled by the prefs themselves
        return Observable.empty();
    }
}
