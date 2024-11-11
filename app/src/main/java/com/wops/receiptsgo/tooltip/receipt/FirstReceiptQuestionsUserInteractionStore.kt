package com.wops.receiptsgo.tooltip.receipt

import android.content.SharedPreferences
import co.smartreceipts.core.di.scopes.ApplicationScope
import com.wops.receiptsgo.utils.rx.RxSchedulers
import dagger.Lazy
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

@ApplicationScope
class FirstReceiptQuestionsUserInteractionStore @Inject constructor(private val preferences: Lazy<SharedPreferences>,
                                                                    @Named(RxSchedulers.IO) private val scheduler: Scheduler) {

    fun hasUserInteractionWithTaxesQuestionOccurred(): Single<Boolean> {
        return Single.fromCallable { preferences.get().getBoolean(KEY_TAXES_QUESTION, false) }
                .subscribeOn(scheduler)
    }

    fun hasUserInteractionWithPaymentMethodsQuestionOccurred(): Single<Boolean> {
        return Single.fromCallable { preferences.get().getBoolean(KEY_PAYMENT_METHODS_QUESTION, false) }
                .subscribeOn(scheduler)
    }

    fun hasUserInteractionWithSecondTaxHintOccurred(): Single<Boolean> {
        return Single.fromCallable { preferences.get().getBoolean(KEY_SECOND_TAX_HINT, false) }
            .subscribeOn(scheduler)
    }

    fun setInteractionWithTaxesQuestionHasOccurred(value: Boolean) {
        preferences.get().edit().putBoolean(KEY_TAXES_QUESTION, value).apply()
    }

    fun setInteractionWithPaymentMethodsQuestionHasOccurred(value: Boolean) {
        preferences.get().edit().putBoolean(KEY_PAYMENT_METHODS_QUESTION, value).apply()
    }

    fun setInteractionWithSecondTaxHintOccured(value: Boolean) {
        preferences.get().edit().putBoolean(KEY_SECOND_TAX_HINT, value).apply()
    }

    companion object {
        private const val KEY_TAXES_QUESTION = "user_interacted_with_taxes_question"
        private const val KEY_PAYMENT_METHODS_QUESTION = "user_interacted_with_payment_methods_question"
        private const val KEY_SECOND_TAX_HINT = "user_interacted_with_second_tax_hint"
    }
}
