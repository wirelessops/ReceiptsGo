package co.smartreceipts.android.receipts.helper

import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory

object ReceiptCustomOrderIdHelper {

    const val DAYS_TO_ORDER_FACTOR = 1000

    // Note: customOrderId = days * 1000; days could be fake for reordered item
    // New receipt has customOrderId = days * 1000 + 999


    fun updateReceiptsCustomOrderIds(
        receipts: MutableList<Receipt>,
        fromPosition: Int,
        toPosition: Int
    ): List<Receipt> {

        val toPositionFakeDays = receipts[toPosition].customOrderId / DAYS_TO_ORDER_FACTOR

        val movedReceipt = receipts.removeAt(fromPosition)

        receipts.add(
            toPosition, ReceiptBuilderFactory(movedReceipt)
                .setCustomOrderId(toPositionFakeDays * DAYS_TO_ORDER_FACTOR)
                .build()
        )

        // we need to update all items with same fake days
        val updatedReceipts = mutableListOf<Receipt>()
        var sameFakeDaysNumber = 0

        for (i in receipts.indices.reversed()) {
            val receipt = receipts[i]

            if (receipt.customOrderId / DAYS_TO_ORDER_FACTOR == toPositionFakeDays) {
                updatedReceipts.add(
                    ReceiptBuilderFactory(receipt)
                        .setCustomOrderId(toPositionFakeDays * DAYS_TO_ORDER_FACTOR + sameFakeDaysNumber)
                        .build()
                )

                sameFakeDaysNumber++
            }
        }

        return updatedReceipts
    }

}
