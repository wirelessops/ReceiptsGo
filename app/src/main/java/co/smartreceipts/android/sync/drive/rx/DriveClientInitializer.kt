package co.smartreceipts.android.sync.drive.rx

import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.sync.drive.managers.DriveDatabaseManager
import co.smartreceipts.android.sync.drive.managers.DriveReceiptsManager
import co.smartreceipts.android.sync.drive.managers.DriveRestoreDataManager
import co.smartreceipts.android.sync.drive.managers.GoogleDriveTableManager
import co.smartreceipts.android.sync.network.NetworkManager
import co.smartreceipts.android.sync.network.NetworkStateChangeListener
import co.smartreceipts.android.utils.log.Logger
import com.google.android.gms.drive.DriveClient
import com.google.android.gms.drive.TransferPreferences
import com.google.android.gms.drive.TransferPreferencesBuilder
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


/**
 * This class is responsible for maintaining the actual work required to perform initialization to
 * our Google Drive client. Callers of this are responsible for maintaining the thread safety for
 * calls to the getter to ensure that [initialize] has first been called
 */
class DriveClientInitializer(private val driveClient: DriveClient,
                             val driveStreamsManager: DriveStreamsManager,
                             val driveReceiptsManager: DriveReceiptsManager,
                             val driveDatabaseManager: DriveDatabaseManager,
                             val driveRestoreDataManager: DriveRestoreDataManager,
                             private val userPreferenceManager: UserPreferenceManager,
                             private val googleDriveTableManager: GoogleDriveTableManager,
                             private val networkManager: NetworkManager) : NetworkStateChangeListener {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun initialize() {
        // Monitor for future changes to our wifi settings preference
        compositeDisposable.add(userPreferenceManager.userPreferenceChangeStream
                .subscribeOn(Schedulers.io())
                .filter{ UserPreference.Misc.AutoBackupOnWifiOnly == it }
                .subscribe {
                    val builder = TransferPreferencesBuilder()
                    if (userPreferenceManager[UserPreference.Misc.AutoBackupOnWifiOnly]) {
                        builder.setNetworkPreference(TransferPreferences.NETWORK_TYPE_WIFI_ONLY)
                    } else {
                        builder.setNetworkPreference(TransferPreferences.NETWORK_TYPE_ANY)
                    }
                    driveClient.setUploadPreferences(builder.build())
                })

        // Configure out baseline wifi settings preference
        val transferPreferencesBuilder = TransferPreferencesBuilder()
        if (userPreferenceManager[UserPreference.Misc.AutoBackupOnWifiOnly]) {
            transferPreferencesBuilder.setNetworkPreference(TransferPreferences.NETWORK_TYPE_WIFI_ONLY)
        } else {
            transferPreferencesBuilder.setNetworkPreference(TransferPreferences.NETWORK_TYPE_ANY)
        }
        driveClient.setUploadPreferences(transferPreferencesBuilder.build())

        this.networkManager.registerListener(this)
        this.googleDriveTableManager.initializeListeners(driveDatabaseManager, driveReceiptsManager)
        this.driveReceiptsManager.syncReceipts()
        this.driveStreamsManager.onConnected(null)
    }

    fun deinitialize() {
        compositeDisposable.clear()
        networkManager.unregisterListener(this)
        googleDriveTableManager.deinitializeListeners()
        driveStreamsManager.onConnectionSuspended(-1)
    }

    override fun onNetworkConnectivityLost() {
        // Intentional no-op
    }

    override fun onNetworkConnectivityGained() {
        Logger.info(this, "Handling a NetworkConnectivityGained event for drive")
        driveReceiptsManager.syncReceipts()
    }
}