package com.wops.receiptsgo.receipts.editor.pricing

import android.os.Bundle
import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.settings.catalog.UserPreference
import com.wops.receiptsgo.widget.mvp.BasePresenter
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

        compositeDisposable.add(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTax2Field)
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .subscribe(view.toggleReceiptTax2FieldVisibility()))

        compositeDisposable.add(userPreferenceManager.userPreferenceChangeStream
                .subscribeOn(ioScheduler)
                .filter { it == UserPreference.Receipts.IncludeTaxField }
                .flatMapSingle { _ -> userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTaxField) }
                .observeOn(mainScheduler)
                .subscribe(view.toggleReceiptTaxFieldVisibility()))

        compositeDisposable.add(userPreferenceManager.userPreferenceChangeStream
            .subscribeOn(ioScheduler)
            .filter { it == UserPreference.Receipts.IncludeTax2Field }
            .flatMapSingle { _ -> userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTax2Field) }
            .observeOn(mainScheduler)
            .subscribe(view.toggleReceiptTax2FieldVisibility()))

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

        compositeDisposable.add(Observable.just(Optional.ofNullable<Receipt>(editableReceipt))
            .filter { it.isPresent }
            .filter { _ -> savedInstanceState == null }
            .map { receipt -> receipt.get().tax2 }
            .subscribe(view.displayReceiptTax2()))
    }
}
