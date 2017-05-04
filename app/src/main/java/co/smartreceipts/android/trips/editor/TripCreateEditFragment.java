package co.smartreceipts.android.trips.editor;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.sql.Date;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.date.DateEditText;
import co.smartreceipts.android.date.DateManager;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.utils.SoftKeyboardManager;
import co.smartreceipts.android.utils.cache.FragmentArgumentCache;
import dagger.android.support.AndroidSupportInjection;
import wb.android.autocomplete.AutoCompleteAdapter;
import wb.android.flex.Flex;

public class TripCreateEditFragment extends WBFragment implements View.OnFocusChangeListener {
    
    @Inject
    Flex flex;
    @Inject
    DateManager dateManager;
    @Inject
    NavigationHandler navigationHandler;
    @Inject
    FragmentArgumentCache fragmentArgumentCache;

    @Inject
    TripCreateEditFragmentPresenter presenter;

    private AutoCompleteTextView nameBox;
    private DateEditText startDateBox;
    private DateEditText endDateBox;
    private Spinner currencySpinner;
    private EditText commentBox;
    private AutoCompleteTextView costCenterBox;

    private View focusedView;
    private AutoCompleteAdapter nameAutoCompleteAdapter, costCenterAutoCompleteAdapter;

    private ArrayAdapter<CharSequence> currencies;

    public static TripCreateEditFragment newInstance() {
        return new TripCreateEditFragment();
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public Trip getTrip() {
        if (fragmentArgumentCache.get(TripCreateEditFragment.class) != null) {
            return fragmentArgumentCache.get(TripCreateEditFragment.class).getParcelable(Trip.PARCEL_KEY);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.update_trip, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
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
            navigationHandler.navigateToHomeTripsFragment();
            return true;
        }
        if (item.getItemId() == R.id.action_save) {
            saveTripChanges();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_cancel);
            actionBar.setTitle((getTrip() == null) ? getFlexString(R.string.DIALOG_TRIPMENU_TITLE_NEW) : getFlexString(R.string.DIALOG_TRIPMENU_TITLE_EDIT));
            actionBar.setSubtitle("");
        }

        if (focusedView != null) {
            focusedView.requestFocus(); // Make sure we're focused on the right view
        }
    }

    @Override
    public void onPause() {
        if (nameAutoCompleteAdapter != null) {
            nameAutoCompleteAdapter.onPause();
        }
        if (costCenterAutoCompleteAdapter != null) {
            costCenterAutoCompleteAdapter.onPause();
        }

        // Dismiss the soft keyboard
        SoftKeyboardManager.hideKeyboard(focusedView);

        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (!getActivity().isChangingConfigurations()) { // clear cache if fragment is not going to be recreated
            fragmentArgumentCache.remove(TripCreateEditFragment.class);
        }
        super.onDestroy();
    }

    private void initViews(View rootView) {
        nameBox = (AutoCompleteTextView) flex.getSubView(getActivity(), rootView, R.id.dialog_tripmenu_name);
        startDateBox = (DateEditText) flex.getSubView(getActivity(), rootView, R.id.dialog_tripmenu_start);
        endDateBox = (DateEditText) flex.getSubView(getActivity(), rootView, R.id.dialog_tripmenu_end);
        currencySpinner = (Spinner) flex.getSubView(getActivity(), rootView, R.id.dialog_tripmenu_currency);
        commentBox = (EditText) flex.getSubView(getActivity(), rootView, R.id.dialog_tripmenu_comment);

        costCenterBox = (AutoCompleteTextView) rootView.findViewById(R.id.dialog_tripmenu_cost_center);
        View costCenterBoxLayout = rootView.findViewById(R.id.dialog_tripmenu_cost_center_layout);
        costCenterBoxLayout.setVisibility(presenter.isIncludeCostCenter() ? View.VISIBLE : View.GONE);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        if (navigationHandler.isDualPane()) {
            toolbar.setVisibility(View.GONE);
        } else {
            setSupportActionBar(toolbar);
        }

        currencies = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, presenter.getCurrenciesList());
        currencies.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currencies);

        // Show default dictionary with auto-complete
        TextKeyListener input = TextKeyListener.getInstance(true, TextKeyListener.Capitalize.SENTENCES);
        nameBox.setKeyListener(input);

        setKeyboardRelatedListeners();
    }

    private void fillFields() {
        int currencySpinnerPosition;

        if (getTrip() == null) { // new trip

            if (presenter.isEnableAutoCompleteSuggestions()) {
                DatabaseHelper db = presenter.getDatabaseHelper();
                nameAutoCompleteAdapter = AutoCompleteAdapter.getInstance(getActivity(), DatabaseHelper.TAG_TRIPS_NAME, db);
                costCenterAutoCompleteAdapter = AutoCompleteAdapter.getInstance(getActivity(), DatabaseHelper.TAG_TRIPS_COST_CENTER, db);

                nameBox.setAdapter(nameAutoCompleteAdapter);
                costCenterBox.setAdapter(costCenterAutoCompleteAdapter);
            }

            startDateBox.setOnClickListener(dateManager.getDurationDateEditTextListener(endDateBox));

            //prefill the dates
            startDateBox.date = new Date(Calendar.getInstance().getTimeInMillis());
            startDateBox.setText(DateFormat.getDateFormat(getActivity()).format(startDateBox.date));
            endDateBox.date = new Date(startDateBox.date.getTime() + TimeUnit.DAYS.toMillis(presenter.getDefaultTripDuration()));
            endDateBox.setText(DateFormat.getDateFormat(getActivity()).format(endDateBox.date));

            currencySpinnerPosition = currencies.getPosition(presenter.getDefaultCurrency());
        } else { // edit trip
            nameBox.setText(getTrip().getName());

            startDateBox.setText(getTrip().getFormattedStartDate(getActivity(), presenter.getDateSeparator()));
            startDateBox.date = getTrip().getStartDate();

            endDateBox.setText(getTrip().getFormattedEndDate(getActivity(), presenter.getDateSeparator()));
            endDateBox.date = getTrip().getEndDate();

            commentBox.setText(getTrip().getComment());

            currencySpinnerPosition = currencies.getPosition(getTrip().getDefaultCurrencyCode());

            startDateBox.setOnClickListener(dateManager.getDateEditTextListener());
            costCenterBox.setText(getTrip().getCostCenter());

            currencySpinner.setOnItemSelectedListener(new CurrencySpinnerSelectionListener());

        }

        // Focused View
        if (focusedView == null) {
            focusedView = nameBox;
        }
        // set currency
        if (currencySpinnerPosition > 0) {
            currencySpinner.setSelection(currencySpinnerPosition);
        }

        startDateBox.setFocusableInTouchMode(false);

        endDateBox.setFocusableInTouchMode(false);
        endDateBox.setOnClickListener(dateManager.getDateEditTextListener());
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

        if (presenter.checkTrip(name, startDateText, startDateBox.date, endDateText, endDateBox.date)) {
            Trip updatedTrip = presenter.saveTrip(name, startDateBox.date, endDateBox.date,
                    currencyCode, comment, costCenter);
            // open created/edited trip info
            navigationHandler.navigateToReportInfoFragmentWithoutBackStack(updatedTrip);
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
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        focusedView = hasFocus ? view : null;
        if (getTrip() == null && hasFocus) {
            SoftKeyboardManager.showKeyboard(view);
        }
    }

    private class CurrencySpinnerSelectionListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            final String newCurrencyCode = currencies.getItem(position).toString();

            if (!getTrip().getDefaultCurrencyCode().equals(newCurrencyCode)) {
                Toast.makeText(view.getContext(), R.string.toast_warning_reset_exchange_rate, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Intentional no-op
        }
    }

    private String getFlexString(int id) {
        return getFlexString(flex, id);
    }
}
