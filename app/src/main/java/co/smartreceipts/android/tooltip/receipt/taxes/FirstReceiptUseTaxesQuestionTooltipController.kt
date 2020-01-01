package co.smartreceipts.android.tooltip.receipt.taxes

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import co.smartreceipts.android.R
import com.hadisatrio.optional.Optional

import co.smartreceipts.android.analytics.Analytics
import co.smartreceipts.android.analytics.events.Events
import co.smartreceipts.android.columns.ordering.CsvColumnsOrderer
import co.smartreceipts.android.columns.ordering.PdfColumnsOrderer
import co.smartreceipts.core.di.scopes.FragmentScope
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.tooltip.TooltipView
import co.smartreceipts.android.tooltip.TooltipController
import co.smartreceipts.android.tooltip.model.TooltipType
import co.smartreceipts.android.tooltip.model.TooltipInteraction
import co.smartreceipts.android.tooltip.model.TooltipMetadata
import co.smartreceipts.android.tooltip.receipt.FirstReceiptQuestionsUserInteractionStore
import co.smartreceipts.core.utils.log.Logger
import co.smartreceipts.android.utils.rx.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.Consumer
import javax.inject.Inject
import javax.inject.Named

/**
 * An implementation of the [TooltipController] contract, which presents a question to our users
 * about if they wish to enable the tax tracking field (as this is not otherwise obvious to new
 * users, since it is hidden in the settings menu).
 */
@FragmentScope
class FirstReceiptUseTaxesQuestionTooltipController @Inject constructor(private val context: Context,
                                                                        private val tooltipView: TooltipView,
                                                                        private val store: FirstReceiptQuestionsUserInteractionStore,
                                                                        private val userPreferenceManager: UserPreferenceManager,
                                                                        private val pdfColumnsOrderer: PdfColumnsOrderer,
                                                                        private val csvColumnsOrderer: CsvColumnsOrderer,
                                                                        private val analytics: Analytics,
                                                                        @Named(RxSchedulers.IO) private val scheduler: Scheduler) : TooltipController {

    @UiThread
    override fun shouldDisplayTooltip(): Single<Optional<TooltipMetadata>> {
        return store.hasUserInteractionWithTaxesQuestionOccurred()
                .subscribeOn(scheduler)
                .map { hasUserInteractionOccurred ->
                    if (hasUserInteractionOccurred) {
                        Logger.debug(this, "This user has interacted with the tax tracking question tooltip before. Ignoring.")
                        return@map Optional.absent<TooltipMetadata>()
                    } else {
                        Logger.debug(this, "The user has not see the tax tracking question before. Displaying...")
                        return@map Optional.of(newTooltipMetadata())
                    }
                }
    }

    @AnyThread
    override fun handleTooltipInteraction(interaction: TooltipInteraction): Completable {
        return Single.just(interaction)
                .subscribeOn(scheduler)
                .doOnSuccess {
                    Logger.info(this, "User interacted with the taxes question: {}", interaction)
                }
                .flatMapCompletable {
                    when (interaction) {
                        TooltipInteraction.YesButtonClick -> {
                            Completable.fromAction {
                                userPreferenceManager[UserPreference.Receipts.IncludeTaxField] = true
                                store.setInteractionWithTaxesQuestionHasOccurred(true)
                                analytics.record(Events.Informational.ClickedTaxQuestionTipYes)
                            }
                            .andThen(pdfColumnsOrderer.insertColumnAfter(ReceiptColumnDefinitions.ActualDefinition.TAX, ReceiptColumnDefinitions.ActualDefinition.PRICE))
                            .andThen(csvColumnsOrderer.insertColumnAfter(ReceiptColumnDefinitions.ActualDefinition.TAX, ReceiptColumnDefinitions.ActualDefinition.PRICE))
                        }
                        TooltipInteraction.NoButtonClick -> Completable.fromAction {
                            userPreferenceManager[UserPreference.Receipts.IncludeTaxField] = false
                            store.setInteractionWithTaxesQuestionHasOccurred(true)
                            analytics.record(Events.Informational.ClickedTaxQuestionTipNo)
                        }
                        TooltipInteraction.CloseCancelButtonClick -> Completable.fromAction {
                            store.setInteractionWithTaxesQuestionHasOccurred(true)
                        }
                        else -> Completable.complete()
                    }
                }
                .doOnError {
                    Logger.warn(this, "Failed to handle tooltip interaction for the taxes question. Failing silently")
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
        return TooltipMetadata(TooltipType.FirstReceiptUseTaxesQuestion, context.getString(R.string.pref_receipt_include_tax_field_title))
    }

}
