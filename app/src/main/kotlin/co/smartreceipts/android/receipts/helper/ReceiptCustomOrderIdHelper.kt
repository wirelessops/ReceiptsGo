package co.smartreceipts.android.receipts.helper

import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory

object ReceiptCustomOrderIdHelper {

    const val DAYS_TO_ORDER_FACTOR = 1000

    /**
     * Calculates changes that need to be applied to receipts after reordering.
     * More detailed [Receipt.customOrderId] formation [scheme](https://s3.amazonaws.com/smartreceipts/Diagrams/SmartReceiptsCustomSortingOrderDesign.png)
     *
     * Note: customOrderId = days * 1000; days could be fake for reordered item.
     * New receipt has customOrderId = days * 1000 + 999
     *
     * @param receipts the list of all [Receipt]s before reordering.
     * @param fromPosition previous position of the moved item.
     * @param toPosition new position of the moved item.
     *
     * @return list of [Receipt]s that need to be updated.
     */
    fun updateReceiptsCustomOrderIds(
        receipts: MutableList<Receipt>,
        fromPosition: Int,
        toPosition: Int
    ): List<Receipt> {

        // This calculates the "fake" days at the position that we've moved this item to
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
