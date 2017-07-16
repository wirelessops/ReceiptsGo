package co.smartreceipts.android.receipts.editor;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.text.method.TextKeyListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.adapters.TaxAutoCompleteAdapter;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.apis.ExchangeRateServiceManager;
import co.smartreceipts.android.apis.MemoryLeakSafeCallback;
import co.smartreceipts.android.date.DateEditText;
import co.smartreceipts.android.date.DateManager;
import co.smartreceipts.android.fragments.ChildFragmentNavigationHandler;
import co.smartreceipts.android.fragments.ReceiptInputCache;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.impl.ImmutablePaymentMethodImpl;
import co.smartreceipts.android.ocr.apis.model.OcrResponse;
import co.smartreceipts.android.ocr.util.OcrResponseParser;
import co.smartreceipts.android.ocr.widget.tooltip.OcrInformationalTooltipFragment;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.impl.CategoriesTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.PaymentMethodsTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener;
import co.smartreceipts.android.utils.SoftKeyboardManager;
import co.smartreceipts.android.utils.cache.FragmentStateCache;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.NetworkRequestAwareEditText;
import co.smartreceipts.android.widget.UserSelectionTrackingOnItemSelectedListener;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;
import wb.android.autocomplete.AutoCompleteAdapter;
import wb.android.flex.Flex;

public class ReceiptCreateEditFragment extends WBFragment implements View.OnFocusChangeListener, NetworkRequestAwareEditText.RetryListener, DatabaseHelper.ReceiptAutoCompleteListener {

    public static final String ARG_FILE = "arg_file";
    public static final String ARG_OCR = "arg_ocr";
    private static final String KEY_OUT_STATE_IS_EXCHANGE_RATE_VISIBLE = "key_is_exchange_rate_visible";

    @Inject
    Flex flex;
    @Inject
    DateManager dateManager;
    @Inject
    DatabaseHelper database;
    @Inject
    Analytics analytics;
    @Inject
    CategoriesTableController categoriesTableController;
    @Inject
    PaymentMethodsTableController paymentMethodsTableController;
    @Inject
    NavigationHandler navigationHandler;
    @Inject
    FragmentStateCache fragmentStateCache;


    @Inject
    ReceiptCreateEditFragmentPresenter presenter;

    // Metadata
    private OcrResponse ocrResponse;

    // Views
    private AutoCompleteTextView nameBox;
    private EditText priceBox;
    private AutoCompleteTextView taxBox;
    private Spinner currencySpinner;
    private NetworkRequestAwareEditText exchangeRateBox;
    private DateEditText dateBox;
    private AutoCompleteTextView commentBox;
    private Spinner categoriesSpinner;
    private CheckBox reimbursable;
    private CheckBox fullpage;
    private Spinner paymentMethodsSpinner;
    private EditText extra_edittext_box_1;
    private EditText extra_edittext_box_2;
    private EditText extra_edittext_box_3;
    private ViewGroup paymentMethodsContainer;
    private ViewGroup exchangeRateContainer;
    private Toolbar toolbar;
    private View focusedView;

    // Rx
    private Disposable idDisposable;
    private TableEventsListener<Category> categoryTableEventsListener;
    private TableEventsListener<PaymentMethod> paymentMethodTableEventsListener;

    // Misc
    private MemoryLeakSafeCallback<ExchangeRate, EditText> lastExchangeRateFetchCallback;
    private ExchangeRateServiceManager exchangeRateServiceManager;
    private ReceiptInputCache receiptInputCache;
    private AutoCompleteAdapter receiptsNameAutoCompleteAdapter, receiptsCommentAutoCompleteAdapter;
    private ArrayAdapter<CharSequence> currenciesAdapter;
    private List<Category> categoriesList;
    private ArrayAdapter<Category> categoriesAdpater;
    private ArrayAdapter<PaymentMethod> paymentMethodsAdapter;

    public static ReceiptCreateEditFragment newInstance() {
        return new ReceiptCreateEditFragment();
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.debug(this, "onCreate");
        ocrResponse = (OcrResponse) getArguments().getSerializable(ARG_OCR);
        receiptInputCache = new ReceiptInputCache(getFragmentManager());
        exchangeRateServiceManager = new ExchangeRateServiceManager(getFragmentManager());
        currenciesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
                database.getCurrenciesList());
        categoriesList = Collections.emptyList();
        categoriesAdpater = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, Collections.<Category>emptyList());
        paymentMethodsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, Collections.<PaymentMethod>emptyList());
        setHasOptionsMenu(true);
    }

    Trip getParentTrip() {
        return getArguments().getParcelable(Trip.PARCEL_KEY);
    }

    Receipt getReceipt() {
        return getArguments().getParcelable(Receipt.PARCEL_KEY);
    }

    File getFile() {
        return (File) getArguments().getSerializable(ARG_FILE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.update_receipt, container, false);
    }

    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        if (savedInstanceState == null) {
            if (getReceipt() == null) {
                new ChildFragmentNavigationHandler(this).addChild(new OcrInformationalTooltipFragment(), R.id.update_receipt_tooltip);
            }
        }

        this.nameBox = (AutoCompleteTextView) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_NAME);
        this.priceBox = (EditText) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_PRICE);
        this.taxBox = (AutoCompleteTextView) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_TAX);
        this.currencySpinner = (Spinner) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_CURRENCY);
        this.exchangeRateBox = (NetworkRequestAwareEditText) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_EXCHANGE_RATE);
        exchangeRateContainer = (ViewGroup) flex.getSubView(getActivity(), rootView, R.id.exchange_rate_container);
        this.dateBox = (DateEditText) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_DATE);
        this.commentBox = (AutoCompleteTextView) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_COMMENT);
        this.categoriesSpinner = (Spinner) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_CATEGORY);
        this.reimbursable = (CheckBox) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_EXPENSABLE);
        this.fullpage = (CheckBox) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_FULLPAGE);
        this.paymentMethodsSpinner = (Spinner) flex.getSubView(getActivity(), rootView, R.id.dialog_receiptmenu_payment_methods_spinner);
        paymentMethodsContainer = (ViewGroup) flex.getSubView(getActivity(), rootView, R.id.payment_methods_container);

        // Extras
        final LinearLayout extras = (LinearLayout) flex.getSubView(getActivity(), rootView, R.id.DIALOG_RECEIPTMENU_EXTRAS);
        this.extra_edittext_box_1 = (EditText) extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_1));
        this.extra_edittext_box_2 = (EditText) extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_2));
        this.extra_edittext_box_3 = (EditText) extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_3));

        // Toolbar stuff
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
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

        // Custom view properties
        exchangeRateBox.setFailedHint(R.string.DIALOG_RECEIPTMENU_HINT_EXCHANGE_RATE_FAILED);

        // Set click listeners
        View.OnTouchListener hideSoftKeyboardOnTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    SoftKeyboardManager.hideKeyboard(view);
                }
                return false;
            }
        };
        dateBox.setOnTouchListener(hideSoftKeyboardOnTouchListener);
        categoriesSpinner.setOnTouchListener(hideSoftKeyboardOnTouchListener);
        currencySpinner.setOnTouchListener(hideSoftKeyboardOnTouchListener);
        paymentMethodsSpinner.setOnTouchListener(hideSoftKeyboardOnTouchListener);

        // Show default dictionary with auto-complete
        nameBox.setKeyListener(TextKeyListener.getInstance(true, TextKeyListener.Capitalize.SENTENCES));

        // Set-up tax layers
        if (presenter.isIncludeTaxField()) {
            priceBox.setHint(getFlexString(R.string.DIALOG_RECEIPTMENU_HINT_PRICE_SHORT));
            taxBox.setVisibility(View.VISIBLE);
        }

        // Configure dropdown defaults for currencies
        currenciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currenciesAdapter);

        // And the exchange rate processing for our currencies
        final boolean exchangeRateIsVisible = savedInstanceState != null && savedInstanceState.getBoolean(KEY_OUT_STATE_IS_EXCHANGE_RATE_VISIBLE);
        if (exchangeRateIsVisible) {
            // Note: the restoration of selected spinner items (in the currency spinner) is delayed so we use this state tracker to restore immediately
            exchangeRateContainer.setVisibility(View.VISIBLE);
        }
        currencySpinner.setOnItemSelectedListener(new UserSelectionTrackingOnItemSelectedListener() {

            @Override
            public void onUserSelectedNewItem(AdapterView<?> parent, View view, int position, long id, int previousPosition) {
                // Then determine if we should show/hide the box
                final String baseCurrencyCode = currenciesAdapter.getItem(position).toString();
                configureExchangeRateField(baseCurrencyCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Intentional no-op
            }
        });

        // Outline date defaults
        dateBox.setFocusableInTouchMode(false);
        dateBox.setOnClickListener(dateManager.getDateEditTextListener());

        // Lastly, preset adapters for "new" receipts
        if (getReceipt() == null) {
            if (presenter.isIncludeTaxField()) {
                taxBox.setAdapter(new TaxAutoCompleteAdapter(getActivity(), priceBox, taxBox,
                        presenter.isUsePreTaxPrice(),
                        presenter.getDefaultTaxPercentage()));
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Configure things if it's not a restored fragment
        if (savedInstanceState == null) {
            if (getReceipt() == null) { // new receipt

                final Time now = new Time();
                now.setToNow();
                if (receiptInputCache.getCachedDate() == null) {
                    if (presenter.isReceiptDateDefaultsToReportStartDate()) {
                        dateBox.date = getParentTrip().getStartDate();
                    } else {
                        dateBox.date = new Date(now.toMillis(false));
                    }
                } else {
                    dateBox.date = receiptInputCache.getCachedDate();
                }
                dateBox.setText(DateFormat.getDateFormat(getActivity()).format(dateBox.date));

                reimbursable.setChecked(presenter.isReceiptsDefaultAsReimbursable());
                if (presenter.isMatchReceiptCommentToCategory() && presenter.isMatchReceiptNameToCategory()) {
                    if (focusedView == null) {
                        focusedView = priceBox;
                    }
                } else if (presenter.isMatchReceiptNameToCategory()) {
                    if (focusedView == null) {
                        focusedView = priceBox;
                    }
                }

                final Trip parentTrip = getParentTrip();

                int idx = currenciesAdapter.getPosition((parentTrip != null) ?
                        parentTrip.getDefaultCurrencyCode() : presenter.getDefaultCurrency());
                int cachedIdx = (receiptInputCache.getCachedCurrency() != null) ?
                        currenciesAdapter.getPosition(receiptInputCache.getCachedCurrency()) : -1;
                idx = (cachedIdx >= 0) ? cachedIdx : idx;
                if (idx >= 0) {
                    currencySpinner.setSelection(idx);
                }
                if (!parentTrip.getDefaultCurrencyCode().equals(receiptInputCache.getCachedCurrency())) {
                    configureExchangeRateField(receiptInputCache.getCachedCurrency());
                }
                fullpage.setChecked(presenter.isDefaultToFullPage());

                if (ocrResponse != null) {
                    final OcrResponseParser ocrResponseParser = new OcrResponseParser(ocrResponse);
                    if (ocrResponseParser.getMerchant() != null) {
                        nameBox.setText(ocrResponseParser.getMerchant());
                    }
                    if (ocrResponseParser.getTotalAmount() != null) {
                        priceBox.setText(ocrResponseParser.getTotalAmount());
                    }
                    if (ocrResponseParser.getTaxAmount() != null) {
                        taxBox.setText(ocrResponseParser.getTaxAmount());
                    }
                    if (ocrResponseParser.getDate() != null) {
                        dateBox.date = ocrResponseParser.getDate();
                        dateBox.setText(DateFormat.getDateFormat(getActivity()).format(dateBox.date));
                    }
                }

            } else { // edit receipt
                final Receipt receipt = getReceipt();
                final Trip parentTrip = getParentTrip();

                nameBox.setText(receipt.getName());
                priceBox.setText(receipt.getPrice().getDecimalFormattedPrice());
                dateBox.setText(receipt.getFormattedDate(getActivity(),
                        presenter.getDateSeparator()));
                dateBox.date = receipt.getDate();
                commentBox.setText(receipt.getComment());
                taxBox.setText(receipt.getTax().getDecimalFormattedPrice());

                final ExchangeRate exchangeRate = receipt.getPrice().getExchangeRate();
                if (exchangeRate.supportsExchangeRateFor(parentTrip.getDefaultCurrencyCode())) {
                    exchangeRateBox.setText(exchangeRate.getDecimalFormattedExchangeRate(parentTrip.getDefaultCurrencyCode()));
                }

                int idx = currenciesAdapter.getPosition(receipt.getPrice().getCurrencyCode());
                if (idx > 0) {
                    currencySpinner.setSelection(idx);
                }

                if (receipt.getPrice().getCurrency().equals(parentTrip.getPrice().getCurrency())) {
                    exchangeRateContainer.setVisibility(View.GONE);
                } else {
                    exchangeRateContainer.setVisibility(View.VISIBLE);
                }

                reimbursable.setChecked(receipt.isReimbursable());
                fullpage.setChecked(receipt.isFullPage());

                if (extra_edittext_box_1 != null && receipt.hasExtraEditText1()) {
                    extra_edittext_box_1.setText(receipt.getExtraEditText1());
                }
                if (extra_edittext_box_2 != null && receipt.hasExtraEditText2()) {
                    extra_edittext_box_2.setText(receipt.getExtraEditText2());
                }
                if (extra_edittext_box_3 != null && receipt.hasExtraEditText3()) {
                    extra_edittext_box_3.setText(receipt.getExtraEditText3());
                }
            }

            // Focused View
            if (focusedView == null) {
                focusedView = nameBox;
            }

        }

        // Configure items that require callbacks
        categoryTableEventsListener = new StubTableEventsListener<Category>() {
            @Override
            public void onGetSuccess(@NonNull List<Category> list) {
                if (isAdded()) {
                    categoriesList = list;
                    categoriesAdpater = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
                    categoriesAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    categoriesSpinner.setAdapter(categoriesAdpater);

                    if (getReceipt() == null) { // new receipt
                        if (presenter.isMatchReceiptCommentToCategory() || presenter.isMatchReceiptNameToCategory()) {
                            categoriesSpinner.setOnItemSelectedListener(new SpinnerSelectionListener());
                        }

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
                                    for (int i = 0; i < categoriesAdpater.getCount(); i++) {
                                        if (nameToIndex.equals(categoriesAdpater.getItem(i).getName())) {
                                            categoriesSpinner.setSelection(i);
                                            break; // Exit loop now
                                        }
                                    }
                                }
                            } else {
                                int idx = categoriesAdpater.getPosition(receiptInputCache.getCachedCategory());
                                if (idx > 0) {
                                    categoriesSpinner.setSelection(idx);
                                }
                            }
                        }
                    } else {
                        categoriesSpinner.setSelection(categoriesAdpater.getPosition(getReceipt().getCategory()));
                    }
                }
            }
        };
        paymentMethodTableEventsListener = new StubTableEventsListener<PaymentMethod>() {
            @Override
            public void onGetSuccess(@NonNull List<PaymentMethod> list) {
                if (isAdded()) {
                    List<PaymentMethod> paymentMethods = new ArrayList<>(list);
                    paymentMethods.add(0, ImmutablePaymentMethodImpl.NONE);

                    paymentMethodsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, paymentMethods);
                    paymentMethodsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    paymentMethodsSpinner.setAdapter(paymentMethodsAdapter);
                    if (presenter.isUsePaymentMethods()) {
                        paymentMethodsContainer.setVisibility(View.VISIBLE);
                        if (getReceipt() != null) {
                            final PaymentMethod oldPaymentMethod = getReceipt().getPaymentMethod();
                            final int paymentIdx = paymentMethodsAdapter.getPosition(oldPaymentMethod);
                            if (paymentIdx > 0) {
                                paymentMethodsSpinner.setSelection(paymentIdx);
                            }
                        }
                    }
                }
            }
        };
        categoriesTableController.subscribe(categoryTableEventsListener);
        paymentMethodsTableController.subscribe(paymentMethodTableEventsListener);
        categoriesTableController.get();
        paymentMethodsTableController.get();
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.debug(this, "onResume");

        final boolean isNewReceipt = getReceipt() == null;

        final String title;
        if (isNewReceipt) {
            title = getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_NEW);
        } else {
            if (presenter.isShowReceiptId()) {
                title = String.format(getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_EDIT_ID), getReceipt().getId());
            } else {
                title = getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_EDIT);
            }
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_cancel);
            actionBar.setTitle(title);
            actionBar.setSubtitle("");
        }

        if (isNewReceipt && presenter.isShowReceiptId()) {
            idDisposable = database.getNextReceiptAutoIncremenetIdHelper()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(receiptId -> {
                        if (isResumed()) {
                            final ActionBar bar = getSupportActionBar();
                            if (bar != null) {
                                final String titleWithId = String.format(getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_NEW_ID), receiptId);
                                bar.setTitle(titleWithId);
                            }
                        }
                    });
        }

        if (isNewReceipt) {
            if (presenter.isEnableAutoCompleteSuggestions()) {
                if (receiptsNameAutoCompleteAdapter == null) {
                    receiptsNameAutoCompleteAdapter = AutoCompleteAdapter.getInstance(getActivity(),
                            DatabaseHelper.TAG_RECEIPTS_NAME, database, database);
                } else {
                    receiptsNameAutoCompleteAdapter.reset();
                }
                if (receiptsCommentAutoCompleteAdapter == null) {
                    receiptsCommentAutoCompleteAdapter = AutoCompleteAdapter.getInstance(getActivity(),
                            DatabaseHelper.TAG_RECEIPTS_COMMENT, database);
                } else {
                    receiptsCommentAutoCompleteAdapter.reset();
                }
                nameBox.setAdapter(receiptsNameAutoCompleteAdapter);
                commentBox.setAdapter(receiptsCommentAutoCompleteAdapter);
            }
        }

        if (focusedView != null) {
            focusedView.requestFocus(); // Make sure we're focused on the right view
        }

        exchangeRateBox.setRetryListener(this);
        database.registerReceiptAutoCompleteListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_save, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigationHandler.navigateToReportInfoFragment(getParentTrip());
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
    public void onFocusChange(View v, boolean hasFocus) {
        focusedView = hasFocus ? v : null;
        if (getReceipt() == null && hasFocus) {
            // Only launch if we have focus and it's a new receipt
            SoftKeyboardManager.showKeyboard(v);
        }
    }

    @Override
    public void onUserRetry() {
        if (presenter.hasActivePlusPurchase()) {
            Logger.info(this, "Attempting to retry with valid subscription. Submitting request directly");
            submitExchangeRateRequest((String) currencySpinner.getSelectedItem());
        } else {
            Logger.info(this, "Attempting to retry without valid subscription. Directing user to purchase intent");
            final Activity activity = getActivity();
            if (activity instanceof SmartReceiptsActivity) {
                presenter.initiatePurchase();
            }
        }
    }

    @Override
    public void onReceiptRowAutoCompleteQueryResult(String name, String price, String category) {
        if (isAdded()) {
            if (nameBox != null && name != null) {
                nameBox.setText(name);
                nameBox.setSelection(name.length());
            }
            if (priceBox != null && price != null && priceBox.getText().length() == 0) {
                priceBox.setText(price);
            }
            if (categoriesSpinner != null && category != null) {
                for (int i = 0; i < categoriesList.size(); i++) {
                    if (category.equals(categoriesList.get(i).getName())) {
                        categoriesSpinner.setSelection(categoriesList.indexOf(categoriesList.get(i)));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onPause() {
        // Notify the downstream adapters
        if (receiptsNameAutoCompleteAdapter != null) {
            receiptsNameAutoCompleteAdapter.onPause();
        }
        if (receiptsCommentAutoCompleteAdapter != null) {
            receiptsCommentAutoCompleteAdapter.onPause();
        }
        if (idDisposable != null) {
            idDisposable.dispose();
            idDisposable = null;
        }

        // Dismiss the soft keyboard
        SoftKeyboardManager.hideKeyboard(focusedView);

        exchangeRateBox.setRetryListener(null);
        database.unregisterReceiptAutoCompleteListener();
        super.onPause();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.debug(this, "onSaveInstanceState");

        if (exchangeRateContainer != null && outState != null) {
            outState.putBoolean(KEY_OUT_STATE_IS_EXCHANGE_RATE_VISIBLE, exchangeRateContainer.getVisibility() == View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        categoriesTableController.unsubscribe(categoryTableEventsListener);
        paymentMethodsTableController.unsubscribe(paymentMethodTableEventsListener);
        fragmentStateCache.onDestroy(this);
        super.onDestroy();
    }

    private void configureExchangeRateField(@Nullable String baseCurrencyCode) {
        final String exchangeRateCurrencyCode = getParentTrip().getDefaultCurrencyCode();
        if (exchangeRateCurrencyCode.equals(baseCurrencyCode) || baseCurrencyCode == null) {
            exchangeRateContainer.setVisibility(View.GONE);
            exchangeRateBox.setText(""); // Clear out if we're hiding the box
        } else {
            exchangeRateContainer.setVisibility(View.VISIBLE);
            submitExchangeRateRequest(baseCurrencyCode);
        }
    }

    private synchronized void submitExchangeRateRequest(@NonNull String baseCurrencyCode) {
        exchangeRateBox.setText(""); // Clear results to avoid stale data here
        if (presenter.hasActivePlusPurchase()) {
            Logger.info(this, "Submitting exchange rate request");
            analytics.record(Events.Receipts.RequestExchangeRate);
            final String exchangeRateCurrencyCode = getParentTrip().getDefaultCurrencyCode();
            exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Loading);
            if (lastExchangeRateFetchCallback != null) {
                // Ignore any outstanding results to not confuse ourselves
                lastExchangeRateFetchCallback.ignoreResult();
            }
            lastExchangeRateFetchCallback = new MemoryLeakSafeCallback<ExchangeRate, EditText>(exchangeRateBox) {
                @Override
                public void success(EditText editText, Call<ExchangeRate> call, Response<ExchangeRate> response) {
                    final ExchangeRate exchangeRate = response.body();
                    if (exchangeRate != null && exchangeRate.supportsExchangeRateFor(exchangeRateCurrencyCode)) {
                        analytics.record(Events.Receipts.RequestExchangeRateSuccess);
                        if (TextUtils.isEmpty(editText.getText())) {
                            editText.setText(exchangeRate.getDecimalFormattedExchangeRate(exchangeRateCurrencyCode));
                        } else {
                            Logger.warn(ReceiptCreateEditFragment.this, "User already started typing... Ignoring exchange rate result");
                        }
                        exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Success);
                    } else {
                        Logger.error(ReceiptCreateEditFragment.this, "Received a null exchange rate");
                        analytics.record(Events.Receipts.RequestExchangeRateFailedWithNull);
                        exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Failure);
                    }
                }

                @Override
                public void failure(EditText editText, Call<ExchangeRate> call, Throwable th) {
                    Logger.error(ReceiptCreateEditFragment.this, th);
                    analytics.record(Events.Receipts.RequestExchangeRateFailed);
                    exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Failure);
                }
            };
            exchangeRateServiceManager.getService().getExchangeRate(dateBox.date, getString(R.string.exchange_rate_key), baseCurrencyCode).enqueue(lastExchangeRateFetchCallback);
        } else {
            exchangeRateBox.setCurrentState(NetworkRequestAwareEditText.State.Ready);
            Logger.info(this, "Ignoring exchange rate request, since there is no subscription for it");
        }
    }

    private void saveReceipt() {

        if (presenter.checkReceipt(dateBox.date)) {

            final String name = TextUtils.isEmpty(nameBox.getText().toString()) ? "" : nameBox.getText().toString();
            final Category category = categoriesAdpater.getItem(categoriesSpinner.getSelectedItemPosition());
            final String currency = currencySpinner.getSelectedItem().toString();
            final String price = priceBox.getText().toString();
            final String tax = taxBox.getText().toString();
            final String exchangeRate = exchangeRateBox.getText().toString();
            final String comment = commentBox.getText().toString();
            final PaymentMethod paymentMethod = (PaymentMethod) (presenter.isUsePaymentMethods() ? paymentMethodsSpinner.getSelectedItem() : null);
            final String extraText1 = (extra_edittext_box_1 == null) ? null : extra_edittext_box_1.getText().toString();
            final String extraText2 = (extra_edittext_box_2 == null) ? null : extra_edittext_box_2.getText().toString();
            final String extraText3 = (extra_edittext_box_3 == null) ? null : extra_edittext_box_3.getText().toString();

            receiptInputCache.setCachedDate((Date) dateBox.date.clone());
            receiptInputCache.setCachedCategory(category);
            receiptInputCache.setCachedCurrency(currency);

            presenter.saveReceipt(dateBox.date, price, tax, exchangeRate, comment,
                    paymentMethod, reimbursable.isChecked(), fullpage.isChecked(), name, category, currency,
                    extraText1, extraText2, extraText3);

            analytics.record(getReceipt() == null ? Events.Receipts.PersistNewReceipt : Events.Receipts.PersistUpdateReceipt);
            dateManager.setDateEditTextListenerDialogHolder(null);


            navigationHandler.navigateToReportInfoFragment(getParentTrip());
        }
    }

    public void showDateError() {
        Toast.makeText(getActivity(), getFlexString(R.string.CALENDAR_TAB_ERROR), Toast.LENGTH_SHORT).show();
    }

    public void showDateWarning() {
        Toast.makeText(getActivity(), getFlexString(R.string.DIALOG_RECEIPTMENU_TOAST_BAD_DATE), Toast.LENGTH_LONG).show();
    }

    private class SpinnerSelectionListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            if (presenter.isMatchReceiptNameToCategory()) {
                nameBox.setText(categoriesAdpater.getItem(position).getName());
            }
            if (presenter.isMatchReceiptCommentToCategory()) {
                commentBox.setText(categoriesAdpater.getItem(position).getName());
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
