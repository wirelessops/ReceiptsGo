package co.smartreceipts.android.distance.editor

import co.smartreceipts.android.autocomplete.distance.DistanceAutoCompleteField
import co.smartreceipts.android.model.factory.DistanceBuilderFactory
import co.smartreceipts.android.widget.viper.BaseViperPresenter
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
                        DistanceAutoCompleteField.Location -> interactor.updateDistance(t.item.firstItem, DistanceBuilderFactory(t.item.firstItem)
                                .setLocationHiddenFromAutoComplete(true)
                                .build())
                        DistanceAutoCompleteField.Comment -> interactor.updateDistance(t.item.firstItem, DistanceBuilderFactory(t.item.firstItem)
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
                        DistanceAutoCompleteField.Location -> interactor.updateDistance(t.item.firstItem, DistanceBuilderFactory(t.item.firstItem)
                                .setLocationHiddenFromAutoComplete(false)
                                .build())
                        DistanceAutoCompleteField.Comment -> interactor.updateDistance(t.item.firstItem, DistanceBuilderFactory(t.item.firstItem)
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