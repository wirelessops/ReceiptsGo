package co.smartreceipts.automatic_backups.drive

import android.content.Context
import android.content.Intent
import co.smartreceipts.analytics.log.Logger
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.hadisatrio.optional.Optional
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.*
import javax.inject.Inject

class DriveAccountHelper @Inject constructor() {

    val signInIntentsSubject: Subject<Intent> = PublishSubject.create()
    val errorIntentsSubject: Subject<Intent> = PublishSubject.create()

    fun signIn(context: Context): Single<Optional<DriveServiceHelper>> {
        val signInAccount = GoogleSignIn.getLastSignedInAccount(context)

        if (signInAccount == null) {
            val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(
                    Scope(DriveScopes.DRIVE_FILE),
                    Scope(DriveScopes.DRIVE_APPDATA)
                )
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, signInOptions)

            signInIntentsSubject.onNext(googleSignInClient.signInIntent)

            return Single.just(Optional.absent())
        } else {
            return onGoogleSignInAccountReady(signInAccount, context)
                .map { Optional.of(it) }
        }
    }

    private fun onGoogleSignInAccountReady(signInAccount: GoogleSignInAccount, context: Context): Single<DriveServiceHelper> {

        return Single.fromCallable {
                val scopes = "oauth2:" + DriveScopes.DRIVE_APPDATA + " " + DriveScopes.DRIVE_FILE
                GoogleAuthUtil.getToken(context, signInAccount.account, scopes)
            }.doOnError { throwable ->
                Logger.error(this@DriveAccountHelper, "Failed to authenticate user with status: {}", throwable.message)
                when (throwable) {
                    is UserRecoverableAuthException -> errorIntentsSubject.onNext(throwable.intent)
                    is UserRecoverableAuthIOException -> errorIntentsSubject.onNext(throwable.intent)
                }
            }
            .subscribeOn(Schedulers.io())
            .map { token -> getDriveServiceHelper(signInAccount, context) }
    }

    private fun getDriveServiceHelper(signInAccount: GoogleSignInAccount, context: Context): DriveServiceHelper {
        val scopes: MutableCollection<String> = ArrayList()
        scopes.add(DriveScopes.DRIVE_FILE)
        scopes.add(DriveScopes.DRIVE_APPDATA)
        // Use the authenticated account to sign in to the Drive service.
        val credential: GoogleAccountCredential = GoogleAccountCredential.usingOAuth2(context, scopes)
        credential.selectedAccount = signInAccount.account

        val googleDriveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory(),
                setHttpTimeout(credential)
            )
            .setApplicationName("Smart Receipts")
            .build()

        // The DriveServiceHelper encapsulates all REST API and SAF functionality.
        // Its instantiation is required before handling any onClick actions.
        return DriveServiceHelper(context, googleDriveService)

    }

    fun processResultIntent(intent: Intent?, context: Context): Single<DriveServiceHelper> {
        val signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(intent)

        if (signInAccountTask.isSuccessful) {
            Logger.info(this, "Successfully authorized our Google Drive account")
            val result = signInAccountTask.result
            if (result != null) {
                return onGoogleSignInAccountReady(result, context)
            }
        }

        return Single.error(Exception("Failed to successfully authorize our Google Drive account"))
    }

    private fun setHttpTimeout(requestInitializer: HttpRequestInitializer): HttpRequestInitializer? {
        return HttpRequestInitializer { httpRequest: HttpRequest ->
            requestInitializer.initialize(httpRequest)
            httpRequest.connectTimeout = 3 * 60000 // 3 minutes connect timeout
            httpRequest.readTimeout = 3 * 60000 // 3 minutes read timeout
        }
    }
}