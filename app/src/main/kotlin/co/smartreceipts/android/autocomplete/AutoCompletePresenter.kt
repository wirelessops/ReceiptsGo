package co.smartreceipts.android.autocomplete

import co.smartreceipts.android.di.scopes.FragmentScope
import co.smartreceipts.android.utils.log.Logger
import co.smartreceipts.android.widget.mvp.BasePresenter
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * Manages the process of presenting auto-completion logic to a [AutoCompleteView]
 */
@FragmentScope
class AutoCompletePresenter<Type>(view: AutoCompleteView<Type>,
                                  private val interactor: AutoCompleteInteractor<Type>,
                                  private val mainThreadScheduler: Scheduler) : BasePresenter<AutoCompleteView<Type>>(view) {

    @Inject
    constructor(view: AutoCompleteView<Type>,
                interactor: AutoCompleteInteractor<Type>) : this(view, interactor, AndroidSchedulers.mainThread())

    override fun subscribe() {
        if (!view.isInEditingMode()) {
            interactor.supportedAutoCompleteFields.forEach { autoCompleteField ->
                compositeDisposable.add(view.getTextChangeStream(autoCompleteField)
                        .flatMapMaybe { inputText ->
                            interactor.getAutoCompleteResults(autoCompleteField, inputText)
                        }
                        .observeOn(mainThreadScheduler)
                        .subscribe{
                            view.displayAutoCompleteResults(autoCompleteField, it)
                        })
            }
        }
    }

}