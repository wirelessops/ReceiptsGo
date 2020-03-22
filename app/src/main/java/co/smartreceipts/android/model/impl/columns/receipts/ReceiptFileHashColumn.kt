package co.smartreceipts.android.model.impl.columns.receipts

import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl
import co.smartreceipts.core.sync.model.SyncState
import okhttp3.internal.and
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

/**
 * Provides a column that returns the file path for a particular receipt
 */
class ReceiptFileHashColumn(id: Int, syncState: SyncState, customOrderId: Long, uuid: UUID) :
    AbstractColumnImpl<Receipt>(
        id,
        ReceiptColumnDefinitions.ActualDefinition.IMAGE_HASH,
        syncState,
        customOrderId,
        uuid
    ) {

    override fun getValue(rowItem: Receipt): String {
        return if (rowItem.file != null) {
            hashImgFile(rowItem.file)
        } else {
            ""
        }
    }

    private fun hashImgFile(file: File): String {
        try {
            FileInputStream(file).use { inputStream ->
                val buffer = ByteArray(1024)
                val digest = MessageDigest.getInstance("SHA-256")
                var numRead = 0
                while (numRead != -1) {
                    numRead = inputStream.read(buffer)
                    if (numRead > 0)
                        digest.update(buffer, 0, numRead)
                }
                val hashedBytes = digest.digest()
                return convertHashToString(hashedBytes)
            }
        } catch (e: NoSuchAlgorithmException) {
            return ""
        }

    }

    private fun convertHashToString(hashedBytes: ByteArray): String {
        val returnVal = StringBuilder()
        for (hashedByte in hashedBytes) {
            returnVal.append(((hashedByte and 0xff) + 0x100).toString(16).substring(1))
        }
        return returnVal.toString().takeLast(16).toUpperCase(Locale.ENGLISH)
    }
}
