package co.smartreceipts.android.images

import com.squareup.picasso.Picasso
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


/**
 * [Picasso] does some disk work when initializing the cache. We load it first in a background thread
 * to speed up this process
 */
class PicassoInitializer @Inject constructor(private val picasso: Picasso) {

    fun initialize() {
        Single.fromCallable {
                    return@fromCallable picasso
                }
                .observeOn(Schedulers.io())
                .subscribe()
    }
}