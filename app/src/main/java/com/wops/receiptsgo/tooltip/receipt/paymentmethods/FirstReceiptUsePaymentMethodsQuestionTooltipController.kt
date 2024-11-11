package com.wops.receiptsgo.tooltip.receipt.paymentmethods

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import co.smartreceipts.analytics.Analytics
import co.smartreceipts.analytics.events.Events
import co.smartreceipts.analytics.log.Logger
import com.wops.receiptsgo.R
import com.wops.receiptsgo.columns.ordering.CsvColumnsOrderer
import com.wops.receiptsgo.columns.ordering.PdfColumnsOrderer
import com.wops.receiptsgo.model.impl.columns.receipts.ReceiptColumnDefinitions
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.settings.catalog.UserPreference
import com.wops.receiptsgo.tooltip.TooltipController
import com.wops.receiptsgo.tooltip.TooltipView
import com.wops.receiptsgo.tooltip.model.TooltipInteraction
import com.wops.receiptsgo.tooltip.model.TooltipMetadata
import com.wops.receiptsgo.tooltip.model.TooltipType
import com.wops.receiptsgo.tooltip.receipt.FirstReceiptQuestionsUserInteractionStore
import com.wops.receiptsgo.utils.rx.RxSchedulers
import co.smartreceipts.core.di.scopes.FragmentScope
import com.hadisatrio.optional.Optional
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.Consumer
import javax.inject.Inject
import javax.inject.Named

/**
 * An implementation of the [TooltipController] contract, which presents a question to our users
 * about if they wish to enable the payment method tracking field (as this is not otherwise obvious
 * to new users, since it is hidden in the settings menu).
 */
@FragmentScope
class FirstReceiptUsePaymentMethodsQuestionTooltipController @Inject constructor(private val context: Context,
                                                                                 private val tooltipView: TooltipView,
                                                                                 private val store: FirstReceiptQuestionsUserInteractionStore,
                                                                                 private val userPreferenceManager: UserPreferenceManager,
                                                                                 private val pdfColumnsOrderer: PdfColumnsOrderer,
                                                                                 private val csvColumnsOrderer: CsvColumnsOrderer,
                                                                                 private val analytics: Analytics,
                                                                                 @Named(RxSchedulers.IO) private val scheduler: Scheduler) : TooltipController {

    @UiThread
    override fun shouldDisplayTooltip(): Single<Optional<TooltipMetadata>> {
        return store.hasUserInteractionWithPaymentMethodsQuestionOccurred()
                .subscribeOn(scheduler)
                .map { hasUserInteractionOccurred ->
                    if (hasUserInteractionOccurred) {
                        Logger.debug(this, "This user has interacted with the payment methods tracking question tooltip before. Ignoring.")
                        return@map Optional.absent<TooltipMetadata>()
                    } else {
                        Logger.debug(this, "The user has not seen the payment methods tracking question before. Displaying...")
                        return@map Optional.of(newTooltipMetadata())
                    }
                }
    }

    @AnyThread
    override fun handleTooltipInteraction(interaction: TooltipInteraction): Completable {
        return Single.just(interaction)
                .subscribeOn(scheduler)
                .doOnSuccess {
                    Logger.info(this, "User interacted with the payment methods question: {}", interaction)
                }
                .flatMapCompletable {
                    when (interaction) {
                        TooltipInteraction.YesButtonClick -> {
                            Completable.fromAction {
                                userPreferenceManager[UserPreference.Receipts.UsePaymentMethods] = true
                                store.setInteractionWithPaymentMethodsQuestionHasOccurred(true)
                                analytics.record(Events.Informational.ClickedPaymentMethodsQuestionTipYes)
                            }
                            .andThen(pdfColumnsOrderer.insertColumnAfter(ReceiptColumnDefinitions.ActualDefinition.PAYMENT_METHOD, ReceiptColumnDefinitions.ActualDefinition.CATEGORY_CODE))
                            .andThen(csvColumnsOrderer.insertColumnAfter(ReceiptColumnDefinitions.ActualDefinition.PAYMENT_METHOD, ReceiptColumnDefinitions.ActualDefinition.CATEGORY_CODE))
                        }
                        TooltipInteraction.NoButtonClick -> Completable.fromAction {
                            userPreferenceManager[UserPreference.Receipts.UsePaymentMethods] = false
                            store.setInteractionWithPaymentMethodsQuestionHasOccurred(true)
                            analytics.record(Events.Informational.ClickedPaymentMethodsQuestionTipNo)
                        }
                        TooltipInteraction.CloseCancelButtonClick -> Completable.fromAction {
                            store.setInteractionWithPaymentMethodsQuestionHasOccurred(true)
                        }
                        else -> Completable.complete()
                    }
                }
                .doOnError {
                    Logger.warn(this, "Failed to handle tooltip interaction for the payment methods question. Failing silently")
                }
                .onErrorComplete()
    }

    @UiThread
    override fun consumeTooltipInteraction(): Consumer<TooltipInteraction> {
        return Consumer {
            if (it == TooltipInteraction.CloseCancelButtonClick || it == TooltipInteraction.YesButtonClick || it == TooltipInteraction.NoButtonClick) {
                tooltipView.hideTooltip()
            }
        }
    }

    private fun newTooltipMetadata() : TooltipMetadata {
        return TooltipMetadata(TooltipType.FirstReceiptUsePaymentMethodsQuestion, context.getString(R.string.pref_receipt_use_payment_methods_title))
    }

}
