package co.smartreceipts.android.graphs;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.widget.viper.BaseViperPresenter;

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
