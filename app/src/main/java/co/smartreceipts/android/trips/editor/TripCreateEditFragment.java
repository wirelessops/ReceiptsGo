package co.smartreceipts.android.trips.editor;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.method.TextKeyListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import com.jakewharton.rxbinding2.widget.RxDateEditText;
import com.jakewharton.rxbinding2.widget.RxTextView;

import org.jetbrains.annotations.NotNull;

import java.sql.Date;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.autocomplete.AutoCompleteArrayAdapter;
import co.smartreceipts.android.autocomplete.AutoCompleteField;
import co.smartreceipts.android.autocomplete.AutoCompletePresenter;
import co.smartreceipts.android.autocomplete.AutoCompleteResult;
import co.smartreceipts.android.autocomplete.AutoCompleteView;
import co.smartreceipts.android.autocomplete.trip.TripAutoCompleteField;
import co.smartreceipts.android.currency.widget.CurrencyListEditorPresenter;
import co.smartreceipts.android.currency.widget.CurrencyListEditorView;
import co.smartreceipts.android.currency.widget.DefaultCurrencyListEditorView;
import co.smartreceipts.android.date.DateEditText;
import co.smartreceipts.android.editor.Editor;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.trips.editor.currency.TripCurrencyCodeSupplier;
import co.smartreceipts.android.trips.editor.date.TripDateView;
import co.smartreceipts.android.trips.editor.date.TripDatesPresenter;
import co.smartreceipts.android.utils.SoftKeyboardManager;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import wb.android.flex.Flex;

public class TripCreateEditFragment extends WBFragment implements Editor<Trip>,
        View.OnFocusChangeListener,
        CurrencyListEditorView,
        TripDateView,
        AutoCompleteView<Trip> {

    public static final String ARG_EXISTING_TRIPS = "arg_existing_trips";

    @Inject
    Flex flex;

    @Inject
    NavigationHandler navigationHandler;

    @Inject
    UserPreferenceManager userPreferenceManager;

    @Inject
    DatabaseHelper database;

    @Inject
    TripCreateEditFragmentPresenter presenter;

    @Inject
    AutoCompletePresenter<Trip> tripAutoCompletePresenter;

    TripDatesPresenter tripDatesPresenter;

    private AutoCompleteTextView nameBox;
    private DateEditText startDateBox;
    private DateEditText endDateBox;
    private Spinner currencySpinner;
    private AutoCompleteTextView commentBox;
    private AutoCompleteTextView costCenterBox;

    private View focusedView;

    // Presenters
    private CurrencyListEditorPresenter currencyListEditorPresenter;

    private DefaultCurrencyListEditorView defaultCurrencyListEditorView;

    public static TripCreateEditFragment newInstance() {
        return new TripCreateEditFragment();
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        final TripCurrencyCodeSupplier currencyCodeSupplier = new TripCurrencyCodeSupplier(userPreferenceManager, getEditableItem());
        currencyListEditorPresenter = new CurrencyListEditorPresenter(this, database, currencyCodeSupplier, savedInstanceState);
        defaultCurrencyListEditorView = new DefaultCurrencyListEditorView(requireContext(), () -> currencySpinner);
        tripDatesPresenter = new TripDatesPresenter(this, userPreferenceManager, getEditableItem());
    }

    @NonNull
    public List<Trip> getExistingTrips() {
        final List<Trip> existingTrips = getArguments() != null ? getArguments().getParcelableArrayList(ARG_EXISTING_TRIPS) : Collections.emptyList();
        if (existingTrips != null) {
            return existingTrips;
        } else {
            return Collections.emptyList();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.update_trip, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fillFields();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_save, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigationHandler.navigateBack();
            return true;
        }
        if (item.getItemId() == R.id.action_save) {
            saveTripChanges();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        tripAutoCompletePresenter.subscribe();
        currencyListEditorPresenter.subscribe();
        tripDatesPresenter.subscribe();
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_cancel);
            actionBar.setTitle((getEditableItem() == null) ? getFlexString(R.string.DIALOG_TRIPMENU_TITLE_NEW) : getFlexString(R.string.DIALOG_TRIPMENU_TITLE_EDIT));
            actionBar.setSubtitle("");
        }

        if (focusedView != null) {
            focusedView.requestFocus(); // Make sure we're focused on the right view
        }
    }

    @Override
    public void onPause() {
        // Dismiss the soft keyboard
        SoftKeyboardManager.hideKeyboard(focusedView);

        super.onPause();
    }

    @Override
    public void onStop() {
        tripAutoCompletePresenter.unsubscribe();
        currencyListEditorPresenter.unsubscribe();
        tripDatesPresenter.unsubscribe();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        currencyListEditorPresenter.onSaveInstanceState(outState);
    }

    private void initViews(View rootView) {
        nameBox = (AutoCompleteTextView) flex.getSubView(getActivity(), rootView, R.id.dialog_tripmenu_name);
        startDateBox = (DateEditText) flex.getSubView(getActivity(), rootView, R.id.dialog_tripmenu_start);
        endDateBox = (DateEditText) flex.getSubView(getActivity(), rootView, R.id.dialog_tripmenu_end);
        currencySpinner = (Spinner) flex.getSubView(getActivity(), rootView, R.id.dialog_tripmenu_currency);
        commentBox = (AutoCompleteTextView) flex.getSubView(getActivity(), rootView, R.id.dialog_tripmenu_comment);

        costCenterBox = rootView.findViewById(R.id.dialog_tripmenu_cost_center);
        View costCenterBoxLayout = rootView.findViewById(R.id.dialog_tripmenu_cost_center_layout);
        costCenterBoxLayout.setVisibility(presenter.isIncludeCostCenter() ? View.VISIBLE : View.GONE);

        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        if (navigationHandler.isDualPane()) {
            toolbar.setVisibility(View.GONE);
        } else {
            setSupportActionBar(toolbar);
        }

        // Show default dictionary with auto-complete
        TextKeyListener input = TextKeyListener.getInstance(true, TextKeyListener.Capitalize.SENTENCES);
        nameBox.setKeyListener(input);

        // Configure default separators
        startDateBox.setDateSeparator(userPreferenceManager.get(UserPreference.General.DateSeparator));
        endDateBox.setDateSeparator(userPreferenceManager.get(UserPreference.General.DateSeparator));

        setKeyboardRelatedListeners();
    }

    private void fillFields() {
        if (getEditableItem() == null) { // new trip
            //prefill the dates
            final Calendar startCalendar = Calendar.getInstance();
            startCalendar.set(Calendar.HOUR_OF_DAY, 0);
            startCalendar.set(Calendar.MINUTE, 0);
            startCalendar.set(Calendar.SECOND, 0);
            startCalendar.set(Calendar.MILLISECOND, 0);

            startDateBox.setDate(new Date(startCalendar.getTimeInMillis()));
            endDateBox.setDate(new Date(startCalendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(presenter.getDefaultTripDuration())));
        } else { // edit trip
            nameBox.setText(getEditableItem().getName());
            startDateBox.setDate(getEditableItem().getStartDate());
            startDateBox.setTimeZone(getEditableItem().getStartTimeZone());
            endDateBox.setDate(getEditableItem().getEndDate());
            endDateBox.setTimeZone(getEditableItem().getEndTimeZone());
            commentBox.setText(getEditableItem().getComment());
            costCenterBox.setText(getEditableItem().getCostCenter());
        }

        // Focused View
        if (focusedView == null) {
            focusedView = nameBox;
        }

        startDateBox.setFocusableInTouchMode(false);
        endDateBox.setFocusableInTouchMode(false);
        nameBox.setSelection(nameBox.getText().length()); // Put the cursor at the end
    }

    private void setKeyboardRelatedListeners() {
        // Set each focus listener, so we can track the focus view across resume -> pauses
        nameBox.setOnFocusChangeListener(this);
        startDateBox.setOnFocusChangeListener(this);
        endDateBox.setOnFocusChangeListener(this);
        currencySpinner.setOnFocusChangeListener(this);
        commentBox.setOnFocusChangeListener(this);
        costCenterBox.setOnFocusChangeListener(this);

        // Set click listeners
        View.OnTouchListener hideSoftKeyboardOnTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    SoftKeyboardManager.hideKeyboard(view);
                }
                view.performClick();
                return false;
            }
        };
        startDateBox.setOnTouchListener(hideSoftKeyboardOnTouchListener);
        endDateBox.setOnTouchListener(hideSoftKeyboardOnTouchListener);
        currencySpinner.setOnTouchListener(hideSoftKeyboardOnTouchListener);
    }

    private void saveTripChanges() {
        String name = nameBox.getText().toString().trim();
        final String startDateText = startDateBox.getText().toString();
        final String endDateText = endDateBox.getText().toString();
        final String currencyCode = currencySpinner.getSelectedItem().toString();
        final String comment = commentBox.getText().toString();
        final String costCenter = costCenterBox.getText().toString();

        if (presenter.checkTrip(name, startDateText, startDateBox.getDate(), endDateText, endDateBox.getDate())) {
            presenter.saveTrip(name, startDateBox.getDate(), startDateBox.getTimeZone(), endDateBox.getDate(), endDateBox.getTimeZone(), currencyCode, comment, costCenter);
            navigationHandler.navigateBack();
        }
    }

    public void showError(TripEditorErrors error) {
        switch (error) {
            case MISSING_FIELD:
                Toast.makeText(getActivity(), getFlexString(R.string.DIALOG_TRIPMENU_TOAST_MISSING_FIELD), Toast.LENGTH_LONG).show();
                break;
            case CALENDAR_ERROR:
                Toast.makeText(getActivity(), getFlexString(R.string.CALENDAR_TAB_ERROR), Toast.LENGTH_LONG).show();
                break;
            case DURATION_ERROR:
                Toast.makeText(getActivity(), getFlexString(R.string.DURATION_ERROR), Toast.LENGTH_LONG).show();
                break;
            case SPACE_ERROR:
                Toast.makeText(getActivity(), getFlexString(R.string.SPACE_ERROR), Toast.LENGTH_LONG).show();
                break;
            case ILLEGAL_CHAR_ERROR:
                Toast.makeText(getActivity(), getFlexString(R.string.ILLEGAL_CHAR_ERROR), Toast.LENGTH_LONG).show();
                break;
            case NON_UNIQUE_NAME:
                Toast.makeText(getActivity(), R.string.toast_error_trip_exists, Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        focusedView = hasFocus ? view : null;
        if (getEditableItem() == null && hasFocus) {
            SoftKeyboardManager.showKeyboard(view);
        }
    }

    @NonNull
    @Override
    public Consumer<? super List<CharSequence>> displayCurrencies() {
        return defaultCurrencyListEditorView.displayCurrencies();
    }

    @NonNull
    @Override
    public Consumer<? super Integer> displayCurrencySelection() {
        // Note: we override the default behavior in the #link DefaultCurrencyListEditorView class for the exchange rate warning
        return (Consumer<Integer>) position -> {
            currencySpinner.setSelection(position);
            if (getEditableItem() != null && position >= 0 && !getEditableItem().getDefaultCurrencyCode().equals(currencySpinner.getItemAtPosition(position).toString())) {
                Toast.makeText(getContext(), R.string.toast_warning_reset_exchange_rate, Toast.LENGTH_LONG).show();
            }
        };
    }

    @NonNull
    @Override
    public Observable<Integer> currencyClicks() {
        return defaultCurrencyListEditorView.currencyClicks();
    }

    @NonNull
    @Override
    public Consumer<Date> displayEndDate() {
        return date -> {
            endDateBox.setDate(date);
            endDateBox.setTimeZone(TimeZone.getDefault());
        };
    }

    @NonNull
    @Override
    public Observable<Date> getStartDateChanges() {
        return RxDateEditText.dateChanges(startDateBox);
    }

    private String getFlexString(int id) {
        return getFlexString(flex, id);
    }

    @NotNull
    @Override
    public Observable<CharSequence> getTextChangeStream(@NotNull AutoCompleteField field) {
        if (field == TripAutoCompleteField.Name) {
            return RxTextView.textChanges(nameBox);
        } else if (field == TripAutoCompleteField.Comment) {
            return RxTextView.textChanges(commentBox);
        } else if (field == TripAutoCompleteField.CostCenter) {
            return RxTextView.textChanges(costCenterBox);
        } else {
            throw new IllegalArgumentException("Unsupported field type: " + field);
        }
    }

    @Override
    public void displayAutoCompleteResults(@NotNull AutoCompleteField field, @NotNull List<AutoCompleteResult<Trip>> autoCompleteResults) {
        final AutoCompleteArrayAdapter<Trip> resultsAdapter = new AutoCompleteArrayAdapter<>(requireContext(), autoCompleteResults);
        if (field == TripAutoCompleteField.Name) {
            nameBox.setAdapter(resultsAdapter);
            nameBox.showDropDown();
        } else if (field == TripAutoCompleteField.Comment) {
            commentBox.setAdapter(resultsAdapter);
            commentBox.showDropDown();
        } else if (field == TripAutoCompleteField.CostCenter) {
            costCenterBox.setAdapter(resultsAdapter);
            costCenterBox.showDropDown();
        } else {
            throw new IllegalArgumentException("Unsupported field type: " + field);
        }
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public Trip getEditableItem() {
        return getArguments() != null ? getArguments().getParcelable(Trip.PARCEL_KEY) : null;
    }
}
