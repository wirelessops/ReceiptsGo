package co.smartreceipts.android.versioning

import android.content.Context
import android.support.annotation.AnyThread
import android.util.Pair
import co.smartreceipts.android.di.scopes.ApplicationScope

import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.utils.log.Logger
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Monitors the application version code and triggers callbacks to [VersionUpgradedListener] whenever
 * an upgrade occurs
 */
@ApplicationScope
class AppVersionManager internal constructor(private val context: Context,
                                             private val userPreferenceManager: UserPreferenceManager,
                                             private val appVersionUpgradesList: AppVersionUpgradesList,
                                             private val onLaunchScheduler: Scheduler) {

    @Inject
    constructor(context: Context,
                userPreferenceManager: UserPreferenceManager,
                appVersionUpgradesList: AppVersionUpgradesList) : this(context, userPreferenceManager,appVersionUpgradesList,  Schedulers.io())

    @AnyThread
    fun onLaunch() {
        Observable.combineLatest(userPreferenceManager.getObservable(UserPreference.Internal.ApplicationVersionCode),
                Observable.fromCallable { context.packageManager.getPackageInfo(context.packageName, 0).versionCode },
                BiFunction<Int, Int, Pair<Int, Int>> { oldVersion, newVersion ->
                    Pair(oldVersion, newVersion)
                })
                .subscribeOn(onLaunchScheduler)
                .subscribe({
                    val oldVersion = it.first
                    val newVersion = it.second
                    if (newVersion > oldVersion) {
                        Logger.info(this, "Upgrading the app from version {} to {}", oldVersion, newVersion)
                        userPreferenceManager.set(UserPreference.Internal.ApplicationVersionCode, newVersion)
                        appVersionUpgradesList.getUpgradeListeners().forEach { listener ->
                            listener.onVersionUpgrade(oldVersion, newVersion)
                        }
                    }
                }, {
                    Logger.warn(this, "Failed to perform a version upgrade")
                })
    }

}
