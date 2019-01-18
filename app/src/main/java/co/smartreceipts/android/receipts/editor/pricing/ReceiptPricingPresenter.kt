package co.smartreceipts.android.receipts.editor.pricing

import android.os.Bundle
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.widget.mvp.BasePresenter
import com.hadisatrio.optional.Optional
import io.reactivex.Observable
import io.reactivex.Scheduler

/**
 * A default presenter implementation to manage displaying the current receipt price and tax values
 */
class ReceiptPricingPresenter(view: ReceiptPricingView,
                              private val userPreferenceManager: UserPreferenceManager,
                              private val editableReceipt: Receipt?,
                              private val savedInstanceState: Bundle?,
                              private val ioScheduler: Scheduler,
                              private val mainScheduler: Scheduler) : BasePresenter<ReceiptPricingView>(view) {

    override fun subscribe() {
        compositeDisposable.add(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTaxField)
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe(view.toggleReceiptTaxFieldVisibility()))

        compositeDisposable.add(userPreferenceManager.userPreferenceChangeStream
                .subscribeOn(ioScheduler)
                .filter { it == UserPreference.Receipts.IncludeTaxField }
                .flatMapSingle { _ -> userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTaxField) }
                .observeOn(mainScheduler)
                .subscribe(view.toggleReceiptTaxFieldVisibility()))

        compositeDisposable.add(Observable.just(Optional.ofNullable<Receipt>(editableReceipt))
                .filter { it.isPresent }
                .filter { _ -> savedInstanceState == null }
                .map { receipt -> receipt.get().price }
                .subscribe(view.displayReceiptPrice()))

        compositeDisposable.add(Observable.just(Optional.ofNullable<Receipt>(editableReceipt))
                .filter { it.isPresent }
                .filter { _ -> savedInstanceState == null }
                .map { receipt -> receipt.get().tax }
                .subscribe(view.displayReceiptTax()))
    }
}
