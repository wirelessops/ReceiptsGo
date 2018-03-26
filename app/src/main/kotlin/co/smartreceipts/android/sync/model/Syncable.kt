package co.smartreceipts.android.sync.model

/**
 * Marks a particular model object as capable of being synced with a remote server environment
 */
interface Syncable {

    /**
     * The current [SyncState] associated with this item
     */
    val syncState: SyncState
}
