package co.smartreceipts.android.receipts.editor;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
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

import com.hadisatrio.optional.Optional;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxDateEditText;
import com.jakewharton.rxbinding2.widget.RxTextView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.adapters.FooterButtonArrayAdapter;
import co.smartreceipts.android.adapters.TaxAutoCompleteAdapter;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.autocomplete.AutoCompleteArrayAdapter;
import co.smartreceipts.android.autocomplete.AutoCompleteField;
import co.smartreceipts.android.autocomplete.AutoCompletePresenter;
import co.smartreceipts.android.autocomplete.AutoCompleteResult;
import co.smartreceipts.android.autocomplete.AutoCompleteView;
import co.smartreceipts.android.autocomplete.receipt.ReceiptAutoCompleteField;
import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.currency.widget.CurrencyListEditorPresenter;
import co.smartreceipts.android.currency.widget.DefaultCurrencyListEditorView;
import co.smartreceipts.android.date.DateEditText;
import co.smartreceipts.android.editor.Editor;
import co.smartreceipts.android.fragments.ChildFragmentNavigationHandler;
import co.smartreceipts.android.fragments.ReceiptInputCache;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.keyboard.decimal.SamsungDecimalInputPresenter;
import co.smartreceipts.android.keyboard.decimal.SamsungDecimalInputView;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.ocr.apis.model.OcrResponse;
import co.smartreceipts.android.ocr.util.OcrResponseParser;
import co.smartreceipts.android.ocr.widget.tooltip.OcrInformationalTooltipFragment;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.impl.CategoriesTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.PaymentMethodsTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener;
import co.smartreceipts.android.receipts.editor.currency.ReceiptCurrencyCodeSupplier;
import co.smartreceipts.android.receipts.editor.date.ReceiptDateView;
import co.smartreceipts.android.receipts.editor.exchange.CurrencyExchangeRateEditorPresenter;
import co.smartreceipts.android.receipts.editor.exchange.CurrencyExchangeRateEditorView;
import co.smartreceipts.android.receipts.editor.exchange.ExchangeRateServiceManager;
import co.smartreceipts.android.receipts.editor.pricing.EditableReceiptPricingView;
import co.smartreceipts.android.receipts.editor.pricing.ReceiptPricingPresenter;
import co.smartreceipts.android.receipts.editor.toolbar.ReceiptsEditorToolbarPresenter;
import co.smartreceipts.android.receipts.editor.toolbar.ReceiptsEditorToolbarView;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.SoftKeyboardManager;
import co.smartreceipts.android.utils.butterknife.ButterKnifeActions;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.NetworkRequestAwareEditText;
import co.smartreceipts.android.widget.model.UiIndicator;
import co.smartreceipts.android.widget.rxbinding2.RxTextViewExtensions;
import co.smartreceipts.android.widget.tooltip.report.backup.data.BackupReminderTooltipStorage;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import wb.android.flex.Flex;

import static java.util.Collections.emptyList;

public class ReceiptCreateEditFragment extends WBFragment implements Editor<Receipt>,
        View.OnFocusChangeListener,
        EditableReceiptPricingView,
        ReceiptDateView,
        CurrencyExchangeRateEditorView,
        SamsungDecimalInputView,
        AutoCompleteView<Receipt>,
        ReceiptsEditorToolbarView {

    public static final String ARG_FILE = "arg_file";
    public static final String ARG_OCR = "arg_ocr";

    @Inject
    Flex flex;

    @Inject
    DatabaseHelper database;

    @Inject
    ExchangeRateServiceManager exchangeRateServiceManager;

    @Inject
    Analytics analytics;

    @Inject
    CategoriesTableController categoriesTableController;

    @Inject
    PaymentMethodsTableController paymentMethodsTableController;

    @Inject
    NavigationHandler navigationHandler;

    @Inject
    BackupReminderTooltipStorage backupReminderTooltipStorage;

    @Inject
    UserPreferenceManager userPreferenceManager;

    @Inject
    ReceiptCreateEditFragmentPresenter presenter;

    @Inject
    SamsungDecimalInputPresenter samsungDecimalInputPresenter;

    @Inject
    AutoCompletePresenter<Receipt> autoCompletePresenter;

    @Inject
    ReceiptsEditorToolbarPresenter receiptsEditorToolbarPresenter;

    // Butterknife Fields
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.DIALOG_RECEIPTMENU_NAME)
    AutoCompleteTextView nameBox;

    @BindView(R.id.DIALOG_RECEIPTMENU_PRICE)
    EditText priceBox;

    @BindView(R.id.DIALOG_RECEIPTMENU_TAX)
    AutoCompleteTextView taxBox;

    @BindView(R.id.DIALOG_RECEIPTMENU_CURRENCY)
    Spinner currencySpinner;

    @BindView(R.id.receipt_input_exchange_rate)
    NetworkRequestAwareEditText exchangeRateBox;

    @BindView(R.id.receipt_input_exchanged_result)
    EditText exchangedPriceInBaseCurrencyBox;

    @BindView(R.id.receipt_input_exchange_rate_base_currency)
    TextView receiptInputExchangeRateBaseCurrencyTextView;

    @BindView(R.id.DIALOG_RECEIPTMENU_DATE)
    DateEditText dateBox;

    @BindView(R.id.DIALOG_RECEIPTMENU_CATEGORY)
    Spinner categoriesSpinner;

    @BindView(R.id.DIALOG_RECEIPTMENU_COMMENT)
    AutoCompleteTextView commentBox;

    @BindView(R.id.receipt_input_payment_method)
    Spinner paymentMethodsSpinner;

    @BindView(R.id.DIALOG_RECEIPTMENU_EXPENSABLE)
    CheckBox reimbursableCheckbox;

    @BindView(R.id.DIALOG_RECEIPTMENU_FULLPAGE)
    CheckBox fullpageCheckbox;

    @BindView(R.id.decimalSeparatorButton)
    Button decimalSeparatorButton;

    @BindView(R.id.receipt_input_tax_wrapper)
    View taxInputWrapper;

    @BindViews({R.id.receipt_input_guide_image_payment_method, R.id.receipt_input_payment_method})
    List<View> paymentMethodsViewsList;

    @BindViews({R.id.receipt_input_guide_image_exchange_rate, R.id.receipt_input_exchange_rate, R.id.receipt_input_exchanged_result, R.id.receipt_input_exchange_rate_base_currency})
    List<View> exchangeRateViewsList;

    // Flex fields (ie for white-label projects)
    EditText extraEditText1;
    EditText extraEditText2;
    EditText extraEditText3;

    // Misc views
    View focusedView;

    // Butterknife unbinding
    private Unbinder unbinder;

    // Metadata
    private OcrResponse ocrResponse;

    // Presenters
    private CurrencyListEditorPresenter currencyListEditorPresenter;
    private ReceiptPricingPresenter receiptPricingPresenter;
    private CurrencyExchangeRateEditorPresenter currencyExchangeRateEditorPresenter;

    // Database monitor callbacks
    private TableEventsListener<Category> categoryTableEventsListener;
    private TableEventsListener<PaymentMethod> paymentMethodTableEventsListener;

    // Misc
    private ReceiptInputCache receiptInputCache;
    private List<Category> categoriesList;
    private FooterButtonArrayAdapter<Category> categoriesAdapter;
    private FooterButtonArrayAdapter<PaymentMethod> paymentMethodsAdapter;

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
        categoriesAdapter = new FooterButtonArrayAdapter<>(requireActivity(), new ArrayList<Category>(),
                R.string.manage_categories, v -> {
            analytics.record(Events.Informational.ClickedManageCategories);
            navigationHandler.navigateToCategoriesEditor();
        });
        paymentMethodsAdapter = new FooterButtonArrayAdapter<>(requireActivity(), new ArrayList<PaymentMethod>(),
                R.string.manage_payment_methods, v -> {
            analytics.record(Events.Informational.ClickedManagePaymentMethods);
            navigationHandler.navigateToPaymentMethodsEditor();
        });

        setHasOptionsMenu(true);

        final DefaultCurrencyListEditorView defaultCurrencyListEditorView = new DefaultCurrencyListEditorView(requireContext(), () -> currencySpinner);
        final ReceiptCurrencyCodeSupplier currencyCodeSupplier = new ReceiptCurrencyCodeSupplier(getParentTrip(), receiptInputCache, getEditableItem());
        currencyListEditorPresenter = new CurrencyListEditorPresenter(defaultCurrencyListEditorView, database, currencyCodeSupplier, savedInstanceState);
        receiptPricingPresenter = new ReceiptPricingPresenter(this, userPreferenceManager, getEditableItem(), savedInstanceState);
        currencyExchangeRateEditorPresenter = new CurrencyExchangeRateEditorPresenter(this, this, defaultCurrencyListEditorView, this, exchangeRateServiceManager, database, getParentTrip(), getEditableItem(), savedInstanceState);
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
        return inflater.inflate(R.layout.update_receipt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.unbinder = ButterKnife.bind(this, view);

        if (savedInstanceState == null) {
            if (isNewReceipt()) {
                new ChildFragmentNavigationHandler(this).addChild(new OcrInformationalTooltipFragment(), R.id.update_receipt_tooltip);
            }
        }

        // Apply white-label settings via our 'Flex' mechanism to update defaults
        flex.applyCustomSettings(nameBox);
        flex.applyCustomSettings(priceBox);
        flex.applyCustomSettings(taxBox);
        flex.applyCustomSettings(currencySpinner);
        flex.applyCustomSettings(exchangeRateBox);
        flex.applyCustomSettings(dateBox);
        flex.applyCustomSettings(categoriesSpinner);
        flex.applyCustomSettings(commentBox);
        flex.applyCustomSettings(reimbursableCheckbox);
        flex.applyCustomSettings(fullpageCheckbox);

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
        this.taxBox.setOnFocusChangeListener(this);
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
            taxBox.setAdapter(new TaxAutoCompleteAdapter(getActivity(),
                    priceBox,
                    taxBox,
                    presenter.isUsePreTaxPrice(),
                    presenter.getDefaultTaxPercentage(),
                    isNewReceipt()));
        }

        // Outline date defaults
        dateBox.setFocusableInTouchMode(false);
        dateBox.setDateSeparator(userPreferenceManager.get(UserPreference.General.DateSeparator));
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

                fullpageCheckbox.setChecked(presenter.isDefaultToFullPage());

                if (ocrResponse != null) {
                    final OcrResponseParser ocrResponseParser = new OcrResponseParser(ocrResponse);
                    if (ocrResponseParser.getMerchant() != null) {
                        nameBox.setText(ocrResponseParser.getMerchant());
                    }

                    if (presenter.isIncludeTaxField() && ocrResponseParser.getTaxAmount() != null) {
                        taxBox.setText(ocrResponseParser.getTaxAmount());
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
                fullpageCheckbox.setChecked(receipt.isFullPage());

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

        // Configure items that require callbacks (note: Moves these to presenters at some point for testing)
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

        paymentMethodTableEventsListener = new StubTableEventsListener<PaymentMethod>() {
            @Override
            public void onGetSuccess(@NonNull List<PaymentMethod> list) {
                if (isAdded()) {
                    List<PaymentMethod> paymentMethods = new ArrayList<>(list);
                    paymentMethods.add(PaymentMethod.Companion.getNONE());

                    paymentMethodsAdapter.update(paymentMethods);
                    paymentMethodsSpinner.setAdapter(paymentMethodsAdapter);
                    if (presenter.isUsePaymentMethods()) {
                        ButterKnife.apply(paymentMethodsViewsList, ButterKnifeActions.setVisibility(View.VISIBLE));
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
                    } else {
                        ButterKnife.apply(paymentMethodsViewsList, ButterKnifeActions.setVisibility(View.GONE));
                    }
                }
            }
        };
        categoriesTableController.subscribe(categoryTableEventsListener);
        paymentMethodsTableController.subscribe(paymentMethodTableEventsListener);
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

        // Attempt to update our lists in case they were changed in the background
        categoriesTableController.get();
        paymentMethodsTableController.get();

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_cancel);
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
            navigationHandler.navigateBack();
            presenter.deleteReceiptFileIfUnused();
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
        receiptsEditorToolbarPresenter.unsubscribe();
        receiptPricingPresenter.unsubscribe();
        currencyListEditorPresenter.unsubscribe();
        currencyExchangeRateEditorPresenter.unsubscribe();
        autoCompletePresenter.unsubscribe();
        samsungDecimalInputPresenter.unsubscribe();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Logger.debug(this, "onDestroyView");
        extraEditText1 = null;
        extraEditText2 = null;
        extraEditText3 = null;
        focusedView = null;
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        categoriesTableController.unsubscribe(categoryTableEventsListener);
        paymentMethodsTableController.unsubscribe(paymentMethodTableEventsListener);
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
            final String tax = taxBox.getText().toString();
            final String exchangeRate = exchangeRateBox.getText().toString();
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

            presenter.saveReceipt(receiptDate, timeZone, price, tax, exchangeRate, comment,
                    paymentMethod, reimbursableCheckbox.isChecked(), fullpageCheckbox.isChecked(), name, category, currency,
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
        return RxTextViewExtensions.price(taxBox);
    }

    @NonNull
    @UiThread
    @Override
    public Consumer<? super Boolean> toggleReceiptTaxFieldVisibility() {
        return RxView.visibility(taxInputWrapper);
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
        return RxTextView.textChanges(taxBox);
    }

    @NonNull
    @UiThread
    @Override
    public Observable<Date> getReceiptDateChanges() {
        return RxDateEditText.dateChanges(dateBox)
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
            ButterKnife.apply(exchangeRateViewsList, ButterKnifeActions.setVisibility(isVisible ? View.VISIBLE : View.GONE));
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
                        exchangeRateBox.setText(exchangeRateUiIndicator.getData().get().getDecimalFormattedExchangeRate(getParentTrip().getDefaultCurrencyCode()));
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
    public Consumer<? super PriceCurrency> displayBaseCurrency() {
        return (Consumer<PriceCurrency>) priceCurrency -> receiptInputExchangeRateBaseCurrencyTextView.setText(priceCurrency.getCurrencyCode());
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
    public Observable<Object> getClickStream() {
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
        final AutoCompleteArrayAdapter<Receipt> resultsAdapter = new AutoCompleteArrayAdapter<>(requireContext(), autoCompleteResults);
        if (field == ReceiptAutoCompleteField.Name) {
            nameBox.setAdapter(resultsAdapter);
            nameBox.showDropDown();
            nameBox.setOnItemClickListener((parent, view, position, id) -> {
                final Object selectedItem = parent.getAdapter().getItem(position);
                // Whenever we select an old item, attempt to map our price and category to the same
                if (selectedItem instanceof AutoCompleteResult) {
                    //noinspection unchecked
                    final AutoCompleteResult<Receipt> selectedAutoCompleteResult = (AutoCompleteResult<Receipt>) selectedItem;
                    final Receipt receipt = selectedAutoCompleteResult.getItem();
                    if (priceBox.getText().length() == 0) {
                        priceBox.setText(receipt.getPrice().getDecimalFormattedPrice());
                    }

                    final int categoryIndex = categoriesList.indexOf(receipt.getCategory());
                    if (categoryIndex > 0) {
                        categoriesSpinner.setSelection(categoryIndex);
                    }
                }
            });
        } else if (field == ReceiptAutoCompleteField.Comment) {
            commentBox.setAdapter(resultsAdapter);
            commentBox.showDropDown();
        } else {
            throw new IllegalArgumentException("Unsupported field type: " + field);
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
