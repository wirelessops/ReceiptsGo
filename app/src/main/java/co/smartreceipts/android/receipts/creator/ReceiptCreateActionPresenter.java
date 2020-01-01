package co.smartreceipts.android.receipts.creator;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.core.di.scopes.FragmentScope;
import co.smartreceipts.core.utils.log.Logger;
import co.smartreceipts.android.widget.mvp.BasePresenter;
import co.smartreceipts.android.widget.mvp.Presenter;

/**
 * Provides an implementation of the {@link Presenter} contract for our {@link ReceiptCreateActionView},
 * so we can manage the process by which the user can create a new receipt via the camera, gallery
 * import, or plain text.
 */
@FragmentScope
public class ReceiptCreateActionPresenter extends BasePresenter<ReceiptCreateActionView> {

    private final Analytics analytics;

    @Inject
    public ReceiptCreateActionPresenter(@NonNull ReceiptCreateActionView view,
                                        @NonNull Analytics analytics) {
        super(view);
        this.analytics = Preconditions.checkNotNull(analytics);
    }

    @Override
    public void subscribe() {
        compositeDisposable.add(view.getCreateNewReceiptMenuButtonToggles()
                .subscribe(isOpen -> {
                    if (isOpen) {
                        view.displayReceiptCreationMenuOptions();
                    } else {
                        view.hideReceiptCreationMenuOptions();
                    }
                }));

        compositeDisposable.add(view.getCreateNewReceiptFromCameraButtonClicks()
                .doOnNext(ignored -> {
                    Logger.info(this, "Launching camera for new receipt");
                    analytics.record(Events.Receipts.AddPictureReceipt);
                })
                .subscribe(ignored -> view.createNewReceiptViaCamera()));

        compositeDisposable.add(view.getCreateNewReceiptFromPlainTextButtonClicks()
                .doOnNext(ignored -> {
                    Logger.info(this, "Launching new text receipt");
                    analytics.record(Events.Receipts.AddTextReceipt);
                })
                .subscribe(ignored -> view.createNewReceiptViaPlainText()));

        compositeDisposable.add(view.getCreateNewReceiptFromImportedFileButtonClicks()
                .doOnNext(ignored -> {
                    Logger.info(this, "Launching import intent for new receipt");
                    analytics.record(Events.Receipts.ImportPictureReceipt);
                })
                .subscribe(ignored -> view.createNewReceiptViaFileImport()));
    }

}
