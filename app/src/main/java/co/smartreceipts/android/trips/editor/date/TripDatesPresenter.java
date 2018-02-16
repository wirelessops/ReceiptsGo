package co.smartreceipts.android.trips.editor.date;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.sql.Date;
import java.util.concurrent.TimeUnit;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.widget.mvp.BasePresenter;

public class TripDatesPresenter extends BasePresenter<TripDateView> {

    private final UserPreferenceManager userPreferenceManager;
    private final Trip editableTrip;

    public TripDatesPresenter(@NonNull TripDateView view, @NonNull UserPreferenceManager userPreferenceManager, @Nullable Trip editableTrip) {
        super(view);
        this.userPreferenceManager = Preconditions.checkNotNull(userPreferenceManager);
        this.editableTrip = editableTrip;
    }


    @Override
    public void subscribe() {
        if (editableTrip == null) {
            compositeDisposable.add(view.getStartDateChanges()
                    .map(date -> new Date(date.getTime() + TimeUnit.DAYS.toMillis(userPreferenceManager.get(UserPreference.General.DefaultReportDuration))))
                    .subscribe(view.displayEndDate()));
        }
    }
}
