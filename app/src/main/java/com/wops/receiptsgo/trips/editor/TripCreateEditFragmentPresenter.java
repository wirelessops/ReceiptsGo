package com.wops.receiptsgo.trips.editor;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import java.io.File;
import java.sql.Date;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import com.wops.analytics.Analytics;
import com.wops.analytics.events.Events;
import com.wops.receiptsgo.autocomplete.trip.TripAutoCompleteField;
import com.wops.core.di.scopes.FragmentScope;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.model.factory.TripBuilderFactory;
import com.wops.receiptsgo.persistence.DatabaseHelper;
import com.wops.receiptsgo.persistence.PersistenceManager;
import com.wops.receiptsgo.persistence.database.controllers.impl.TripTableController;
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;
import com.wops.receiptsgo.settings.catalog.UserPreference;
import com.wops.receiptsgo.utils.FileUtils;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

@FragmentScope
public class TripCreateEditFragmentPresenter {

    private final TripCreateEditFragment fragment;
    private final Analytics analytics;
    private final TripTableController tripTableController;
    private final PersistenceManager persistenceManager;
    private final CompositeDisposable compositeDisposable;
    private int positionToRemoveOrAdd;

    @Inject
    public TripCreateEditFragmentPresenter(@NonNull TripCreateEditFragment fragment,
                                           @NonNull Analytics analytics,
                                           @NonNull TripTableController tripTableController,
                                           @NonNull PersistenceManager persistenceManager) {
        this.fragment = Preconditions.checkNotNull(fragment);
        this.analytics = Preconditions.checkNotNull(analytics);
        this.tripTableController = Preconditions.checkNotNull(tripTableController);
        this.persistenceManager = Preconditions.checkNotNull(persistenceManager);
        this.compositeDisposable = new CompositeDisposable();
    }

    public void subscribe() {
        compositeDisposable.add(fragment.getHideAutoCompleteVisibilityClick()
                .flatMap(autoCompleteClickEvent -> {
                    positionToRemoveOrAdd = autoCompleteClickEvent.getPosition();
                    if (autoCompleteClickEvent.getType() == TripAutoCompleteField.Name) {
                        return updateTrip(autoCompleteClickEvent.getItem().getFirstItem(),
                                new TripBuilderFactory(autoCompleteClickEvent.getItem().getFirstItem())
                                        .setNameHiddenFromAutoComplete(true)
                                        .build());
                    } else if (autoCompleteClickEvent.getType() == TripAutoCompleteField.Comment) {
                        return updateTrip(autoCompleteClickEvent.getItem().getFirstItem(),
                                new TripBuilderFactory(autoCompleteClickEvent.getItem().getFirstItem())
                                        .setCommentHiddenFromAutoComplete(true)
                                        .build());
                    } else if (autoCompleteClickEvent.getType() == TripAutoCompleteField.CostCenter) {
                        return updateTrip(autoCompleteClickEvent.getItem().getFirstItem(),
                                new TripBuilderFactory(autoCompleteClickEvent.getItem().getFirstItem())
                                        .setCostCenterHiddenFromAutoComplete(true)
                                        .build());
                    } else {
                        throw new UnsupportedOperationException("Unknown type: " + autoCompleteClickEvent.getType());
                    }
                })
                .subscribe(tripOptional -> {
                    if (tripOptional.isPresent()) {
                        fragment.removeValueFromAutoComplete(positionToRemoveOrAdd);
                    }
                }));

        compositeDisposable.add(fragment.getUnHideAutoCompleteVisibilityClick()
                .flatMap(autoCompleteClickEvent -> {
                    if (autoCompleteClickEvent.getType() == TripAutoCompleteField.Name) {
                        return updateTrip(autoCompleteClickEvent.getItem().getFirstItem(),
                                new TripBuilderFactory(autoCompleteClickEvent.getItem().getFirstItem())
                                        .setNameHiddenFromAutoComplete(false)
                                        .build());
                    } else if (autoCompleteClickEvent.getType() == TripAutoCompleteField.Comment) {
                        return updateTrip(autoCompleteClickEvent.getItem().getFirstItem(),
                                new TripBuilderFactory(autoCompleteClickEvent.getItem().getFirstItem())
                                        .setCommentHiddenFromAutoComplete(false)
                                        .build());
                    } else if (autoCompleteClickEvent.getType() == TripAutoCompleteField.CostCenter) {
                        return updateTrip(autoCompleteClickEvent.getItem().getFirstItem(),
                                new TripBuilderFactory(autoCompleteClickEvent.getItem().getFirstItem())
                                        .setCostCenterHiddenFromAutoComplete(false)
                                        .build());
                    } else {
                        throw new UnsupportedOperationException("Unknown type: " + autoCompleteClickEvent.getType());
                    }
                })
                .subscribe(tripOptional -> {
                    if (tripOptional.isPresent()) {
                        fragment.sendAutoCompleteUnHideEvent(positionToRemoveOrAdd);
                    } else {
                        fragment.displayAutoCompleteError();
                    }
                }));
    }

    public void unsubscribe() {
        compositeDisposable.clear();
    }

    public boolean checkTrip(String name, String startDateText, Date startDate,
                             String endDateText, Date endDate) {
        // Error Checking
        if (name.length() == 0 || startDateText.length() == 0 || endDateText.length() == 0) {
            fragment.showError(TripEditorErrors.MISSING_FIELD);
            return false;
        }
        if (startDate == null || endDate == null) {
            fragment.showError(TripEditorErrors.CALENDAR_ERROR);
            return false;
        }
        if (startDate.compareTo(endDate) > 0) {
            fragment.showError(TripEditorErrors.DURATION_ERROR);
            return false;
        }
        if (name.startsWith(" ")) {
            fragment.showError(TripEditorErrors.SPACE_ERROR);
            return false;
        }
        if (FileUtils.filenameContainsIllegalCharacter(name)) {
            fragment.showError(TripEditorErrors.ILLEGAL_CHAR_ERROR);
            return false;
        }

        final String trimmedName = name.trim();
        for (final Trip trip : fragment.getExistingTrips()) {
            if (trip.getName().equals(trimmedName)) {
                fragment.showError(TripEditorErrors.NON_UNIQUE_NAME);
                return false;
            }
        }

        return true;
    }

    public Trip saveTrip(String name, Date startDate, TimeZone startTimeZone, Date endDate, TimeZone endTimeZone, String defaultCurrency,
                         String comment, String costCenter) {

        File file = persistenceManager.getStorageManager().getFile(name);

        if (fragment.getEditableItem() == null) { // Insert
            analytics.record(Events.Reports.PersistNewReport);
            final Trip insertTrip = new TripBuilderFactory()
                    .setDirectory(file)
                    .setStartDate(startDate)
                    .setStartTimeZone(startTimeZone)
                    .setEndDate(endDate)
                    .setEndTimeZone(endTimeZone)
                    .setComment(comment)
                    .setCostCenter(costCenter)
                    .setDefaultCurrency(defaultCurrency)
                    .build();
            tripTableController.insert(insertTrip, new DatabaseOperationMetadata());
            return insertTrip;
        } else { // Update
            analytics.record(Events.Reports.PersistUpdateReport);
            final Trip updateTrip = new TripBuilderFactory(fragment.getEditableItem())
                    .setDirectory(file)
                    .setStartDate(startDate)
                    .setEndDate(endDate)
                    // TODO: Update trip timezones if date was changed
                    .setComment(comment)
                    .setCostCenter(costCenter)
                    .setDefaultCurrency(defaultCurrency)
                    .build();
            tripTableController.update(fragment.getEditableItem(), updateTrip, new DatabaseOperationMetadata());
            return updateTrip;
        }
    }

    public boolean isIncludeCostCenter() {
        return persistenceManager.getPreferenceManager().get(UserPreference.General.IncludeCostCenter);
    }

    public List<CharSequence> getCurrenciesList() {
        return persistenceManager.getDatabase().getCurrenciesList();
    }

    public boolean isEnableAutoCompleteSuggestions() {
        return persistenceManager.getPreferenceManager().get(UserPreference.Receipts.EnableAutoCompleteSuggestions);
    }

    public DatabaseHelper getDatabaseHelper() {
        return persistenceManager.getDatabase();
    }

    public int getDefaultTripDuration() {
        return persistenceManager.getPreferenceManager().get(UserPreference.General.DefaultReportDuration);
    }

    public String getDefaultCurrency() {
        return persistenceManager.getPreferenceManager().get(UserPreference.General.DefaultCurrency);
    }

    public String getDateSeparator() {
        return persistenceManager.getPreferenceManager().get(UserPreference.General.DateSeparator);
    }

    public Observable<Optional<Trip>> updateTrip(Trip oldTrip, Trip newTrip) {
        return tripTableController.update(oldTrip, newTrip, new DatabaseOperationMetadata());
    }
}
