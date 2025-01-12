package com.wops.receiptsgo.workers.reports.pdf.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * If PDFBox does not support a particular character, we straight-up crash. As a short-term fix,
 * we're just going to replace these illegal characters with "?" ones. As we grow, we should look
 * into much more graceful solutions (eg rendering these on Android and then converting the text
 * Canvas into an image of the glyph).
 */
public class HeavyHandedReplaceIllegalCharacters {

    @NonNull
    public static String getSafeString(@Nullable String string) {
        if (string != null) {
            return string.replaceAll("\\p{C}", "").replace("(", "{").replace(")", "}");
        } else {
            return "";
        }
    }

}
