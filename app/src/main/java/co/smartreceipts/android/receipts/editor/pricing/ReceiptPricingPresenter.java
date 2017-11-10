package co.smartreceipts.android.receipts.editor.pricing;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.widget.mvp.BasePresenter;
import io.reactivex.Observable;

/**
 * A default presenter implementation to manage displaying the current receipt price and tax values
 */
public class ReceiptPricingPresenter extends BasePresenter<ReceiptPricingView> {

    private final UserPreferenceManager userPreferenceManager;
    private final Receipt editableReceipt;
    private final Bundle savedInstanceState;

    public ReceiptPricingPresenter(@NonNull ReceiptPricingView view,
                                   @NonNull UserPreferenceManager userPreferenceManager,
                                   @Nullable Receipt editableReceipt,
                                   @Nullable Bundle savedInstanceState) {
        super(view);
        this.userPreferenceManager = Preconditions.checkNotNull(userPreferenceManager);
        this.editableReceipt = editableReceipt;
        this.savedInstanceState = savedInstanceState;
    }

    @Override
    public void subscribe() {
        compositeDisposable.add(Observable.just(userPreferenceManager.get(UserPreference.Receipts.IncludeTaxField))
                .subscribe(view.toggleReceiptTaxFieldVisibility()));

        compositeDisposable.add(Observable.just(Optional.ofNullable(editableReceipt))
                .filter(Optional::isPresent)
                .filter(ignored -> savedInstanceState == null)
                .map(receipt -> receipt.get().getPrice())
                .subscribe(view.displayReceiptPrice()));

        compositeDisposable.add(Observable.just(Optional.ofNullable(editableReceipt))
                .filter(Optional::isPresent)
                .filter(ignored -> savedInstanceState == null)
                .map(receipt -> receipt.get().getTax())
                .subscribe(view.displayReceiptTax()));
    }
}
