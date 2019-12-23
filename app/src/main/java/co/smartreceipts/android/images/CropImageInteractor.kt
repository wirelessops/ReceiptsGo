package co.smartreceipts.android.images

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import co.smartreceipts.android.utils.ImageUtils
import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.core.utils.log.Logger
import com.squareup.picasso.Picasso
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject

@ApplicationScope
class CropImageInteractor(
    private val subscribeOnScheduler: Scheduler = Schedulers.io(),
    private val observeOnScheduler: Scheduler = AndroidSchedulers.mainThread()
) {
    @Inject
    constructor() : this(Schedulers.io(), AndroidSchedulers.mainThread())


    fun getImage(imageFile: File): Observable<Bitmap> {

        return Observable.just(imageFile)
            .map { BitmapFactory.decodeFile(it.absolutePath) }
            .subscribeOn(subscribeOnScheduler)
            .observeOn(observeOnScheduler)
    }

    fun updateImage(file: File, bitmap: Bitmap): Completable {

        return Completable.create { emitter ->

            val result= ImageUtils.writeImageToFile(bitmap, file)
                Picasso.get().invalidate(file)

            if (result) {
                emitter.onComplete()
            } else {
                Logger.error(this, "Failed to save bitmap to file")
                emitter.onError(Exception("Failed to save bitmap to file"))
            }

        }
            .subscribeOn(subscribeOnScheduler)
            .observeOn(observeOnScheduler)

    }

    fun rotateImage(file: File, isRight: Boolean): Single<Bitmap> {
        return Single.create<Bitmap> { emitter ->

                val bitmap = ImageUtils.getImageFromFile(file)

                val orientation = when {
                    isRight -> ExifInterface.ORIENTATION_ROTATE_90 // turn right
                    else -> ExifInterface.ORIENTATION_ROTATE_270 // turn left
                }

                when {
                    bitmap!= null -> emitter.onSuccess(ImageUtils.rotateBitmap(bitmap, orientation))
                    else -> emitter.onError(Exception("Failed to get bitmap"))
                }

        }
            .subscribeOn(subscribeOnScheduler)
            .observeOn(observeOnScheduler)
    }

}