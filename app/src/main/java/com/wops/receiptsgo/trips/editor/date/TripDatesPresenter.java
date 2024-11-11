package com.wops.receiptsgo.trips.editor.date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.sql.Date;
import java.util.concurrent.TimeUnit;

import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.settings.catalog.UserPreference;
import com.wops.receiptsgo.widget.mvp.BasePresenter;

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
