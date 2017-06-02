package co.smartreceipts.android.widget.tooltip.report.generate;

import com.hadisatrio.optional.Optional;

import javax.inject.Inject;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.tooltip.report.generate.data.GenerateInfoTooltipStorage;
import io.reactivex.Single;

public class GenerateInfoTooltipManager {

    /*
    Generate info tooltip should be shown under the following conditions:

    + If it was never previously dismissed -- GenerateInfoTooltipStorage
    + The user only has a single report
    + The user has one or more receipts in the report
    + The user has never created a report -- GenerateInfoTooltipStorage
    + There are no GoogleDriveSync errors to show (these should be the highest priority)
    */

    private final GenerateInfoTooltipStorage preferencesStorage;
    private final DatabaseHelper databaseHelper;


    @Inject
    public GenerateInfoTooltipManager(DatabaseHelper databaseHelper, GenerateInfoTooltipStorage storage) {
        this.databaseHelper = databaseHelper;
        this.preferencesStorage = storage;
    }

    public Single<Boolean> needToShowGenerateTooltip() {

        return databaseHelper.getTripsTable()
                .get()
                .<Optional<Trip>>map(trips -> {
                    if (trips.size() == 1) {
                        return Optional.of(trips.get(0));
                    } else {
                        return Optional.absent();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get) // here comes a single trip
                .flatMapSingle(trip -> databaseHelper.getReceiptsTable().get(trip))
                .map(receipts -> {
                    // tooltip wasn't dismissed, user has never generated report before and user has some receipts
                    return !preferencesStorage.wasTooltipDismissed() &&
                            !preferencesStorage.wasReportEverGenerated() &&
                            receipts.size() != 0;
                })
                .onErrorReturn(throwable -> false);
    }

    public void reportWasGenerated() {
        preferencesStorage.reportWasGenerated();
        Logger.debug(this, "Report was generated");
    }

    public void tooltipWasDismissed() {
        preferencesStorage.tooltipWasDismissed();
        Logger.debug(this, "Generate info tooltip was dismissed");
    }
}

