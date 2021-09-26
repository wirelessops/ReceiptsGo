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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBar;

import com.google.android.material.snackbar.Snackbar;
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
    public void onAttach(@NonNull Context context) {
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

        final DefaultCurrencyListEditorView defaultCurrencyListEditorView = new DefaultCurrencyListEditorView(requireContext(), () -> binding.receiptCurrency.get());
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
        flex.applyCustomSettings(binding.receiptName);
        flex.applyCustomSettings(binding.receiptPrice);
        flex.applyCustomSettings(binding.receiptTax1);
        flex.applyCustomSettings(binding.receiptTax2);
        flex.applyCustomSettings(binding.receiptCurrency.get());
        flex.applyCustomSettings(binding.receiptExchangeRate);
        flex.applyCustomSettings(binding.receiptDate);
        flex.applyCustomSettings(binding.receiptCategory.get());
        flex.applyCustomSettings(binding.receiptComment);
        flex.applyCustomSettings(binding.receiptExpensable);
        flex.applyCustomSettings(binding.receiptFullpage);

        // Apply white-label settings via our 'Flex' mechanism to add custom fields
        final LinearLayout extras = (LinearLayout) flex.getSubView(getActivity(), view, R.id.receipt_extras);
        this.extraEditText1 = extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_1));
        this.extraEditText2 = extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_2));
        this.extraEditText3 = extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_3));

        // Toolbar stuff
        if (navigationHandler.isDualPane()) {
            binding.toolbar.toolbar.setVisibility(View.GONE);
        } else {
            setSupportActionBar(binding.toolbar.toolbar);
        }

        // Set each focus listener, so we can track the focus view across resume -> pauses
        binding.receiptName.setOnFocusChangeListener(this);
        binding.receiptPrice.setOnFocusChangeListener(this);
        binding.receiptTax1.setOnFocusChangeListener(this);
        binding.receiptTax2.setOnFocusChangeListener(this);
        binding.receiptCurrency.get().setOnFocusChangeListener(this);
        binding.receiptDate.setOnFocusChangeListener(this);
        binding.receiptComment.setOnFocusChangeListener(this);
        binding.receiptPaymentMethod.get().setOnFocusChangeListener(this);
        binding.receiptExchangeRate.setOnFocusChangeListener(this);
        binding.receiptExchangedResult.setOnFocusChangeListener(this);

        // Configure our custom view properties
        binding.receiptExchangeRate.setFailedHint(R.string.DIALOG_RECEIPTMENU_HINT_EXCHANGE_RATE_FAILED);

        // And ensure that we do not show the keyboard when clicking these views
        final View.OnTouchListener hideSoftKeyboardOnTouchListener = new SoftKeyboardManager.HideSoftKeyboardOnTouchListener();
        binding.receiptDate.setOnTouchListener(hideSoftKeyboardOnTouchListener);
        binding.receiptCategory.get().setOnTouchListener(hideSoftKeyboardOnTouchListener);
        binding.receiptCurrency.get().setOnTouchListener(hideSoftKeyboardOnTouchListener);
        binding.receiptPaymentMethod.get().setOnTouchListener(hideSoftKeyboardOnTouchListener);

        // Set-up tax adapter
        if (presenter.isIncludeTaxField()) {
            binding.receiptTax1.setAdapter(new TaxAutoCompleteAdapter(getActivity(),
                    binding.receiptPrice,
                    binding.receiptTax1,
                    presenter.isUsePreTaxPrice(),
                    presenter.getDefaultTaxPercentage(),
                    isNewReceipt()));

            if (presenter.isIncludeTax2Field()) {
                binding.receiptTax2.setAdapter(new TaxAutoCompleteAdapter(getActivity(),
                        binding.receiptPrice,
                        binding.receiptTax2,
                        presenter.isUsePreTaxPrice(),
                        presenter.getDefaultTax2Percentage(),
                        isNewReceipt()));
            }
        }

        // Set custom tax names if tax2 is enabled
        if (userPreferenceManager.get(UserPreference.Receipts.IncludeTaxField) && userPreferenceManager.get(UserPreference.Receipts.IncludeTax2Field)) {
            binding.receiptTax1Wrapper.setHint(userPreferenceManager.get(UserPreference.Receipts.Tax1Name));
            binding.receiptTax2Wrapper.setHint(userPreferenceManager.get(UserPreference.Receipts.Tax2Name));
        }

        // Outline date defaults
        binding.receiptDate.setFocusableInTouchMode(false);
        binding.receiptDate.setDateFormatter(dateFormatter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Configure things if it's not a restored fragment
        if (savedInstanceState == null) {
            if (isNewReceipt()) { // new receipt

                final Time now = new Time();
                now.setToNow();

                final Date date;
                if (receiptInputCache.getCachedDate() == null) {
                    date = presenter.isReceiptDateDefaultsToReportStartDate() ? getParentTrip().getStartDate() : new Date(now.toMillis(false));
                } else {
                    date = receiptInputCache.getCachedDate();
                }
                binding.receiptDate.setDate(date);

                binding.receiptExpensable.setChecked(presenter.isReceiptsDefaultAsReimbursable());

                if (presenter.isMatchReceiptNameToCategory()) {
                    if (focusedView == null) {
                        focusedView = binding.receiptPrice;
                    }
                }

                binding.receiptFullpage.setChecked(presenter.isDefaultToFullPage());

                if (ocrResponse != null) {
                    final OcrResponseParser ocrResponseParser = new OcrResponseParser(ocrResponse);
                    if (ocrResponseParser.getMerchant() != null) {
                        binding.receiptName.setText(ocrResponseParser.getMerchant());
                    }

                    if (presenter.isIncludeTaxField() && ocrResponseParser.getTaxAmount() != null) {
                        binding.receiptTax1.setText(ocrResponseParser.getTaxAmount());
                        if (ocrResponseParser.getTotalAmount() != null) {
                            if (presenter.isUsePreTaxPrice()) {
                                // If we're in pre-tax mode, let's calculate the price as (total - tax = pre-tax-price)
                                final BigDecimal preTaxPrice = ModelUtils.tryParse(ocrResponseParser.getTotalAmount()).subtract(ModelUtils.tryParse(ocrResponseParser.getTaxAmount()));
                                binding.receiptPrice.setText(preTaxPrice.toPlainString());
                            } else {
                                binding.receiptPrice.setText(ocrResponseParser.getTotalAmount());
                            }
                        }
                    } else if (ocrResponseParser.getTotalAmount() != null) {
                        binding.receiptPrice.setText(ocrResponseParser.getTotalAmount());
                    }

                    if (ocrResponseParser.getDate() != null) {
                        binding.receiptDate.setDate(ocrResponseParser.getDate());
                    }
                }

            } else { // edit receipt
                final Receipt receipt = getEditableItem();

                binding.receiptName.setText(receipt.getName());
                binding.receiptDate.setDate(receipt.getDate());
                binding.receiptDate.setTimeZone(receipt.getTimeZone());
                binding.receiptComment.setText(receipt.getComment());

                binding.receiptExpensable.setChecked(receipt.isReimbursable());
                binding.receiptFullpage.setChecked(receipt.isFullPage());

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
                focusedView = binding.receiptName;
            }

        }

        // Configure items that require callbacks (note: Move these to presenters at some point for testing)
        categoryTableEventsListener = new StubTableEventsListener<Category>() {
            @Override
            public void onGetSuccess(@NonNull List<Category> list) {
                if (isAdded()) {
                    categoriesList = list;
                    categoriesAdapter.update(list);
                    binding.receiptCategory.get().setAdapter(categoriesAdapter);

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
                                            binding.receiptCategory.get().setSelection(i);
                                            break; // Exit loop now
                                        }
                                    }
                                }
                            } else {
                                int idx = categoriesAdapter.getPosition(receiptInputCache.getCachedCategory());
                                if (idx > 0) {
                                    binding.receiptCategory.get().setSelection(idx);
                                }
                            }
                        }
                    } else {
                        // Here we manually loop through all categories and check for id == id in case the user changed this via "Manage"
                        final Category receiptCategory = getEditableItem().getCategory();
                        for (int i = 0; i < categoriesAdapter.getCount(); i++) {
                            final Category category = categoriesAdapter.getItem(i);
                            if (category != null && category.getId() == receiptCategory.getId()) {
                                binding.receiptCategory.get().setSelection(i);
                                break;
                            }
                        }
                    }

                    if (presenter.isMatchReceiptCommentToCategory() || presenter.isMatchReceiptNameToCategory()) {
                        binding.receiptCategory.get().setOnItemSelectedListener(new SpinnerSelectionListener());
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
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_save, menu);
        menu.findItem(R.id.menu_main_search).setVisible(false);
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
        if (presenter.checkReceipt(binding.receiptDate.getDate())) {
            final String name = TextUtils.isEmpty(binding.receiptName.getText().toString()) ? "" : binding.receiptName.getText().toString();
            final Category category = categoriesAdapter.getItem(binding.receiptCategory.get().getSelectedItemPosition());
            final String currency = binding.receiptCurrency.get().getSelectedItem().toString();
            final String price = binding.receiptPrice.getText().toString();
            final String tax = binding.receiptTax1.getText().toString();
            final String tax2 = binding.receiptTax2.getText().toString();
            final String exchangeRate = binding.receiptExchangeRate.getText() != null ? binding.receiptExchangeRate.getText().toString() : "";
            final String comment = binding.receiptComment.getText().toString();
            final PaymentMethod paymentMethod = (PaymentMethod) (presenter.isUsePaymentMethods() ? binding.receiptPaymentMethod.get().getSelectedItem() : null);
            final String extraText1 = (extraEditText1 == null) ? null : extraEditText1.getText().toString();
            final String extraText2 = (extraEditText2 == null) ? null : extraEditText2.getText().toString();
            final String extraText3 = (extraEditText3 == null) ? null : extraEditText3.getText().toString();
            final TimeZone timeZone = binding.receiptDate.getTimeZone();
            final Date receiptDate;

            // updating date just if it was really changed (to prevent reordering)
            if (getEditableItem() != null && getEditableItem().getDate().equals(binding.receiptDate.getDate())) {
                receiptDate = getEditableItem().getDate();
            } else {
                receiptDate = binding.receiptDate.getDate();
            }

            receiptInputCache.setCachedDate((Date) binding.receiptDate.getDate().clone());
            receiptInputCache.setCachedCategory(category);
            receiptInputCache.setCachedCurrency(currency);

            presenter.saveReceipt(receiptDate, timeZone, price, tax, tax2, exchangeRate, comment,
                    paymentMethod, binding.receiptExpensable.isChecked(), binding.receiptFullpage.isChecked(), name, category, currency,
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
        return RxTextViewExtensions.price(binding.receiptPrice);
    }

    @NonNull
    @UiThread
    @Override
    public Consumer<? super Price> displayReceiptTax() {
        return RxTextViewExtensions.price(binding.receiptTax1);
    }

    @NonNull
    @UiThread
    @Override
    public Consumer<? super Price> displayReceiptTax2() {
        return RxTextViewExtensions.price(binding.receiptTax2);
    }

    @NonNull
    @UiThread
    @Override
    public Consumer<? super Boolean> toggleReceiptTaxFieldVisibility() {
        return (Consumer<Boolean>) isVisible -> binding.receiptTax1Wrapper.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @NonNull
    @UiThread
    @Override
    public Consumer<? super Boolean> toggleReceiptTax2FieldVisibility() {
        return RxView.visibility(binding.receiptTax2Wrapper);
    }

    @NonNull
    @UiThread
    @Override
    public Observable<CharSequence> getReceiptPriceChanges() {
        return RxTextView.textChanges(binding.receiptPrice);
    }

    @NonNull
    @UiThread
    @Override
    public Observable<CharSequence> getReceiptTaxChanges() {
        return Observable.merge(RxTextView.textChanges(binding.receiptTax1), RxTextView.textChanges(binding.receiptTax2));
    }

    @NonNull
    @UiThread
    @Override
    public Observable<Date> getReceiptDateChanges() {
        return RxDateEditText.INSTANCE.dateChanges(binding.receiptDate)
                .doOnNext(ignored -> {
                    if (binding.receiptExchangedResult.isFocused()) {
                        binding.receiptExchangedResult.clearFocus();
                    }
                });
    }

    @NonNull
    @UiThread
    @Override
    public Consumer<? super Boolean> toggleExchangeRateFieldVisibility() {
        return (Consumer<Boolean>) isVisible -> {
            final int visibility = isVisible ? View.VISIBLE : View.GONE;

            binding.receiptExchangeRateWrapper.setVisibility(visibility);
            binding.receiptExchangedWrapper.setVisibility(visibility);
            binding.receiptExchangeBaseCurrency.setVisibility(visibility);
        };
    }

    @NonNull
    @UiThread
    @Override
    public Consumer<? super UiIndicator<ExchangeRate>> displayExchangeRate() {
        return (Consumer<UiIndicator<ExchangeRate>>) exchangeRateUiIndicator -> {
            if (exchangeRateUiIndicator.getState() == UiIndicator.State.Loading) {
                binding.receiptExchangeRate.setText("");
                binding.receiptExchangeRate.setCurrentState(NetworkRequestAwareEditText.State.Loading);
            } else if (exchangeRateUiIndicator.getState() == UiIndicator.State.Error) {
                binding.receiptExchangeRate.setCurrentState(NetworkRequestAwareEditText.State.Failure);
            } else if (exchangeRateUiIndicator.getState() == UiIndicator.State.Success) {
                if (exchangeRateUiIndicator.getData().isPresent()) {
                    if (TextUtils.isEmpty(binding.receiptExchangeRate.getText()) || binding.receiptExchangedResult.isFocused()) {
                        final BigDecimal exchangeRate = exchangeRateUiIndicator.getData().get().getExchangeRate(getParentTrip().getTripCurrency());
                        final String exchangeRateString = exchangeRate != null ? exchangeRate.setScale(ExchangeRate.PRECISION, RoundingMode.HALF_UP).toPlainString() : "";
                        binding.receiptExchangeRate.setText(exchangeRateString);
                    } else {
                        Logger.warn(ReceiptCreateEditFragment.this, "Ignoring remote exchange rate result now that one is already set");
                    }
                    binding.receiptExchangeRate.setCurrentState(NetworkRequestAwareEditText.State.Success);
                } else {
                    // If the data is empty, reset to use the ready state to allow for user interaction
                    binding.receiptExchangeRate.setText("");
                }
            } else {
                binding.receiptExchangeRate.setCurrentState(NetworkRequestAwareEditText.State.Ready);
            }
        };
    }

    @NonNull
    @UiThread
    @Override
    public Consumer<? super CurrencyUnit> displayBaseCurrency() {
        return (Consumer<CurrencyUnit>) priceCurrency ->
                binding.receiptExchangeBaseCurrency.setText(priceCurrency.getCode());
    }

    @NonNull
    @UiThread
    @Override
    public Consumer<? super Optional<Price>> displayExchangedPriceInBaseCurrency() {
        return RxTextViewExtensions.priceOptional(binding.receiptExchangedResult);
    }

    @NonNull
    @Override
    public String getCurrencySelectionText() {
        return binding.receiptCurrency.get().getSelectedItem() == null ? getParentTrip().getDefaultCurrencyCode() : binding.receiptCurrency.get().getSelectedItem().toString();
    }

    @NonNull
    @UiThread
    @Override
    public Observable<CharSequence> getExchangeRateChanges() {
        return RxTextView.textChanges(binding.receiptExchangeRate);
    }

    @NonNull
    @UiThread
    @Override
    public Observable<CharSequence> getExchangedPriceInBaseCurrencyChanges() {
        return RxTextView.textChanges(binding.receiptExchangedResult);
    }

    @NonNull
    @Override
    public Observable<Boolean> getExchangedPriceInBaseCurrencyFocusChanges() {
        return RxView.focusChanges(binding.receiptExchangedResult);
    }

    @NonNull
    @UiThread
    @Override
    public Observable<Object> getUserInitiatedExchangeRateRetries() {
        return binding.receiptExchangeRate.getUserRetries()
                .doOnNext(ignored -> {
                    if (binding.receiptExchangedResult.isFocused()) {
                        binding.receiptExchangedResult.clearFocus();
                    }
                });
    }

    @Override
    public void showSamsungDecimalInputView(@NotNull String separator) {
        binding.decimalSeparatorButton.setText(separator);
        binding.decimalSeparatorButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideSamsungDecimalInputView() {
        binding.decimalSeparatorButton.setVisibility(View.GONE);
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
        return RxView.clicks(binding.decimalSeparatorButton);
    }

    @NotNull
    @Override
    public Observable<CharSequence> getTextChangeStream(@NotNull AutoCompleteField field) {
        if (field == ReceiptAutoCompleteField.Name) {
            return RxTextView.textChanges(binding.receiptName);
        } else if (field == ReceiptAutoCompleteField.Comment) {
            return RxTextView.textChanges(binding.receiptComment);
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
                binding.receiptName.setAdapter(resultsAdapter);
                if (binding.receiptName.hasFocus()) {
                    binding.receiptName.showDropDown();
                }
            } else if (field == ReceiptAutoCompleteField.Comment) {
                binding.receiptComment.setAdapter(resultsAdapter);
                if (binding.receiptComment.hasFocus()) {
                    binding.receiptComment.showDropDown();
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
        return isVisible -> binding.receiptPaymentMethod.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void displayPaymentMethods(List<PaymentMethod> list) {
        if (isAdded()) {
            paymentMethodsAdapter.update(list);
            binding.receiptPaymentMethod.get().setAdapter(paymentMethodsAdapter);
            binding.receiptPaymentMethod.get().setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            final PaymentMethod paymentMethod = paymentMethodsAdapter.getItem(i);
                            binding.receiptExpensable.setChecked(paymentMethod.isReimbursable());
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
                        binding.receiptPaymentMethod.get().setSelection(i);
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
        if (binding.receiptName.isPopupShowing()) {
            // Whenever we select an old item, attempt to map our price and category to the same
            // Only update the price if: no text is set AND the next item price == the first
            if (binding.receiptPrice.getText().length() == 0) {
                final Receipt secondReceipt = autoCompleteResult.getSecondItem();
                if (secondReceipt != null && firstReceipt.getPrice().equals(secondReceipt.getPrice())) {
                    binding.receiptPrice.setText(firstReceipt.getPrice().getPrice().toPlainString());
                }
            }

            final int categoryIndex = categoriesList.indexOf(firstReceipt.getCategory());
            if (categoryIndex > 0) {
                binding.receiptCategory.get().setSelection(categoryIndex);
            }
            binding.receiptName.setText(autoCompleteResult.getDisplayName());
            binding.receiptName.setSelection(binding.receiptName.getText().length());
            binding.receiptName.dismissDropDown();
        } else {
            binding.receiptComment.setText(autoCompleteResult.getDisplayName());
            binding.receiptComment.setSelection(binding.receiptComment.getText().length());
            binding.receiptComment.dismissDropDown();
        }
        SoftKeyboardManager.hideKeyboard(focusedView);
    }

    @Override
    public void sendAutoCompleteHideEvent(@NotNull AutoCompleteResult<Receipt> autoCompleteResult) {
        SoftKeyboardManager.hideKeyboard(focusedView);
        if (binding.receiptName.isPopupShowing()) {
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
            if (position >= 0 && position < resultsAdapter.getCount()) {
                itemToRemoveOrReAdd = resultsAdapter.getItem(position);
                resultsAdapter.remove(itemToRemoveOrReAdd);
                resultsAdapter.notifyDataSetChanged();
                View view = getActivity().findViewById(R.id.update_receipt_layout);
                snackbar = Snackbar.make(view, getString(
                        R.string.item_removed_from_auto_complete, itemToRemoveOrReAdd.getDisplayName()), Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.undo, v -> {
                    if (binding.receiptName.hasFocus()) {
                        _unHideAutoCompleteVisibilityClicks.onNext(
                                new AutoCompleteUpdateEvent(itemToRemoveOrReAdd, ReceiptAutoCompleteField.Name, position));
                    } else {
                        _unHideAutoCompleteVisibilityClicks.onNext(
                                new AutoCompleteUpdateEvent(itemToRemoveOrReAdd, ReceiptAutoCompleteField.Comment, position));
                    }
                });
                snackbar.show();
            }
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
                        binding.receiptName.setText(category.getName());
                    }
                    if (presenter.isMatchReceiptCommentToCategory()) {
                        binding.receiptComment.setText(category.getName());
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
