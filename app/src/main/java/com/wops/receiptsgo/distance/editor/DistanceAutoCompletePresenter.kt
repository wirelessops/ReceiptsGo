package com.wops.receiptsgo.distance.editor

import com.wops.receiptsgo.autocomplete.distance.DistanceAutoCompleteField
import com.wops.receiptsgo.model.factory.DistanceBuilderFactory
import com.wops.receiptsgo.widget.viper.BaseViperPresenter
import co.smartreceipts.core.di.scopes.FragmentScope
import javax.inject.Inject

@FragmentScope
class DistanceAutoCompletePresenter @Inject constructor(
    view: DistanceCreateEditView, interactor: DistanceCreateEditInteractor
) :
    BaseViperPresenter<DistanceCreateEditView, DistanceCreateEditInteractor>(view, interactor) {

    private var positionToRemoveOrAdd: Int = 0

    override fun subscribe() {

        compositeDisposable.add(view.hideAutoCompleteVisibilityClick
                .flatMap { t ->
                    positionToRemoveOrAdd = t.position
                    when (t.type) {
                        DistanceAutoCompleteField.Location -> interactor.updateDistance(t.item!!.firstItem, DistanceBuilderFactory(t.item.firstItem)
                                .setLocationHiddenFromAutoComplete(true)
                                .build())
                        DistanceAutoCompleteField.Comment -> interactor.updateDistance(t.item!!.firstItem, DistanceBuilderFactory(t.item.firstItem)
                                .setCommentHiddenFromAutoComplete(true)
                                .build())
                        else -> throw UnsupportedOperationException("Unknown type: " + t.type)
                    }
                }
                .subscribe {
                    if (it.isPresent) {
                        view.removeValueFromAutoComplete(positionToRemoveOrAdd)
                    }
                }
        )

        compositeDisposable.add(view.unHideAutoCompleteVisibilityClick
                .flatMap { t ->
                    when (t.type) {
                        DistanceAutoCompleteField.Location -> interactor.updateDistance(t.item!!.firstItem, DistanceBuilderFactory(t.item.firstItem)
                                .setLocationHiddenFromAutoComplete(false)
                                .build())
                        DistanceAutoCompleteField.Comment -> interactor.updateDistance(t.item!!.firstItem, DistanceBuilderFactory(t.item.firstItem)
                                .setCommentHiddenFromAutoComplete(false)
                                .build())
                        else -> throw UnsupportedOperationException("Unknown type: " + t.type)
                    }
                }
                .subscribe {
                    if (it.isPresent) {
                        view.sendAutoCompleteUnHideEvent(positionToRemoveOrAdd)
                    } else {
                        view.displayAutoCompleteError()
                    }
                }
        )
    }

}