package com.wops.receiptsgo.tooltip.image

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import com.wops.receiptsgo.R
import co.smartreceipts.core.di.scopes.FragmentScope
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.settings.catalog.UserPreference
import com.wops.receiptsgo.tooltip.TooltipController
import com.wops.receiptsgo.tooltip.TooltipView
import com.wops.receiptsgo.tooltip.image.data.ImageCroppingPreferenceStorage
import com.wops.receiptsgo.tooltip.model.TooltipInteraction
import com.wops.receiptsgo.tooltip.model.TooltipMetadata
import com.wops.receiptsgo.tooltip.model.TooltipType
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