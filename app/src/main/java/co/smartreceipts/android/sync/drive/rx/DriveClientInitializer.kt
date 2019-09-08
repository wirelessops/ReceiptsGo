package co.smartreceipts.android.sync.drive.rx

import co.smartreceipts.android.sync.drive.managers.DriveDatabaseManager
import co.smartreceipts.android.sync.drive.managers.DriveReceiptsManager
import co.smartreceipts.android.sync.drive.managers.DriveRestoreDataManager
import co.smartreceipts.android.sync.drive.managers.GoogleDriveTableManager
import co.smartreceipts.android.sync.network.NetworkManager
import co.smartreceipts.android.sync.network.NetworkStateChangeListener
import co.smartreceipts.android.utils.log.Logger
import io.reactivex.disposables.CompositeDisposable

/**
 * This class is responsible for maintaining the actual work required to perform initialization to
 * our Google Drive client. Callers of this are responsible for maintaining the thread safety for
 * calls to the getter to ensure that [initialize] has first been called
 */
class DriveClientInitializer(val driveStreamsManager: DriveStreamsManager,
                             val driveReceiptsManager: DriveReceiptsManager,
                             private val driveDatabaseManager: DriveDatabaseManager,
                             val driveRestoreDataManager: DriveRestoreDataManager,
                             private val googleDriveTableManager: GoogleDriveTableManager,
                             private val networkManager: NetworkManager) : NetworkStateChangeListener {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun initialize() {
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