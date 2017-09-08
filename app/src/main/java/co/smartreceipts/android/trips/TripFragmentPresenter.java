package co.smartreceipts.android.trips;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.rating.AppRatingManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import wb.android.storage.StorageManager;

@FragmentScope
public class TripFragmentPresenter {

    @Inject
    TripFragment fragment;
    @Inject
    AppRatingManager appRatingManager;
    @Inject
    StorageManager storageManager;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public TripFragmentPresenter() {
    }

    public void subscribe() {
        compositeDisposable.add(appRatingManager.checkIfNeedToAskRating()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ratingPrompt -> {
                    if (ratingPrompt) {
                        fragment.showRatingTooltip();
                    }
                }));
    }

    public void unsubscribe() {
        compositeDisposable.clear();
    }

    public void dontShowRatingPrompt() {
        appRatingManager.dontShowRatingPromptAgain();
    }

    public boolean isExternalStorage() {
        return storageManager.isExternal();
    }

}
