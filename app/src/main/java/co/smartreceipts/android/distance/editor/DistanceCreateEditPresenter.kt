package co.smartreceipts.android.distance.editor

import co.smartreceipts.android.R
import co.smartreceipts.android.autocomplete.AutoCompletePresenter
import co.smartreceipts.android.di.scopes.FragmentScope
import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.utils.ModelUtils
import co.smartreceipts.android.widget.model.UiIndicator
import co.smartreceipts.android.widget.viper.BaseViperPresenter
import java.math.BigDecimal
import javax.inject.Inject

@FragmentScope
class DistanceCreateEditPresenter @Inject constructor(
    view: DistanceCreateEditView, interactor: DistanceCreateEditInteractor,
    private var distanceAutoCompletePresenter: AutoCompletePresenter<Distance>
) :
    BaseViperPresenter<DistanceCreateEditView, DistanceCreateEditInteractor>(view, interactor) {

    override fun subscribe() {

        distanceAutoCompletePresenter.subscribe()

        compositeDisposable.add(view.createDistanceClicks
            .distinctUntilChanged()
            .flatMap { interactor.createDistance(it) }
            .map { result ->
                when {
                    result.isPresent -> UiIndicator.success<Int>()
                    else -> UiIndicator.error<Int>(R.string.distance_insert_failed)
                }
            }
            .subscribe { view.present(it) })

        compositeDisposable.add(view.updateDistanceClicks
            .filter { view.editableItem != null }
            .distinctUntilChanged()
            .flatMap { interactor.updateDistance(view.editableItem!!, it) }
            .map { result ->
                when {
                    result.isPresent -> UiIndicator.success<Int>()
                    else -> UiIndicator.error<Int>(R.string.distance_update_failed)
                }
            }
            .subscribe { view.present(it) })

        compositeDisposable.add(view.deleteDistanceClicks
            .distinctUntilChanged()
            .doOnNext { interactor.deleteDistance(it) }
            .subscribe { view.present(UiIndicator.success()) })
    }

    override fun unsubscribe() {
        super.unsubscribe()

        distanceAutoCompletePresenter.unsubscribe()
    }

    fun getDefaultDistanceRate(): String {
        val defaultDistanceRate = interactor.getDefaultDistanceRate()
        return if (defaultDistanceRate > 0) {
            ModelUtils.getDecimalFormattedValue(BigDecimal.valueOf(defaultDistanceRate.toDouble()), Distance.RATE_PRECISION)
        } else {
            ""
        }
    }


}