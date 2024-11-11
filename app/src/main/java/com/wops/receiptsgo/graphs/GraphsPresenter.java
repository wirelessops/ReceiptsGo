package com.wops.receiptsgo.graphs;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import com.wops.core.di.scopes.FragmentScope;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.settings.catalog.UserPreference;
import com.wops.receiptsgo.widget.viper.BaseViperPresenter;

@FragmentScope
public class GraphsPresenter extends BaseViperPresenter<GraphsView, GraphsInteractor> {

    private final UserPreferenceManager preferenceManager;
    private final DatabaseAssistant databaseAssistant;
    private Trip trip;

    @Inject
    public GraphsPresenter(GraphsView view, GraphsInteractor interactor, UserPreferenceManager preferences,
                           DatabaseAssistant databaseAssistant) {
        super(view, interactor);

        this.preferenceManager = preferences;
        this.databaseAssistant = databaseAssistant;
    }

    public void subscribe(Trip trip) {
        this.trip = Preconditions.checkNotNull(trip);

        subscribe();
    }

    @Override
    public void subscribe() {

        if (trip == null) {
            throw new IllegalStateException("Use subscribe(trip) method to subscribe");
        }

        compositeDisposable.add(interactor.getSummationByCategories(trip)
                .subscribe(view::present));

        compositeDisposable.add(interactor.getSummationByReimbursement(trip)
                .subscribe(view::present));

        if (preferenceManager.get(UserPreference.Receipts.UsePaymentMethods)) {
            compositeDisposable.add(interactor.getSummationByPaymentMethod(trip)
                    .subscribe(view::present));
        }

        compositeDisposable.add(databaseAssistant.isReceiptsTableEmpty(trip)
                .subscribe(view::showEmptyText));

        compositeDisposable.add(interactor.getSummationByDate(trip)
                .subscribe(view::present));
    }
}
