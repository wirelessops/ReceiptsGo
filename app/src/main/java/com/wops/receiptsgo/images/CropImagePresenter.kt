package com.wops.receiptsgo.images

import android.graphics.Bitmap
import com.wops.receiptsgo.widget.model.UiIndicator
import com.wops.receiptsgo.widget.viper.BaseViperPresenter
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import javax.inject.Inject

class CropImagePresenter @Inject constructor(view: CropView, interactor: CropImageInteractor) :
    BaseViperPresenter<CropView, CropImageInteractor>(view, interactor) {

    override fun subscribe() {

        compositeDisposable.add(interactor.getImage(view.imageFile)
            .doOnSubscribe { view.present(UiIndicator.loading()) }
            .subscribe({ view.present(UiIndicator.success(it)) }, { view.present(UiIndicator.error()) })
        )

        compositeDisposable.add(view.getApplyCropClicks()
            .doOnNext { view.present(UiIndicator.loading()) }
            .firstElement()
            .flatMapCompletable { bitmap -> interactor.updateImage(view.imageFile, bitmap) }
            .subscribe({ view.finishCropView(true) }, {
                view.present(UiIndicator.error())
                view.finishCropView(false)
            })
        )

        compositeDisposable.add(
            view.rotateLeftClicks
                .compose(rotateImageAndUpdate(false))
                .subscribe({ view.present(UiIndicator.success(it)) }, { view.present(UiIndicator.error()) })
        )

        compositeDisposable.add(
            view.rotateRightClicks
                .compose(rotateImageAndUpdate(true))
                .subscribe({ view.present(UiIndicator.success(it)) }, { view.present(UiIndicator.error()) })
        )

        compositeDisposable.add(
            view.cropToggleClicks
                .subscribe { view.toggleCropMode() }
        )


    }

    private fun rotateImageAndUpdate(isRight: Boolean): ObservableTransformer<Any, Bitmap> {
        return ObservableTransformer {
            it.doOnNext { view.present(UiIndicator.loading()) }
                .flatMapSingle {
                    interactor.rotateImage(view.imageFile, isRight)
                        .flatMap { image ->
                            interactor.updateImage(view.imageFile, image)
                                .andThen(Single.just(image))
                        }
                }
        }
    }

}