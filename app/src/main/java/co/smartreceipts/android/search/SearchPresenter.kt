package co.smartreceipts.android.search

import co.smartreceipts.android.widget.viper.BaseViperPresenter
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SearchPresenter(view: SearchView, interactor: SearchInteractor, private val debounceScheduler: Scheduler, private val observeOnScheduler: Scheduler) :
    BaseViperPresenter<SearchView, SearchInteractor>(view, interactor) {

    @Inject
    constructor(view: SearchView, interactor: SearchInteractor) : this(view, interactor, Schedulers.computation(), AndroidSchedulers.mainThread())

    override fun subscribe() {

        compositeDisposable.add(
            view.inputChanges
                .filter { it.isNotBlank() }
                .debounce(250, TimeUnit.MILLISECONDS, debounceScheduler)
                .map { it.trim() }
                .observeOn(observeOnScheduler)
                .flatMapSingle { input: CharSequence -> interactor.getSearchResults(input.toString()) }
                .subscribe { searchResults -> view.presentSearchResults(searchResults) }
        )
    }
}