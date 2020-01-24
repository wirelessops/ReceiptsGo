package co.smartreceipts.analytics.utils;

import androidx.annotation.NonNull;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {

	/**
	 * Generates a stack trace for a given exception.
	 * 
	 * @param throwable a {@link Throwable} to get the stack trace from
	 * @return - the {@link String} containing the trace
	 */
	public static String getStackTrace(@NonNull Throwable throwable) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		throwable.printStackTrace(pw);
		return sw.toString();
	}

}