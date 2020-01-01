package co.smartreceipts.android.keyboard.decimal

import android.content.Context
import android.provider.Settings
import co.smartreceipts.core.di.scopes.FragmentScope
import co.smartreceipts.core.utils.log.Logger
import co.smartreceipts.android.widget.mvp.BasePresenter
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.DecimalFormatSymbols
import javax.inject.Inject


/**
 * Samsung decided not to include a decimal separator character in their default keyboard when
 * inputType=numberDecimal is set, and they seem rather disinclined to fix it. In order to
 * address this, I've created this utility class, which attempts to toggle a special "decimal
 * separator button", which will allow these users to input the separator character
 */
@FragmentScope
class SamsungDecimalInputPresenter(view: SamsungDecimalInputView,
                                   private val context: Context,
                                   private val scheduler: Scheduler) : BasePresenter<SamsungDecimalInputView>(view) {

    @Inject
    constructor(view: SamsungDecimalInputView, context: Context) : this(view, context, Schedulers.computation())

    override fun subscribe() {
        // Check if we're using the Samsung keyboard to determine visibility
        compositeDisposable.add(Single.fromCallable {
                    // We scroll through our list of known package identifiers for the Samsung keyboard, trying to set the right one
                    val keyboardPackage = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
                    SAMSUNG_PACKAGE_IDENTIFIERS.forEach {
                        if (keyboardPackage.contains(it)) {
                            Logger.info(this, "Discovered Samsung keyboard with package: {}. Showing decimal button", keyboardPackage)
                            return@fromCallable true
                        }
                    }
                    return@fromCallable false
                }
                .subscribeOn(scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { success, _ ->
                    if (success) {
                        view.showSamsungDecimalInputView(getDecimalSeparator())
                    } else {
                        view.hideSamsungDecimalInputView()
                    }
                })

        // Relay all decimal clicks as appropriate
        compositeDisposable.add(view.getClickStream()
                .map { _ -> getDecimalSeparator() }
                .subscribe { view.appendDecimalSeparatorToFocusedVied(it) })
    }

    private fun getDecimalSeparator() : String = DecimalFormatSymbols.getInstance().decimalSeparator.toString()

    companion object {

        private val SAMSUNG_PACKAGE_IDENTIFIERS = listOf("samsung", "sec")



    }
}