package com.wops.receiptsgo.receipts.editor.toolbar

import android.content.Context
import com.wops.receiptsgo.R
import co.smartreceipts.core.di.scopes.ApplicationScope
import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.persistence.DatabaseHelper
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.settings.catalog.UserPreference
import co.smartreceipts.analytics.log.Logger
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


/**
 * Allows us to perform the various business logic processing to configure the receipts editor toolbar
 */
@ApplicationScope
class ReceiptsEditorToolbarInteractor(private val context: Context,
                                      private val databaseHelper: DatabaseHelper,
                                      private val preferenceManager: UserPreferenceManager,
                                      private val backgroundScheduler: Scheduler) {


    @Inject
    constructor(context: Context,
                databaseHelper: DatabaseHelper,
                preferenceManager: UserPreferenceManager) : this(context, databaseHelper, preferenceManager, Schedulers.io())

    /**
     * Fetches the title for the receipts edit page as determined by the user preference and if we're
     * currently editing a receipt or not
     *
     * @param [receiptToEdit] the [Receipt] that we're editing (or null if not)
     *
     * @return a [Single], which will emit a [String] containing the editor toolbar title
     */
    fun getReceiptTitle(receiptToEdit: Receipt?) : Single<String> {
        return preferenceManager.getSingle(UserPreference.Receipts.ShowReceiptID)
                .subscribeOn(backgroundScheduler)
                .flatMap { showReceiptId ->
                    if (showReceiptId) {
                        if (receiptToEdit != null) {
                            Single.just(context.getString(R.string.DIALOG_RECEIPTMENU_TITLE_EDIT_ID, receiptToEdit.id))
                        } else {
                            databaseHelper.nextReceiptAutoIncrementIdHelper
                                    .doOnSuccess {
                                        Logger.debug(this, "Determined next receipt id as {}")
                                    }
                                    .map {
                                        context.getString(R.string.DIALOG_RECEIPTMENU_TITLE_NEW_ID, it)
                                    }
                        }
                    }
                    else {
                        if (receiptToEdit != null) {
                            Single.just(context.getString(R.string.DIALOG_RECEIPTMENU_TITLE_EDIT))
                        } else {
                            Single.just(context.getString(R.string.DIALOG_RECEIPTMENU_TITLE_NEW))
                        }
                    }
                }

    }

}