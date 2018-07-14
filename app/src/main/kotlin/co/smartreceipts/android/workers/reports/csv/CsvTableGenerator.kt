package co.smartreceipts.android.workers.reports.csv

import co.smartreceipts.android.filters.Filter
import co.smartreceipts.android.model.Column
import co.smartreceipts.android.workers.reports.ReportResourcesManager
import co.smartreceipts.android.workers.reports.TableGenerator
import java.util.*

/**
 * Implements the [TableGenerator] contract to generate a CSV file as a [String]
 */
class CsvTableGenerator<DataType> @JvmOverloads constructor(
    private val reportResourceManager: ReportResourcesManager,
    private val columns: List<Column<DataType>>,
    private val printHeaders: Boolean,
    private val printFooters: Boolean,
    private val filter: Filter<DataType>? = null
) : TableGenerator<String, DataType> {

    override fun generate(list: List<DataType>): String {
        if (!list.isEmpty()) {
            val columnCount = columns.size
            val csvBuilder = StringBuilder("")

            // Add the header
            if (printHeaders) {
                for (i in 0 until columnCount) {
                    addCell(
                        csvBuilder,
                        reportResourceManager.getFlexString(columns[i].headerStringResId),
                        i == columnCount - 1
                    )
                }
                csvBuilder.append("\n")
            }

            // Add each row
            val filteredList = ArrayList<DataType>()
            for (j in list.indices) {
                val data = list[j]
                if (filter == null || filter.accept(data)) {
                    for (i in 0 until columnCount) {
                        addCell(csvBuilder, columns[i].getValue(data), i == columnCount - 1)
                    }
                    filteredList.add(data)
                }
                csvBuilder.append("\n")
            }

            // Add the footer
            if (printFooters) {
                for (i in 0 until columnCount) {
                    addCell(csvBuilder, columns[i].getFooter(filteredList), i == columnCount - 1)
                }
                csvBuilder.append("\n")
            }
            return csvBuilder.toString()
        } else {
            return "" // Just return an empty csv if we don't have any objects
        }
    }

    private fun addCell(csvBuilder: StringBuilder, value: String?, isLastElement: Boolean) {
        value?.let {
            var csvValue = it
            if (csvValue.contains(QUOTE)) {
                csvValue = csvValue.replace(QUOTE, ESCAPED_QUOTE)
            }
            for (stringToQuote in STRINGS_THAT_MUST_BE_QUOTED) {
                if (csvValue.contains(stringToQuote)) {
                    csvValue = QUOTE + csvValue + QUOTE
                    break
                }
            }
            csvBuilder.append(csvValue)
        }

        if (!isLastElement) {
            // Only append a comma if this is the last element
            csvBuilder.append(",")
        }
    }

    companion object {

        private const val QUOTE = "\""
        private const val ESCAPED_QUOTE = "\"\""
        private val STRINGS_THAT_MUST_BE_QUOTED = arrayOf(",", "\"", "\n", "\r\n")
    }

}
