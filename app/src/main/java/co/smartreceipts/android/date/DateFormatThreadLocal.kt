package co.smartreceipts.android.date

import java.text.DateFormat


/**
 * Since the [DateFormat] class in Java is not thread safe by default, we take advantage of Java's
 * [ThreadLocal] to optimize for both memory and performance.
 *
 * @see <a href="DateFormat with Multiple Threads">https://stackoverflow.com/a/4021932/4250037</a>
 */
class DateFormatThreadLocal(private val initialValue: DateFormat) : ThreadLocal<DateFormat>() {

    override fun initialValue(): DateFormat {
        return initialValue
    }
}