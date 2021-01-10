package co.smartreceipts.android.receipts.editor;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.hadisatrio.optional.Optional;
import com.jakewharton.rxbinding2.widget.RxDateEditText;
import com.jakewharton.rxbinding3.view.RxView;
import com.jakewharton.rxbinding3.widget.RxTextView;

import org.jetbrains.annotations.NotNull;
import org.joda.money.CurrencyUnit;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Named;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.events.Events;
import co.smartreceipts.analytics.log.Logger;
import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.adapters.FooterButtonArrayAdapter;
import co.smartreceipts.android.adapters.TaxAutoCompleteAdapter;
import co.smartreceipts.android.autocomplete.AutoCompleteArrayAdapter;
import co.smartreceipts.android.autocomplete.AutoCompleteField;
import co.smartreceipts.android.autocomplete.AutoCompletePresenter;
import co.smartreceipts.android.autocomplete.AutoCompleteResult;
import co.smartreceipts.android.autocomplete.AutoCompleteView;
import co.smartreceipts.android.autocomplete.receipt.ReceiptAutoCompleteField;
import co.smartreceipts.android.currency.widget.CurrencyListEditorPresenter;
import co.smartreceipts.android.currency.widget.DefaultCurrencyListEditorView;
import co.smartreceipts.android.databinding.UpdateReceiptBinding;
import co.smartreceipts.android.date.DateEditText;
import co.smartreceipts.android.date.DateFormatter;
import co.smartreceipts.android.editor.Editor;
import co.smartreceipts.android.fragments.ChildFragmentNavigationHandler;
import co.smartreceipts.android.fragments.ReceiptInputCache;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.keyboard.decimal.SamsungDecimalInputPresenter;
import co.smartreceipts.android.keyboard.decimal.SamsungDecimalInputView;
import co.smartreceipts.android.model.AutoCompleteUpdateEvent;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.ocr.apis.model.OcrResponse;
import co.smartreceipts.android.ocr.util.OcrResponseParser;
import co.smartreceipts.android.ocr.widget.tooltip.ReceiptCreateEditFragmentTooltipFragment;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.impl.CategoriesTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.receipts.editor.currency.ReceiptCurrencyCodeSupplier;
import co.smartreceipts.android.receipts.editor.date.ReceiptDateView;
import co.smartreceipts.android.receipts.editor.exchange.CurrencyExchangeRateEditorPresenter;
import co.smartreceipts.android.receipts.editor.exchange.CurrencyExchangeRateEditorView;
import co.smartreceipts.android.receipts.editor.exchange.ExchangeRateServiceManager;
import co.smartreceipts.android.receipts.editor.paymentmethods.PaymentMethodsPresenter;
import co.smartreceipts.android.receipts.editor.paymentmethods.PaymentMethodsView;
import co.smartreceipts.android.receipts.editor.pricing.EditableReceiptPricingView;
import co.smartreceipts.android.receipts.editor.pricing.ReceiptPricingPresenter;
import co.smartreceipts.android.receipts.editor.toolbar.ReceiptsEditorToolbarPresenter;
import co.smartreceipts.android.receipts.editor.toolbar.ReceiptsEditorToolbarView;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.SoftKeyboardManager;
import co.smartreceipts.android.utils.StrictModeConfiguration;
import co.smartreceipts.android.utils.rx.RxSchedulers;
import co.smartreceipts.android.widget.NetworkRequestAwareEditText;
import co.smartreceipts.android.widget.model.UiIndicator;
import co.smartreceipts.android.widget.rxbinding2.RxTextViewExtensions;
import co.smartreceipts.android.widget.tooltip.report.backup.data.BackupReminderTooltipStorage;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import kotlin.Unit;
import wb.android.flex.Flex;

import static java.util.Collections.emptyList;

public class ReceiptCreateEditFragment extends WBFragment implements Editor<Receipt>,
        View.OnFocusChangeListener,
        EditableReceiptPricingView,
        ReceiptDateView,
        CurrencyExchangeRateEditorView,
        SamsungDecimalInputView,
        AutoCompleteView<Receipt>,
        ReceiptsEditorToolbarView,
        PaymentMethodsView {

    public static final String ARG_FILE = "arg_file";
    public static final String ARG_OCR = "arg_ocr";

    @Inject
    Flex flex;

    @Inject
    DatabaseHelper database;

    @Inject
    ExchangeRateServiceManager exchangeRateServiceManager;

    @Inject
    PurchaseManager purchaseManager;

    @Inject
    PurchaseWallet purchaseWallet;

    @Inject
    Analytics analytics;

    @Inject
    CategoriesTableController categoriesTableController;

    @Inject
    ReceiptTableController receiptTableController;

    @Inject
    NavigationHandler navigationHandler;

    @Inject
    BackupReminderTooltipStorage backupReminderTooltipStorage;

    @Inject
    UserPreferenceManager userPreferenceManager;

    @Inject
    DateFormatter dateFormatter;

    @Inject
    SamsungDecimalInputPresenter samsungDecimalInputPresenter;

    @Inject
    AutoCompletePresenter<Receipt> autoCompletePresenter;

    @Inject
    ReceiptsEditorToolbarPresenter receiptsEditorToolbarPresenter;

    @Inject
    PaymentMethodsPresenter paymentMethodsPresenter;

    @Inject
    @Named(RxSchedulers.IO)
    Scheduler ioScheduler;

    @Inject
    @Named(RxSchedulers.MAIN)
    Scheduler mainScheduler;

    private Toolbar toolbar;
    private AutoCompleteTextView nameBox;
    private EditText priceBox;
    private AutoCompleteTextView taxBox1;
    private AutoCompleteTextView taxBox2;
    private Spinner currencySpinner;
    private NetworkRequestAwareEditText exchangeRateBox;
    private EditText exchangedPriceInBaseCurrencyBox;
    private TextView receiptInputExchangeRateBaseCurrencyTextView;
    private DateEditText dateBox;
    private Spinner categoriesSpinner;
    private AutoCompleteTextView commentBox;
    private Spinner paymentMethodsSpinner;
    private CheckBox reimbursableCheckbox;
    private CheckBox fullPageCheckbox;
    private Button decimalSeparatorButton;
    private TextInputLayout taxInputWrapper1;
    private TextInputLayout taxInputWrapper2;

    private List<View> paymentMethodsViewsList;
    private List<View> exchangeRateViewsList;
    private List<View> taxViewsList;

    // Flex fields (ie for white-label projects)
    EditText extraEditText1;
    EditText extraEditText2;
    EditText extraEditText3;

    // Misc views
    private View focusedView;
    private UpdateReceiptBinding binding;

    // Metadata
    private OcrResponse ocrResponse;

    // Presenters
    private CurrencyListEditorPresenter currencyListEditorPresenter;
    private ReceiptPricingPresenter receiptPricingPresenter;
    private CurrencyExchangeRateEditorPresenter currencyExchangeRateEditorPresenter;
    private ReceiptCreateEditFragmentPresenter presenter;

    // Database monitor callbacks
    private TableEventsListener<Category> categoryTableEventsListener;

    // Misc
    private ReceiptInputCache receiptInputCache;
    private List<Category> categoriesList;
    private FooterButtonArrayAdapter<Category> categoriesAdapter;
    private FooterButtonArrayAdapter<PaymentMethod> paymentMethodsAdapter;
    private AutoCompleteArrayAdapter<Receipt> resultsAdapter;
    private Snackbar snackbar;
    private boolean shouldHideResults;
    private AutoCompleteResult<Receipt> itemToRemoveOrReAdd;

    private Subject<AutoCompleteUpdateEvent<Receipt>> _hideAutoCompleteVisibilityClicks =
            PublishSubject.<AutoCompleteUpdateEvent<Receipt>>create().toSerialized();
    private Subject<AutoCompleteUpdateEvent<Receipt>> _unHideAutoCompleteVisibilityClicks =
            PublishSubject.<AutoCompleteUpdateEvent<Receipt>>create().toSerialized();

    @NonNull
    public static ReceiptCreateEditFragment newInstance() {
        return new ReceiptCreateEditFragment();
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.debug(this, "onCreate");

        ocrResponse = (OcrResponse) getArguments().getSerializable(ARG_OCR);
        receiptInputCache = new ReceiptInputCache(requireFragmentManager());
        categoriesList = emptyList();
        categoriesAdapter = new FooterButtonArrayAdapter<>(requireActivity(), new ArrayList<>(),
                R.string.manage_categories, v -> {
            analytics.record(Events.Informational.ClickedManageCategories);
            navigationHandler.navigateToCategoriesEditor();
        });
        paymentMethodsAdapter = new FooterButtonArrayAdapter<>(requireActivity(), new ArrayList<>(),
                R.string.manage_payment_methods, v -> {
            analytics.record(Events.Informational.ClickedManagePaymentMethods);
            navigationHandler.navigateToPaymentMethodsEditor();
        });

        setHasOptionsMenu(true);

        final DefaultCurrencyListEditorView defaultCurrencyListEditorView = new DefaultCurrencyListEditorView(requireContext(), () -> currencySpinner);
        final ReceiptCurrencyCodeSupplier currencyCodeSupplier = new ReceiptCurrencyCodeSupplier(getParentTrip(), receiptInputCache, getEditableItem());
        currencyListEditorPresenter = new CurrencyListEditorPresenter(defaultCurrencyListEditorView, database, currencyCodeSupplier, savedInstanceState);
        receiptPricingPresenter = new ReceiptPricingPresenter(this, userPreferenceManager, getEditableItem(), savedInstanceState, ioScheduler, mainScheduler);
        currencyExchangeRateEditorPresenter = new CurrencyExchangeRateEditorPresenter(this, this, defaultCurrencyListEditorView, this, exchangeRateServiceManager, database, getParentTrip(), getEditableItem(), savedInstanceState);
        presenter = new ReceiptCreateEditFragmentPresenter(this, userPreferenceManager, purchaseManager, purchaseWallet, receiptTableController);
    }

    Trip getParentTrip() {
        return getArguments().getParcelable(Trip.PARCEL_KEY);
    }

    File getFile() {
        return (File) getArguments().getSerializable(ARG_FILE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = UpdateReceiptBinding.inflate(inflater, container, false);

        toolbar = binding.toolbar.toolbar;
        nameBox = binding.DIALOGRECEIPTMENUNAME;
        priceBox = binding.DIALOGRECEIPTMENUPRICE;
        taxBox1 = binding.DIALOGRECEIPTMENUTAX1;
        taxBox2 = binding.DIALOGRECEIPTMENUTAX2;
        currencySpinner = binding.DIALOGRECEIPTMENUCURRENCY;
        exchangeRateBox = binding.receiptInputExchangeRate;
        exchangedPriceInBaseCurrencyBox = binding.receiptInputExchangedResult;
        receiptInputExchangeRateBaseCurrencyTextView = binding.receiptInputExchangeRateBaseCurrency;
        dateBox = binding.DIALOGRECEIPTMENUDATE;
        categoriesSpinner = binding.DIALOGRECEIPTMENUCATEGORY;
        commentBox = binding.DIALOGRECEIPTMENUCOMMENT;
        paymentMethodsSpinner = binding.receiptInputPaymentMethod;
        reimbursableCheckbox = binding.DIALOGRECEIPTMENUEXPENSABLE;
        fullPageCheckbox = binding.DIALOGRECEIPTMENUFULLPAGE;
        decimalSeparatorButton = binding.decimalSeparatorButton;
        taxInputWrapper1 = binding.receiptInputTax1Wrapper;
        taxInputWrapper2 = binding.receiptInputTax2Wrapper;

        paymentMethodsViewsList = new ArrayList<>();
        paymentMethodsViewsList.add(binding.receiptInputGuideImagePaymentMethod);
        paymentMethodsViewsList.add(paymentMethodsSpinner);

        exchangeRateViewsList = new ArrayList<>();
        exchangeRateViewsList.add(binding.receiptInputGuideImageExchangeRate);
        exchangeRateViewsList.add(exchangeRateBox);
        exchangeRateViewsList.add(exchangedPriceInBaseCurrencyBox);
        exchangeRateViewsList.add(receiptInputExchangeRateBaseCurrencyTextView);

        taxViewsList = new ArrayList<>();
        taxViewsList.add(taxInputWrapper1);
        taxViewsList.add(binding.receiptInputGuideImageTax);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            if (isNewReceipt()) {
                new ChildFragmentNavigationHandler(this).addChild(
                        new ReceiptCreateEditFragmentTooltipFragment(), R.id.update_receipt_tooltip);
            }
        }

        // Apply white-label settings via our 'Flex' mechanism to update defaults
        flex.applyCustomSettings(nameBox);
        flex.applyCustomSettings(priceBox);
        flex.applyCustomSettings(taxBox1);
        flex.applyCustomSettings(taxBox2);
        flex.applyCustomSettings(currencySpinner);
        flex.applyCustomSettings(exchangeRateBox);
        flex.applyCustomSettings(dateBox);
        flex.applyCustomSettings(categoriesSpinner);
        flex.applyCustomSettings(commentBox);
        flex.applyCustomSettings(reimbursableCheckbox);
        flex.applyCustomSettings(fullPageCheckbox);

        // Apply white-label settings via our 'Flex' mechanism to add custom fields
        final LinearLayout extras = (LinearLayout) flex.getSubView(getActivity(), view, R.id.DIALOG_RECEIPTMENU_EXTRAS);
        this.extraEditText1 = extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_1));
        this.extraEditText2 = extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_2));
        this.extraEditText3 = extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_3));

        // Toolbar stuff
        if (navigationHandler.isDualPane()) {
            toolbar.setVisibility(View.GONE);
        } else {
            setSupportActionBar(toolbar);
        }

        // Set each focus listener, so we can track the focus view across resume -> pauses
        this.nameBox.setOnFocusChangeListener(this);
        this.priceBox.setOnFocusChangeListener(this);
        this.taxBox1.setOnFocusChangeListener(this);
        this.taxBox2.setOnFocusChangeListener(this);
        this.currencySpinner.setOnFocusChangeListener(this);
        this.dateBox.setOnFocusChangeListener(this);
        this.commentBox.setOnFocusChangeListener(this);
        this.paymentMethodsSpinner.setOnFocusChangeListener(this);
        this.exchangeRateBox.setOnFocusChangeListener(this);
        this.exchangedPriceInBaseCurrencyBox.setOnFocusChangeListener(this);

        // Configure our custom view properties
        exchangeRateBox.setFailedHint(R.string.DIALOG_RECEIPTMENU_HINT_EXCHANGE_RATE_FAILED);

        // And ensure that we do not show the keyboard when clicking these views
        final View.OnTouchListener hideSoftKeyboardOnTouchListener = new SoftKeyboardManager.HideSoftKeyboardOnTouchListener();
        dateBox.setOnTouchListener(hideSoftKeyboardOnTouchListener);
        categoriesSpinner.setOnTouchListener(hideSoftKeyboardOnTouchListener);
        currencySpinner.setOnTouchListener(hideSoftKeyboardOnTouchListener);
        paymentMethodsSpinner.setOnTouchListener(hideSoftKeyboardOnTouchListener);

        // Set-up tax adapter
        if (presenter.isIncludeTaxField()) {
            taxBox1.setAdapter(new TaxAutoCompleteAdapter(getActivity(),
                    priceBox,
                    taxBox1,
                    presenter.isUsePreTaxPrice(),
                    presenter.getDefaultTaxPercentage(),
                    isNewReceipt()));

            if (presenter.isIncludeTax2Field()) {
                taxBox2.setAdapter(new TaxAutoCompleteAdapter(getActivity(),
                        priceBox,
                        taxBox2,
                        presenter.isUsePreTaxPrice(),
                        presenter.getDefaultTax2Percentage(),
                        isNewReceipt()));
            }
        }

        // Set custom tax names if tax2 is enabled
        if (userPreferenceManager.get(UserPreference.Receipts.IncludeTaxField) && userPreferenceManager.get(UserPreference.Receipts.IncludeTax2Field)) {
            taxInputWrapper1.setHint(userPreferenceManager.get(UserPreference.Receipts.Tax1Name));
            taxInputWrapper2.setHint(userPreferenceManager.get(UserPreference.Receipts.Tax2Name));
        }

        // Outline date defaults
        dateBox.setFocusableInTouchMode(false);
        dateBox.setDateFormatter(dateFormatter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Configure things if it's not a restored fragment
        if (savedInstanceState == null) {
            if (isNewReceipt()) { // new receipt

                final Time now = new Time();
                now.setToNow();
                if (receiptInputCache.getCachedDate() == null) {
                    if (presenter.isReceiptDateDefaultsToReportStartDate()) {
                        dateBox.setDate(getParentTrip().getStartDate());
                    } else {
                        dateBox.setDate(new Date(now.toMillis(false)));
                    }
                } else {
                    dateBox.setDate(receiptInputCache.getCachedDate());
                }

                reimbursableCheckbox.setChecked(presenter.isReceiptsDefaultAsReimbursable());

                if (presenter.isMatchReceiptNameToCategory()) {
                    if (focusedView == null) {
                        focusedView = priceBox;
                    }
                }

                fullPageCheckbox.setChecked(presenter.isDefaultToFullPage());

                if (ocrResponse != null) {
                    final OcrResponseParser ocrResponseParser = new OcrResponseParser(ocrResponse);
                    if (ocrResponseParser.getMerchant() != null) {
                        nameBox.setText(ocrResponseParser.getMerchant());
                    }

                    if (presenter.isIncludeTaxField() && ocrResponseParser.getTaxAmount() != null) {
                        taxBox1.setText(ocrResponseParser.getTaxAmount());
                        if (ocrResponseParser.getTotalAmount() != null) {
                            if (presenter.isUsePreTaxPrice()) {
                                // If we're in pre-tax mode, let's calculate the price as (total - tax = pre-tax-price)
                                final BigDecimal preTaxPrice = ModelUtils.tryParse(ocrResponseParser.getTotalAmount()).subtract(ModelUtils.tryParse(ocrResponseParser.getTaxAmount()));
                                priceBox.setText(ModelUtils.getDecimalFormattedValue(preTaxPrice));
                            } else {
                                priceBox.setText(ocrResponseParser.getTotalAmount());
                            }
                        }
                    } else if (ocrResponseParser.getTotalAmount() != null) {
                        priceBox.setText(ocrResponseParser.getTotalAmount());
                    }

                    if (ocrResponseParser.getDate() != null) {
                        dateBox.setDate(ocrResponseParser.getDate());
                    }
                }

            } else { // edit receipt
                final Receipt receipt = getEditableItem();

                nameBox.setText(receipt.getName());
                dateBox.setDate(receipt.getDate());
                dateBox.setTimeZone(receipt.getTimeZone());
                commentBox.setText(receipt.getComment());

                reimbursableCheckbox.setChecked(receipt.isReimbursable());
                fullPageCheckbox.setChecked(receipt.isFullPage());

                if (extraEditText1 != null && receipt.hasExtraEditText1()) {
                    extraEditText1.setText(receipt.getExtraEditText1());
                }
                if (extraEditText2 != null && receipt.hasExtraEditText2()) {
                    extraEditText2.setText(receipt.getExtraEditText2());
                }
                if (extraEditText3 != null && receipt.hasExtraEditText3()) {
                    extraEditText3.setText(receipt.getExtraEditText3());
                }
            }

            // Focused View
            if (focusedView == null) {
                focusedView = nameBox;
            }

        }

        // Configure items that require callbacks (note: Move these to presenters at some point for testing)
        categoryTableEventsListener = new StubTableEventsListener<Category>() {
            @Override
            public void onGetSuccess(@NonNull List<Category> list) {
                if (isAdded()) {
                    categoriesList = list;
                    categoriesAdapter.update(list);
                    categoriesSpinner.setAdapter(categoriesAdapter);

                    if (getEditableItem() == null) { // new receipt
                        if (presenter.isPredictCategories()) { // Predict Breakfast, Lunch, Dinner by the hour
                            if (receiptInputCache.getCachedCategory() == null) {
                                final Time now = new Time();
                                now.setToNow();
                                String nameToIndex = null;
                                if (now.hour >= 4 && now.hour < 11) { // Breakfast hours
                                    nameToIndex = getString(R.string.category_breakfast);
                                } else if (now.hour >= 11 && now.hour < 16) { // Lunch hours
                                    nameToIndex = getString(R.string.category_lunch);
                                } else if (now.hour >= 16 && now.hour < 23) { // Dinner hours
                                    nameToIndex = getString(R.string.category_dinner);
                                }
                                if (nameToIndex != null) {
                                    for (int i = 0; i < categoriesAdapter.getCount(); i++) {
                                        final Category category = categoriesAdapter.getItem(i);
                                        if (category != null && nameToIndex.equals(category.getName())) {
                                            categoriesSpinner.setSelection(i);
                                            break; // Exit loop now
                                        }
                                    }
                                }
                            } else {
                                int idx = categoriesAdapter.getPosition(receiptInputCache.getCachedCategory());
                                if (idx > 0) {
                                    categoriesSpinner.setSelection(idx);
                                }
                            }
                        }
                    } else {
                        // Here we manually loop through all categories and check for id == id in case the user changed this via "Manage"
                        final Category receiptCategory = getEditableItem().getCategory();
                        for (int i = 0; i < categoriesAdapter.getCount(); i++) {
                            final Category category = categoriesAdapter.getItem(i);
                            if (category != null && category.getId() == receiptCategory.getId()) {
                                categoriesSpinner.setSelection(i);
                                break;
                            }
                        }
                    }

                    if (presenter.isMatchReceiptCommentToCategory() || presenter.isMatchReceiptNameToCategory()) {
                        categoriesSpinner.setOnItemSelectedListener(new SpinnerSelectionListener());
                    }
                }
            }
        };

        categoriesTableController.subscribe(categoryTableEventsListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        samsungDecimalInputPresenter.subscribe();
        autoCompletePresenter.subscribe();
        currencyListEditorPresenter.subscribe();
        receiptPricingPresenter.subscribe();
        currencyExchangeRateEditorPresenter.subscribe();
        receiptsEditorToolbarPresenter.subscribe();
        paymentMethodsPresenter.subscribe();
        presenter.subscribe();

        // Attempt to update our lists in case they were changed in the background
        categoriesTableController.get();

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_24dp);
            actionBar.setSubtitle("");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.debug(this, "onResume");
        if (focusedView != null) {
            focusedView.requestFocus(); // Make sure we're focused on the right view
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_save, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            StrictModeConfiguration.permitDiskWrites(() -> presenter.deleteReceiptFileIfUnused());
            navigationHandler.navigateBack();
            return true;
        }
        if (item.getItemId() == R.id.action_save) {
            saveReceipt();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        focusedView = hasFocus ? view : null;
        if (isNewReceipt() && hasFocus) {
            // Only launch if we have focus and it's a new receipt
            SoftKeyboardManager.showKeyboard(view);
        }
    }

    @Override
    public void onPause() {
        // Dismiss the soft keyboard
        SoftKeyboardManager.hideKeyboard(focusedView);

        super.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.debug(this, "onSaveInstanceState");

        // Presenters
        currencyListEditorPresenter.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        paymentMethodsPresenter.unsubscribe();
        receiptsEditorToolbarPresenter.unsubscribe();
        receiptPricingPresenter.unsubscribe();
        currencyListEditorPresenter.unsubscribe();
        currencyExchangeRateEditorPresenter.unsubscribe();
        autoCompletePresenter.unsubscribe();
        samsungDecimalInputPresenter.unsubscribe();
        presenter.unsubscribe();
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Logger.debug(this, "onDestroyView");
        extraEditText1 = null;
        extraEditText2 = null;
        extraEditText3 = null;
        focusedView = null;
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        categoriesTableController.unsubscribe(categoryTableEventsListener);
        super.onDestroy();
    }

    private boolean isNewReceipt() {
        return getEditableItem() == null;
    }

    private void saveReceipt() {
        if (presenter.checkReceipt(dateBox.getDate())) {
            final String name = TextUtils.isEmpty(nameBox.getText().toString()) ? "" : nameBox.getText().toString();
            final Category category = categoriesAdapter.getItem(categoriesSpinner.getSelectedItemPosition());
            final String currency = currencySpinner.getSelectedItem().toString();
            final String price = priceBox.getText().toString();
            final String tax = taxBox1.getText().toString();
            final String tax2 = taxBox2.getText().toString();
            final String exchangeRate = exchangeRateBox.getText() != null ? exchangeRateBox.getText().toString() : "";
            final String comment = commentBox.getText().toString();
            final PaymentMethod paymentMethod = (PaymentMethod) (presenter.isUsePaymentMethods() ? paymentMethodsSpinner.getSelectedItem() : null);
            final String extraText1 = (extraEditText1 == null) ? null : extraEditText1.getText().toString();
            final String extraText2 = (extraEditText2 == null) ? null : extraEditText2.getText().toString();
            final String extraText3 = (extraEditText3 == null) ? null : extraEditText3.getText().toString();
            final TimeZone timeZone = dateBox.getTimeZone();
            final Date receiptDate;

            // updating date just if it was really changed (to prevent reordering)
            if (getEditableItem() != null && getEditableItem().getDate().equals(dateBox.getDate())) {
                receiptDate = getEditableItem().getDate();
            } else {
                receiptDate = dateBox.getDate();
            }

            receiptInputCache.setCachedDate((Date) dateBox.getDate().clone());
            receiptInputCache.setCachedCategory(category);
            receiptInputCache.setCachedCurrency(currency);

            presenter.saveReceipt(receiptDate, timeZone, price, tax, tax2, exchangeRate, comment,
                    paymentMethod, reimbursableCheckbox.isChecked(), fullPageCheckbox.isChecked(), name, category, currency,
                    extraText1, extraText2, extraText3);

            analytics.record(isNewReceipt() ? Events.Receipts.PersistNewReceipt : Events.Receipts.PersistUpdateReceipt);

            backupReminderTooltipStorage.setOneMoreNewReceipt();

            navigationHandler.navigateBack();
        }
    }

    public void showDateError() {
        Toast.makeText(getActivity(), getFlexString(R.string.CALENDAR_TAB_ERROR), Toast.LENGTH_SHORT).show();
    }

    public void showDateWarning() {
        Toast.makeText(getActivity(), getFlexString(R.string.DIALOG_RECEIPTMENU_TOAST_BAD_DATE), Toast.LENGTH_LONG).show();
    }

    @NonNull
    @UiThread
    @Override
    public Consumer<? super Price> displayReceiptPrice() {
        return RxTextViewExtensions.price(priceBox);
    }

    @NonNull
    @UiThread
    @Override
    public Consumer<? super Price> displayReceiptTax() {
        return RxTextViewExtensions.price(taxBox1);
    }

    @NonNull
    @UiThread
    @Override
    public Consumer<? super Price> displayReceiptTax2() {
        return RxTextViewExtensions.price(taxBox2);
    }

    @NonNull
    @UiThread
    @Override
    public Consumer<? super Boolean> toggleReceiptTaxFieldVisibility() {
        return (Consumer<Boolean>) isVisible -> {
            for (View v : taxViewsList) {
                v.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            }
        };
    }

    @NonNull
    @UiThread
    @Override
    public Consumer<? super Boolean> toggleReceiptTax2FieldVisibility() {
        return RxView.visibility(taxInputWrapper2);
    }

    @NonNull
    @UiThread
    @Override
    public Observable<CharSequence> getReceiptPriceChanges() {
        return RxTextView.textChanges(priceBox);
    }

    @NonNull
    @UiThread
    @Override
    public Observable<CharSequence> getReceiptTaxChanges() {
        return Observable.merge(RxTextView.textChanges(taxBox1), RxTextView.textChanges(taxBox2));
    }

    @NonNull
    @UiThread
    @Override
    public Observable<Date> getReceiptDateChanges() {
        return RxDateEditText.INSTANCE.dateChanges(dateBox)
                .doOnNext(ignored -> {
                    if (exchangedPriceInBaseCurrencyBox.isFocused()) {
                        exchangedPriceInBaseCurrencyBox.clearFocus();
                    }
                });
    }

    @NonNull
    @UiThread
    @Override
    public Consumer<? super Boolean> toggleExchangeRateFieldVisibility() {
        return (Consumer<Boolean>) isVisible -> {
            for (View v : exchangeRateViewsList) {
                v.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            }
        };
    }

    @NonNull
    @UiThread
    @Override
    public Consumer<? super UiIndicator<ExchangeRate>> displayExchangeRate() {
        return (Consumer<UiIndicator<ExchangeRate>>) exchangeRateUiIndicator -> {
            if (exchangeRateUiIndicator.getState() == UiIndicator.State.Loading) {
                exchangeRateBox.setText("");
                exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Loading);
            } else if (exchangeRateUiIndicator.getState() == UiIndicator.State.Error) {
                exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Failure);
            } else if (exchangeRateUiIndicator.getState() == UiIndicator.State.Success) {
                if (exchangeRateUiIndicator.getData().isPresent()) {
                    if (TextUtils.isEmpty(exchangeRateBox.getText()) || exchangedPriceInBaseCurrencyBox.isFocused()) {
                        final BigDecimal exchangeRate = exchangeRateUiIndicator.getData().get().getExchangeRate(getParentTrip().getTripCurrency());
                        final String exchangeRateString = exchangeRate != null ? exchangeRate.setScale(ExchangeRate.PRECISION, RoundingMode.HALF_UP).toPlainString() : "";
                        exchangeRateBox.setText(exchangeRateString);
                    } else {
                        Logger.warn(ReceiptCreateEditFragment.this, "Ignoring remote exchange rate result now that one is already set");
                    }
                    exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Success);
                } else {
                    // If the data is empty, reset to use the ready state to allow for user interaction
                    exchangeRateBox.setText("");
                }
            } else {
                exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Ready);
            }
        };
    }

    @NonNull
    @UiThread
    @Override
    public Consumer<? super CurrencyUnit> displayBaseCurrency() {
        return (Consumer<CurrencyUnit>) priceCurrency -> {
            receiptInputExchangeRateBaseCurrencyTextView.setText(priceCurrency.getCode());
        };
    }

    @NonNull
    @UiThread
    @Override
    public Consumer<? super Optional<Price>> displayExchangedPriceInBaseCurrency() {
        return RxTextViewExtensions.priceOptional(exchangedPriceInBaseCurrencyBox);
    }

    @NonNull
    @Override
    public String getCurrencySelectionText() {
        return currencySpinner.getSelectedItem() == null ? getParentTrip().getDefaultCurrencyCode() : currencySpinner.getSelectedItem().toString();
    }

    @NonNull
    @UiThread
    @Override
    public Observable<CharSequence> getExchangeRateChanges() {
        return RxTextView.textChanges(exchangeRateBox);
    }

    @NonNull
    @UiThread
    @Override
    public Observable<CharSequence> getExchangedPriceInBaseCurrencyChanges() {
        return RxTextView.textChanges(exchangedPriceInBaseCurrencyBox);
    }

    @NonNull
    @Override
    public Observable<Boolean> getExchangedPriceInBaseCurrencyFocusChanges() {
        return RxView.focusChanges(exchangedPriceInBaseCurrencyBox);
    }

    @NonNull
    @UiThread
    @Override
    public Observable<Object> getUserInitiatedExchangeRateRetries() {
        return exchangeRateBox.getUserRetries()
                .doOnNext(ignored -> {
                    if (exchangedPriceInBaseCurrencyBox.isFocused()) {
                        exchangedPriceInBaseCurrencyBox.clearFocus();
                    }
                });
    }

    @Override
    public void showSamsungDecimalInputView(@NotNull String separator) {
        decimalSeparatorButton.setText(separator);
        decimalSeparatorButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideSamsungDecimalInputView() {
        decimalSeparatorButton.setVisibility(View.GONE);
    }

    @Override
    public void appendDecimalSeparatorToFocusedVied(@NotNull String separator) {
        if (focusedView instanceof EditText) {
            final EditText editor = (EditText) focusedView;
            editor.append(separator);
        }
    }

    @NotNull
    @Override
    public Observable<Unit> getClickStream() {
        return RxView.clicks(decimalSeparatorButton);
    }

    @NotNull
    @Override
    public Observable<CharSequence> getTextChangeStream(@NotNull AutoCompleteField field) {
        if (field == ReceiptAutoCompleteField.Name) {
            return RxTextView.textChanges(nameBox);
        } else if (field == ReceiptAutoCompleteField.Comment) {
            return RxTextView.textChanges(commentBox);
        } else {
            throw new IllegalArgumentException("Unsupported field type: " + field);
        }
    }

    @Override
    public void displayAutoCompleteResults(@NotNull AutoCompleteField field, @NotNull List<AutoCompleteResult<Receipt>> autoCompleteResults) {
        if (!shouldHideResults) {
            if (snackbar != null && snackbar.isShown()) {
                snackbar.dismiss();
            }
            resultsAdapter = new AutoCompleteArrayAdapter<>(requireContext(), autoCompleteResults, this);
            if (field == ReceiptAutoCompleteField.Name) {
                nameBox.setAdapter(resultsAdapter);
                if (nameBox.hasFocus()) {
                    nameBox.showDropDown();
                }
            } else if (field == ReceiptAutoCompleteField.Comment) {
                commentBox.setAdapter(resultsAdapter);
                if (commentBox.hasFocus()) {
                    commentBox.showDropDown();
                }
            } else {
                throw new IllegalArgumentException("Unsupported field type: " + field);
            }
        } else {
            shouldHideResults = false;
        }
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public Receipt getEditableItem() {
        return getArguments() != null ? getArguments().getParcelable(Receipt.PARCEL_KEY) : null;
    }

    @Override
    public void displayTitle(@NotNull String title) {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @NonNull
    @Override
    public Consumer<? super Boolean> togglePaymentMethodFieldVisibility() {
        return isVisible -> {
            for (View v : paymentMethodsViewsList) {
                v.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            }
        };
    }

    @Override
    public void displayPaymentMethods(List<PaymentMethod> list) {
        if (isAdded()) {
            paymentMethodsAdapter.update(list);
            paymentMethodsSpinner.setAdapter(paymentMethodsAdapter);
            paymentMethodsSpinner.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            final PaymentMethod paymentMethod = paymentMethodsAdapter.getItem(i);
                            reimbursableCheckbox.setChecked(paymentMethod.isReimbursable());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
            if (getEditableItem() != null) {
                // Here we manually loop through all payment methods and check for id == id in case the user changed this via "Manage"
                final PaymentMethod receiptPaymentMethod = getEditableItem().getPaymentMethod();
                for (int i = 0; i < paymentMethodsAdapter.getCount(); i++) {
                    final PaymentMethod paymentMethod = paymentMethodsAdapter.getItem(i);
                    if (paymentMethod != null && paymentMethod.getId() == receiptPaymentMethod.getId()) {
                        paymentMethodsSpinner.setSelection(i);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void fillValueField(@NotNull AutoCompleteResult<Receipt> autoCompleteResult) {
        final Receipt firstReceipt = autoCompleteResult.getFirstItem();
        shouldHideResults = true;
        if (nameBox.isPopupShowing()) {
            // Whenever we select an old item, attempt to map our price and category to the same
            // Only update the price if: no text is set AND the next item price == the first
            if (priceBox.getText().length() == 0) {
                final Receipt secondReceipt = autoCompleteResult.getSecondItem();
                if (secondReceipt != null && firstReceipt.getPrice().getDecimalFormattedPrice().equals(secondReceipt.getPrice().getDecimalFormattedPrice())) {
                    priceBox.setText(firstReceipt.getPrice().getDecimalFormattedPrice());
                }
            }

            final int categoryIndex = categoriesList.indexOf(firstReceipt.getCategory());
            if (categoryIndex > 0) {
                categoriesSpinner.setSelection(categoryIndex);
            }
            nameBox.setText(autoCompleteResult.getDisplayName());
            nameBox.setSelection(nameBox.getText().length());
            nameBox.dismissDropDown();
        } else {
            commentBox.setText(autoCompleteResult.getDisplayName());
            commentBox.setSelection(commentBox.getText().length());
            commentBox.dismissDropDown();
        }
        SoftKeyboardManager.hideKeyboard(focusedView);
    }

    @Override
    public void sendAutoCompleteHideEvent(@NotNull AutoCompleteResult<Receipt> autoCompleteResult) {
        SoftKeyboardManager.hideKeyboard(focusedView);
        if (nameBox.isPopupShowing()) {
            _hideAutoCompleteVisibilityClicks.onNext(
                    new AutoCompleteUpdateEvent(autoCompleteResult, ReceiptAutoCompleteField.Name, resultsAdapter.getPosition(autoCompleteResult)));
        } else {
            _hideAutoCompleteVisibilityClicks.onNext(
                    new AutoCompleteUpdateEvent(autoCompleteResult, ReceiptAutoCompleteField.Comment, resultsAdapter.getPosition(autoCompleteResult)));
        }
    }

    @Override
    public void removeValueFromAutoComplete(int position) {
        getActivity().runOnUiThread(() -> {
            itemToRemoveOrReAdd = resultsAdapter.getItem(position);
            resultsAdapter.remove(itemToRemoveOrReAdd);
            resultsAdapter.notifyDataSetChanged();
            View view = getActivity().findViewById(R.id.update_receipt_layout);
            snackbar = Snackbar.make(view, getString(
                    R.string.item_removed_from_auto_complete, itemToRemoveOrReAdd.getDisplayName()), Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.undo, v -> {
                if (nameBox.hasFocus()) {
                    _unHideAutoCompleteVisibilityClicks.onNext(
                            new AutoCompleteUpdateEvent(itemToRemoveOrReAdd, ReceiptAutoCompleteField.Name, position));
                } else {
                    _unHideAutoCompleteVisibilityClicks.onNext(
                            new AutoCompleteUpdateEvent(itemToRemoveOrReAdd, ReceiptAutoCompleteField.Comment, position));
                }
            });
            snackbar.show();
        });
    }

    @Override
    public void sendAutoCompleteUnHideEvent(int position) {
        getActivity().runOnUiThread(() -> {
            resultsAdapter.insert(itemToRemoveOrReAdd, position);
            resultsAdapter.notifyDataSetChanged();
            Toast.makeText(getContext(), R.string.result_restored, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void displayAutoCompleteError() {
        getActivity().runOnUiThread(() ->
                Toast.makeText(getContext(), R.string.result_restore_failed, Toast.LENGTH_LONG).show());
    }

    @NotNull
    @Override
    public Observable<AutoCompleteUpdateEvent<Receipt>> getHideAutoCompleteVisibilityClick() {
        return _hideAutoCompleteVisibilityClicks;
    }

    @NotNull
    @Override
    public Observable<AutoCompleteUpdateEvent<Receipt>> getUnHideAutoCompleteVisibilityClick() {
        return _unHideAutoCompleteVisibilityClicks;
    }

    private class SpinnerSelectionListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            if (isNewReceipt()) {
                final Category category = categoriesAdapter.getItem(position);
                if (category != null) {
                    if (presenter.isMatchReceiptNameToCategory()) {
                        nameBox.setText(category.getName());
                    }
                    if (presenter.isMatchReceiptCommentToCategory()) {
                        commentBox.setText(category.getName());
                    }
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private String getFlexString(int id) {
        return getFlexString(flex, id);
    }
}
