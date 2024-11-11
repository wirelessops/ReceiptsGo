package com.wops.receiptsgo.receipts.editor.toolbar

import co.smartreceipts.core.di.scopes.FragmentScope
import com.wops.receiptsgo.editor.Editor
import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.widget.mvp.BasePresenter
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject


/**
 * A simple presenter for displaying the toolbar title for the receipt editor page
 */
@FragmentScope
class ReceiptsEditorToolbarPresenter(view: ReceiptsEditorToolbarView,
                                     private val editor: Editor<Receipt>,
                                     private val interactor: ReceiptsEditorToolbarInteractor,
                                     private val androidScheduler: Scheduler) : BasePresenter<ReceiptsEditorToolbarView>(view) {

    @Inject
    constructor(view: ReceiptsEditorToolbarView,
                editor: Editor<Receipt>,
                interactor: ReceiptsEditorToolbarInteractor) : this(view, editor, interactor, AndroidSchedulers.mainThread())

    override fun subscribe() {
        compositeDisposable.add(interactor.getReceiptTitle(editor.editableItem)
                .observeOn(androidScheduler)
                .subscribe { title ->
                    view.displayTitle(title)
                })
    }
}