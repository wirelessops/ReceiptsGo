package co.smartreceipts.automatic_backups.drive

import android.content.Context
import android.content.Intent
import com.hadisatrio.optional.Optional
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

/**
 * no-op
 */
class DriveAccountHelper @Inject constructor() {

    val signInIntentsSubject: Subject<Intent> = PublishSubject.create()
    val errorIntentsSubject: Subject<Intent> = PublishSubject.create()

    fun signIn(context: Context): Single<Optional<DriveServiceHelper>> = Single.never()

    fun processResultIntent(intent: Intent?, context: Context): Single<DriveServiceHelper> = Single.never()
}