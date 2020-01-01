package co.smartreceipts.android.database

import android.content.Context
import android.content.ContextWrapper
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.android.persistence.DatabaseHelper
import wb.android.storage.StorageManager
import java.io.File
import javax.inject.Inject


/**
 * Beginning with app version 78, we moved our receipts database from external storage to internal
 * storage to avoid scenarios in which users could not recover their data with a broken screen (as
 * a sort of emergency recovery outlet).
 *
 * This change requires that we have some knowledge of the file system, which requires a disk read
 * to properly manage. Rather than getting the root path before constructing the database, which
 * presents a UiThread risk, we instead override the [Context] methods responsible for database
 * creation for this explicit purpose.
 */
@ApplicationScope
class DatabaseContext @Inject constructor(context: Context) : ContextWrapper(context) {

    override fun getDatabasePath(name: String?): File {
        return if (DatabaseHelper.DATABASE_NAME == name) {
            // Only return the internal path to this file if it's our receipts database
            val externalDatabaseDirectory = File(StorageManager.GetRootPath())
            File(externalDatabaseDirectory, DatabaseHelper.DATABASE_NAME)
        } else {
            super.getDatabasePath(name)
        }
    }

    override fun openOrCreateDatabase(name: String?, mode: Int, factory: SQLiteDatabase.CursorFactory?): SQLiteDatabase {
        return this.openOrCreateDatabase(name, mode, factory, null)
    }

    override fun openOrCreateDatabase(name: String?, mode: Int, factory: SQLiteDatabase.CursorFactory?, errorHandler: DatabaseErrorHandler?): SQLiteDatabase {
        val databaseFile = getDatabasePath(name)
        return super.openOrCreateDatabase(databaseFile.path, mode, factory, errorHandler)
    }
}