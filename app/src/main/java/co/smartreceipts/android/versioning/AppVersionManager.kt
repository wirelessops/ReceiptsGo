package co.smartreceipts.android.versioning

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.AnyThread
import androidx.core.content.pm.PackageInfoCompat
import android.util.Pair
import co.smartreceipts.android.di.scopes.ApplicationScope

import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.utils.log.Logger
import co.smartreceipts.android.utils.rx.RxSchedulers
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.functions.BiFunction
import javax.inject.Inject
import javax.inject.Named

/**
 * Monitors the application version code and triggers callbacks to [VersionUpgradedListener] whenever
 * an upgrade occurs
 */
@ApplicationScope
class AppVersionManager @Inject constructor(private val context: Context,
                                            private val userPreferenceManager: UserPreferenceManager,
                                            private val appVersionUpgradesList: AppVersionUpgradesList,
                                            @Named(RxSchedulers.IO) private val onLaunchScheduler: Scheduler) {

    @SuppressLint("CheckResult")
    @AnyThread
    fun onLaunch() {
        Observable.combineLatest(userPreferenceManager.getObservable(UserPreference.Internal.ApplicationVersionCode),
                Observable.fromCallable {
                    val longVersionCode = PackageInfoCompat.getLongVersionCode(context.packageManager.getPackageInfo(context.packageName, 0))
                    // We don't use the long version code (most significant 32 digits of the long),
                    // so we just ignore this by taking the lower 32 bytes (the standard version code)
                    return@fromCallable longVersionCode.toInt()
                },
                BiFunction<Int, Int, Pair<Int, Int>> { oldVersion, newVersion ->
                    Pair(oldVersion, newVersion)
                })
                .subscribeOn(onLaunchScheduler)
                .subscribe({
                    val oldVersion = it.first
                    val newVersion = it.second
                    if (newVersion > oldVersion) {
                        Logger.info(this, "Upgrading the app from version {} to {}", oldVersion, newVersion)
                        userPreferenceManager[UserPreference.Internal.ApplicationVersionCode] = newVersion
                        appVersionUpgradesList.getUpgradeListeners().forEach { listener ->
                            listener.onVersionUpgrade(oldVersion, newVersion)
                        }
                    }
                }, {
                    Logger.warn(this, "Failed to perform a version upgrade")
                })
    }

}
