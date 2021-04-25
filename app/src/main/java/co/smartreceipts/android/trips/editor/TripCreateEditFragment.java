package co.smartreceipts.android.trips.editor;

import android.content.Context;
import android.os.Bundle;
import android.text.method.TextKeyListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.rxbinding2.widget.RxDateEditText;
import com.jakewharton.rxbinding3.widget.RxTextView;

import org.jetbrains.annotations.NotNull;

import java.sql.Date;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import co.smartreceipts.analytics.log.Logger;
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
import co.smartreceipts.android.databinding.UpdateTripBinding;
import co.smartreceipts.android.date.DateFormatter;
import co.smartreceipts.android.editor.Editor;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.model.AutoCompleteUpdateEvent;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.utils.CurrencyUtils;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.tooltip.TooltipPresenter;
import co.smartreceipts.android.tooltip.TooltipView;
import co.smartreceipts.android.tooltip.model.TooltipMetadata;
import co.smartreceipts.android.tooltip.model.TooltipType;
import co.smartreceipts.android.trips.editor.currency.TripCurrencyCodeSupplier;
import co.smartreceipts.android.trips.editor.date.TripDateView;
import co.smartreceipts.android.trips.editor.date.TripDatesPresenter;
import co.smartreceipts.android.utils.SoftKeyboardManager;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import kotlin.Unit;
import wb.android.flex.Flex;

public class TripCreateEditFragment extends WBFragment implements Editor<Trip>,
        View.OnFocusChangeListener,
        TooltipView,
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
    DateFormatter dateFormatter;

    @Inject
    TripCreateEditFragmentPresenter presenter;

    @Inject
    AutoCompletePresenter<Trip> tripAutoCompletePresenter;

    @Inject
    TooltipPresenter tooltipPresenter;

    // Constructed Presenters
    private TripDatesPresenter tripDatesPresenter;
    private CurrencyListEditorPresenter currencyListEditorPresenter;
    private DefaultCurrencyListEditorView defaultCurrencyListEditorView;

    private UpdateTripBinding binding;

    // Misc Views
    private View focusedView;

    private AutoCompleteArrayAdapter<Trip> resultsAdapter;
    private Snackbar snackbar;
    private boolean shouldHideResults;
    private AutoCompleteResult<Trip> itemToRemoveOrReAdd;

    private Subject<AutoCompleteUpdateEvent<Trip>> _hideAutoCompleteVisibilityClicks =
            PublishSubject.<AutoCompleteUpdateEvent<Trip>>create().toSerialized();
    private Subject<AutoCompleteUpdateEvent<Trip>> _unHideAutoCompleteVisibilityClicks =
            PublishSubject.<AutoCompleteUpdateEvent<Trip>>create().toSerialized();

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
        defaultCurrencyListEditorView = new DefaultCurrencyListEditorView(requireContext(), () -> binding.currency.get());
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
        binding = UpdateTripBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Apply white-label settings via our 'Flex' mechanism to update defaults
        flex.applyCustomSettings(binding.name);
        flex.applyCustomSettings(binding.startDate);
        flex.applyCustomSettings(binding.endDate);
        flex.applyCustomSettings(binding.currency.get());
        flex.applyCustomSettings(binding.tripComment);
        flex.applyCustomSettings(binding.costCenter);

        // Toolbar stuff
        if (navigationHandler.isDualPane()) {
            binding.toolbar.toolbar.setVisibility(View.GONE);
        } else {
            setSupportActionBar(binding.toolbar.toolbar);
        }

        // Show default dictionary with auto-complete
        TextKeyListener input = TextKeyListener.getInstance(true, TextKeyListener.Capitalize.SENTENCES);
        binding.name.setKeyListener(input);

        // Configure default separators
        binding.startDate.setDateFormatter(dateFormatter);
        binding.endDate.setDateFormatter(dateFormatter);

        // Set Cost Center Visibility
        binding.dialogTripmenuCostCenterLayout.setVisibility(presenter.isIncludeCostCenter() ? View.VISIBLE : View.GONE);

        setKeyboardRelatedListeners();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fillFields();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_save, menu);
        menu.findItem(R.id.menu_main_search).setVisible(false);
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
        tooltipPresenter.subscribe();
        presenter.subscribe();
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_24dp);
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
        tooltipPresenter.unsubscribe();
        tripAutoCompletePresenter.unsubscribe();
        currencyListEditorPresenter.unsubscribe();
        tripDatesPresenter.unsubscribe();
        presenter.unsubscribe();
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Logger.debug(this, "onDestroyView");
        focusedView = null;
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        currencyListEditorPresenter.onSaveInstanceState(outState);
    }

    private void fillFields() {
        if (getEditableItem() == null) { // new trip
            //pre-fill the dates
            final Calendar startCalendar = Calendar.getInstance();
            startCalendar.set(Calendar.HOUR_OF_DAY, 0);
            startCalendar.set(Calendar.MINUTE, 0);
            startCalendar.set(Calendar.SECOND, 0);
            startCalendar.set(Calendar.MILLISECOND, 0);

            binding.startDate.setDate(new Date(startCalendar.getTimeInMillis()));
            binding.endDate.setDate(new Date(startCalendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(presenter.getDefaultTripDuration())));
        } else { // edit trip
            binding.name.setText(getEditableItem().getName());
            binding.startDate.setDate(getEditableItem().getStartDate());
            binding.startDate.setTimeZone(getEditableItem().getStartTimeZone());
            binding.endDate.setDate(getEditableItem().getEndDate());
            binding.endDate.setTimeZone(getEditableItem().getEndTimeZone());
            binding.tripComment.setText(getEditableItem().getComment());
            binding.costCenter.setText(getEditableItem().getCostCenter());
        }

        // Focused View
        if (focusedView == null) {
            focusedView = binding.name;
        }

        binding.startDate.setFocusableInTouchMode(false);
        binding.endDate.setFocusableInTouchMode(false);
        binding.name.setSelection(binding.name.getText().length()); // Put the cursor at the end
    }

    private void setKeyboardRelatedListeners() {
        // Set each focus listener, so we can track the focus view across resume -> pauses
        binding.name.setOnFocusChangeListener(this);
        binding.startDate.setOnFocusChangeListener(this);
        binding.endDate.setOnFocusChangeListener(this);
        binding.currency.get().setOnFocusChangeListener(this);
        binding.tripComment.setOnFocusChangeListener(this);
        binding.costCenter.setOnFocusChangeListener(this);

        // Set click listeners
        View.OnTouchListener hideSoftKeyboardOnTouchListener = (view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                SoftKeyboardManager.hideKeyboard(view);
            }
            view.performClick();
            return false;
        };
        binding.startDate.setOnTouchListener(hideSoftKeyboardOnTouchListener);
        binding.endDate.setOnTouchListener(hideSoftKeyboardOnTouchListener);
        binding.currency.get().setOnTouchListener(hideSoftKeyboardOnTouchListener);
    }

    private void saveTripChanges() {
        String name = binding.name.getText().toString().trim();
        final String startDateText = binding.startDate.getText().toString();
        final String endDateText = binding.endDate.getText().toString();
        final String comment = binding.tripComment.getText().toString();
        final String costCenter = binding.costCenter.getText().toString();

        final String currencyCode;
        if (binding.currency.get().getSelectedItem() != null) {
            currencyCode = binding.currency.get().getSelectedItem().toString();
        } else {
            currencyCode = CurrencyUtils.INSTANCE.getDefaultCurrency().getCode();
        }

        if (presenter.checkTrip(name, startDateText, binding.startDate.getDate(), endDateText, binding.endDate.getDate())) {
            presenter.saveTrip(name, binding.startDate.getDate(), binding.startDate.getTimeZone(), binding.endDate.getDate(), binding.endDate.getTimeZone(), currencyCode, comment, costCenter);
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
            binding.currency.get().setSelection(position);
            if (getEditableItem() != null && position >= 0 && !getEditableItem().getDefaultCurrencyCode().equals(binding.currency.get().getItemAtPosition(position).toString())) {
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
            binding.endDate.setDate(date);
            binding.endDate.setTimeZone(TimeZone.getDefault());
        };
    }

    @NonNull
    @Override
    public Observable<Date> getStartDateChanges() {
        return RxDateEditText.INSTANCE.dateChanges(binding.startDate);
    }

    private String getFlexString(int id) {
        return getFlexString(flex, id);
    }

    @NotNull
    @Override
    public Observable<CharSequence> getTextChangeStream(@NotNull AutoCompleteField field) {
        if (field == TripAutoCompleteField.Name) {
            return RxTextView.textChanges(binding.name);
        } else if (field == TripAutoCompleteField.Comment) {
            return RxTextView.textChanges(binding.tripComment);
        } else if (field == TripAutoCompleteField.CostCenter) {
            return RxTextView.textChanges(binding.costCenter);
        } else {
            throw new IllegalArgumentException("Unsupported field type: " + field);
        }
    }

    @Override
    public void displayAutoCompleteResults(@NotNull AutoCompleteField field, @NotNull List<AutoCompleteResult<Trip>> autoCompleteResults) {
        if (!shouldHideResults) {
            if (snackbar != null && snackbar.isShown()) {
                snackbar.dismiss();
            }
            resultsAdapter = new AutoCompleteArrayAdapter<>(requireContext(), autoCompleteResults, this);
            if (field == TripAutoCompleteField.Name) {
                binding.name.setAdapter(resultsAdapter);
                if (binding.name.hasFocus()) {
                    binding.name.showDropDown();
                }
            } else if (field == TripAutoCompleteField.Comment) {
                binding.tripComment.setAdapter(resultsAdapter);
                if (binding.tripComment.hasFocus()) {
                    binding.tripComment.showDropDown();
                }
            } else if (field == TripAutoCompleteField.CostCenter) {
                binding.costCenter.setAdapter(resultsAdapter);
                if (binding.costCenter.hasFocus()) {
                    binding.costCenter.showDropDown();
                }
            } else {
                throw new IllegalArgumentException("Unsupported field type: " + field);
            }
        } else {
            shouldHideResults = false;
        }
    }

    @Nullable
    @Override
    public Trip getEditableItem() {
        return getArguments() != null ? getArguments().getParcelable(Trip.PARCEL_KEY) : null;
    }

    @NotNull
    @Override
    public List<TooltipType> getSupportedTooltips() {
        return Collections.singletonList(TooltipType.FirstReportHint);
    }

    @Override
    public void display(@NotNull TooltipMetadata tooltip) {
        binding.tooltip.setTooltip(tooltip);
        if (binding.tooltip.getVisibility() != View.VISIBLE) {
            binding.tooltip.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideTooltip() {
        if (binding.tooltip.getVisibility() != View.GONE) {
            binding.tooltip.setVisibility(View.GONE);
        }
    }

    @NotNull
    @Override
    public Observable<Unit> getTooltipClickStream() {
        return binding.tooltip.getTooltipClickStream();
    }

    @NotNull
    @Override
    public Observable<Unit> getButtonNoClickStream() {
        return binding.tooltip.getButtonNoClickStream();
    }

    @NotNull
    @Override
    public Observable<Unit> getButtonYesClickStream() {
        return binding.tooltip.getButtonYesClickStream();
    }

    @NotNull
    @Override
    public Observable<Unit> getButtonCancelClickStream() {
        return binding.tooltip.getButtonCancelClickStream();
    }

    @NotNull
    @Override
    public Observable<Unit> getCloseIconClickStream() {
        return binding.tooltip.getCloseIconClickStream();
    }

    @Override
    public void fillValueField(@NotNull AutoCompleteResult<Trip> autoCompleteResult) {
        shouldHideResults = true;
        if (binding.name.isPopupShowing()) {
            binding.name.setText(autoCompleteResult.getDisplayName());
            binding.name.setSelection(binding.name.getText().length());
            binding.name.dismissDropDown();
        } else if (binding.tripComment.isPopupShowing()) {
            binding.tripComment.setText(autoCompleteResult.getDisplayName());
            binding.tripComment.setSelection(binding.tripComment.getText().length());
            binding.tripComment.dismissDropDown();
        } else {
            binding.costCenter.setText(autoCompleteResult.getDisplayName());
            binding.costCenter.setSelection(binding.costCenter.getText().length());
            binding.costCenter.dismissDropDown();
        }
        SoftKeyboardManager.hideKeyboard(focusedView);
    }

    @Override
    public void sendAutoCompleteHideEvent(@NotNull AutoCompleteResult<Trip> autoCompleteResult) {
        SoftKeyboardManager.hideKeyboard(focusedView);
        if (binding.name.isPopupShowing()) {
            _hideAutoCompleteVisibilityClicks.onNext(
                    new AutoCompleteUpdateEvent(autoCompleteResult, TripAutoCompleteField.Name, resultsAdapter.getPosition(autoCompleteResult)));
        } else if (binding.tripComment.isPopupShowing()) {
            _hideAutoCompleteVisibilityClicks.onNext(
                    new AutoCompleteUpdateEvent(autoCompleteResult, TripAutoCompleteField.Comment, resultsAdapter.getPosition(autoCompleteResult)));
        } else {
            _hideAutoCompleteVisibilityClicks.onNext(
                    new AutoCompleteUpdateEvent(autoCompleteResult, TripAutoCompleteField.CostCenter, resultsAdapter.getPosition(autoCompleteResult)));
        }
    }

    @Override
    public void removeValueFromAutoComplete(int position) {
        getActivity().runOnUiThread(() -> {
            if (position >= 0 && position < resultsAdapter.getCount()) {
                itemToRemoveOrReAdd = resultsAdapter.getItem(position);
                resultsAdapter.remove(itemToRemoveOrReAdd);
                resultsAdapter.notifyDataSetChanged();
                View view = getActivity().findViewById(R.id.update_trip_layout);
                snackbar = Snackbar.make(view, getString(
                        R.string.item_removed_from_auto_complete, itemToRemoveOrReAdd.getDisplayName()), Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.undo, v -> {
                    if (binding.name.hasFocus()) {
                        _unHideAutoCompleteVisibilityClicks.onNext(new AutoCompleteUpdateEvent(itemToRemoveOrReAdd, TripAutoCompleteField.Name, position));
                    } else if (binding.tripComment.hasFocus()) {
                        _unHideAutoCompleteVisibilityClicks.onNext(new AutoCompleteUpdateEvent(itemToRemoveOrReAdd, TripAutoCompleteField.Comment, position));
                    } else {
                        _unHideAutoCompleteVisibilityClicks.onNext(new AutoCompleteUpdateEvent(itemToRemoveOrReAdd, TripAutoCompleteField.CostCenter, position));
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
    public Observable<AutoCompleteUpdateEvent<Trip>> getHideAutoCompleteVisibilityClick() {
        return _hideAutoCompleteVisibilityClicks;
    }

    @NotNull
    @Override
    public Observable<AutoCompleteUpdateEvent<Trip>> getUnHideAutoCompleteVisibilityClick() {
        return _unHideAutoCompleteVisibilityClicks;
    }
}
