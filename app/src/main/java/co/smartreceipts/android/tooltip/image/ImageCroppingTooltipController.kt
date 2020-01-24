package co.smartreceipts.android.tooltip.image

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import co.smartreceipts.android.R
import co.smartreceipts.core.di.scopes.FragmentScope
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.tooltip.TooltipController
import co.smartreceipts.android.tooltip.TooltipView
import co.smartreceipts.android.tooltip.image.data.ImageCroppingPreferenceStorage
import co.smartreceipts.android.tooltip.model.TooltipInteraction
import co.smartreceipts.android.tooltip.model.TooltipMetadata
import co.smartreceipts.android.tooltip.model.TooltipType
import co.smartreceipts.analytics.log.Logger
import com.hadisatrio.optional.Optional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * An implementation of the [TooltipController] contract to display a "Enable Cropping Yes|No" tooltip
 */
@FragmentScope
class ImageCroppingTooltipController @Inject constructor(
    private val context: Context,
    private val tooltipView: TooltipView,
    private val croppingPreferenceStorage: ImageCroppingPreferenceStorage,
    private val preferences: UserPreferenceManager
) : TooltipController {

    @UiThread
    override fun shouldDisplayTooltip(): Single<Optional<TooltipMetadata>> {

        return Observable.combineLatest(croppingPreferenceStorage.getCroppingScreenWasShown().toObservable(),
            croppingPreferenceStorage.getCroppingTooltipWasHandled().toObservable(),
            BiFunction <Boolean, Boolean, Optional<TooltipMetadata>> { cropScreenWasShown, cropTooltipWasHandled ->
                when {
                    cropScreenWasShown && !cropTooltipWasHandled ->
                        Optional.of(TooltipMetadata(TooltipType.ImageCropping, context.getString(R.string.pref_general_enable_crop_title)))
                    else -> Optional.absent()
                }
            })
            .lastOrError()
    }

    @AnyThread
    override fun handleTooltipInteraction(interaction: TooltipInteraction): Completable {
        return Completable.fromCallable {
            croppingPreferenceStorage.setCroppingTooltipWasHandled(true)

            when (interaction) {
                TooltipInteraction.YesButtonClick -> {
                    preferences.set(UserPreference.General.EnableCrop, true)
                    Logger.info(this, "User clicked 'Yes' on the image cropping tooltip")
                }
                TooltipInteraction.NoButtonClick -> {
                    preferences.set(UserPreference.General.EnableCrop, false)
                    Logger.info(this, "User clicked 'No' on the image cropping tooltip")
                }
                else -> Logger.warn(this, "Handling unknown tooltip interaction: {}", interaction)
            }
        }.subscribeOn(Schedulers.io())
    }

    @UiThread
    override fun consumeTooltipInteraction(): Consumer<TooltipInteraction> {
        return Consumer {
            tooltipView.hideTooltip()
        }
    }


}