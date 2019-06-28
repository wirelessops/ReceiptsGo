package co.smartreceipts.android.versioning

import androidx.annotation.WorkerThread

interface VersionUpgradedListener {

    /**
     * Called when the application version was upgraded.
     *
     *
     * This was added after version 78 was release, so version 79 was the first "new" one. If this
     * is a fresh install, the [oldVersion] will appear as -1
     *
     *
     * @param oldVersion the old application version
     * @param newVersion the new application version
     */
    @WorkerThread
    fun onVersionUpgrade(oldVersion: Int, newVersion: Int)

}
