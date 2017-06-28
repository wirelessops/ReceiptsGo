package co.smartreceipts.android.graphs;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.widget.viper.BasePresenter;

@FragmentScope
public class GraphsPresenter extends BasePresenter<GraphsView, GraphsInteractor> {

    private final UserPreferenceManager preferenceManager;
    private final DatabaseHelper databaseHelper;
    private Trip trip;

    @Inject
    public GraphsPresenter(GraphsView view, GraphsInteractor interactor, UserPreferenceManager preferences,
                           DatabaseHelper databaseHelper) {
        super(view, interactor);

        this.preferenceManager = preferences;
        this.databaseHelper = databaseHelper;
    }

    public void subscribe(Trip trip) {
        this.trip = Preconditions.checkNotNull(trip);

        subscribe();
    }

    @Override
    public void subscribe() {

        // TODO: 21.06.2017 we have some nuance with payment methods.
        // when this option is disabled - receipt has no payment method
        // if we turn it on - old receipts still has no payment method (until we edit it - unspecified pm)

        compositeDisposable.add(interactor.getSummationByCategories(trip)
                .subscribe(view::present));

        compositeDisposable.add(interactor.getSummationByReimbursment(trip)
                .subscribe(view::present));

        if (preferenceManager.get(UserPreference.Receipts.UsePaymentMethods)) {
            compositeDisposable.add(interactor.getSummationByPaymentMethod(trip)
                    .subscribe(view::present));
        }

        compositeDisposable.add(databaseHelper.getReceiptsTable()
                .get(trip)
                .subscribe(receipts -> view.showEmptyText(receipts.isEmpty())));

        compositeDisposable.add(interactor.getSummationByDate(trip)
                .subscribe(view::present));
    }
}
