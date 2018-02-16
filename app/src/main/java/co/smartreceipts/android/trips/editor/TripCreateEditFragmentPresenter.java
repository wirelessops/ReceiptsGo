package co.smartreceipts.android.trips.editor;

import java.io.File;
import java.sql.Date;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.TripBuilderFactory;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.FileUtils;

@FragmentScope
public class TripCreateEditFragmentPresenter {

    @Inject
    TripCreateEditFragment fragment;
    @Inject
    Analytics analytics;
    @Inject
    TripTableController tripTableController;
    @Inject
    PersistenceManager persistenceManager;

    @Inject
    public TripCreateEditFragmentPresenter() {
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

        return true;
    }

    public Trip saveTrip(String name, Date startDate, TimeZone startTimeZone, Date endDate, TimeZone endTimeZone, String defaultCurrency,
                         String comment, String costCenter) {

        File file = persistenceManager.getStorageManager().getFile(name);

        if (fragment.getTrip() == null) { // Insert
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
            final Trip updateTrip = new TripBuilderFactory(fragment.getTrip())
                    .setDirectory(file)
                    .setStartDate(startDate)
                    .setEndDate(endDate)
                    // TODO: Update trip timezones iff date was changed
                    .setComment(comment)
                    .setCostCenter(costCenter)
                    .setDefaultCurrency(defaultCurrency)
                    .build();
            tripTableController.update(fragment.getTrip(), updateTrip, new DatabaseOperationMetadata());
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
}
