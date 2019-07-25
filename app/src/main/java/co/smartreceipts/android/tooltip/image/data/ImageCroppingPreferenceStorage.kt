package co.smartreceipts.android.tooltip.image.data

import android.content.SharedPreferences
import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.utils.rx.RxSchedulers
import dagger.Lazy
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

@ApplicationScope
class ImageCroppingPreferenceStorage @Inject constructor(
    private val preferences: Lazy<SharedPreferences>,
    @Named(RxSchedulers.IO) private val scheduler: Scheduler
) : ImageCroppingTooltipStorage {

    companion object {
        private const val PREFERENCE_CROPPING_SCREEN_WAS_SHOWN = "Cropping screen was shown"
        private const val PREFERENCE_CROPPING_TOOLTIP_WAS_HANDLED = "Cropping tooltip was handled"
    }

    override fun getCroppingScreenWasShown(): Single<Boolean> =
        Single.fromCallable{preferences.get().getBoolean(PREFERENCE_CROPPING_SCREEN_WAS_SHOWN, false)}
            .subscribeOn(scheduler)

    override fun getCroppingTooltipWasHandled(): Single<Boolean> =
        Single.fromCallable{preferences.get().getBoolean(PREFERENCE_CROPPING_TOOLTIP_WAS_HANDLED, false)}
            .subscribeOn(scheduler)

    override fun setCroppingScreenWasShown(value: Boolean) {
        preferences.get().edit()
            .putBoolean(PREFERENCE_CROPPING_SCREEN_WAS_SHOWN, value)
            .apply()
    }

    override fun setCroppingTooltipWasHandled(value: Boolean) {
        preferences.get().edit()
            .putBoolean(PREFERENCE_CROPPING_TOOLTIP_WAS_HANDLED, value)
            .apply()
    }

}