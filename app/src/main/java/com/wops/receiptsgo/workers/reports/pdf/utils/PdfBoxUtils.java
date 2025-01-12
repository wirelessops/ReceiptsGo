package com.wops.receiptsgo.workers.reports.pdf.utils;


import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.StringTokenizer;

import com.wops.receiptsgo.workers.reports.pdf.fonts.PdfFontSpec;

public class PdfBoxUtils {

    /**
     * Returns the width of a string, when rendered with the specified font
     * @param text
     * @param fontSpec
     * @return
     * @throws IOException
     */
    public static float getStringWidth(@NonNull String text, @NonNull PdfFontSpec fontSpec)
            throws IOException {
        try {
            return fontSpec.getFont().getStringWidth(text) * fontSpec.getSize() / 1000F;
        } catch (IllegalArgumentException e) {
            // If we cannot encode, try it again with the unicode "?"/tofu character
            // We just use latin chars for a best guess here
            return fontSpec.getFont().getStringWidth(text.replaceAll("\\P{InBasic_Latin}", "\uFFFD\uFFFD")) * fontSpec.getSize() / 1000F;
        }
    }

    /**
     * Returns the full height of a font (including the bounding box).
     * @param fontSpec
     * @return
     * @throws IOException
     */
    public static float getFontHeight(@NonNull PdfFontSpec fontSpec) {
        return fontSpec.getFont().getFontDescriptor().getFontBoundingBox().getHeight() / 1000
                * fontSpec.getSize();
    }


    /**
     * Returns the height of the font, excluding the descent, ie starting from the baseline and
     * above.
     * @param fontSpec
     * @return
     * @throws IOException
     */
    public static float getFontAboveBaselineHeight(@NonNull PdfFontSpec fontSpec) {
        return (fontSpec.getFont().getFontDescriptor().getFontBoundingBox().getHeight()
                + fontSpec.getFont().getFontDescriptor().getDescent()) //descent is negative
                / 1000 * fontSpec.getSize();
    }

    /**
     * Returns the min string width to display the string broken up into various lines by spaces.
     * That's the largest word width.
     * @param text
     * @param fontSpec
     * @return
     * @throws IOException
     */
    public static float getMaxWordWidth(@NonNull String text, @NonNull PdfFontSpec fontSpec) throws IOException {
        float min = 0.0f;

        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        while (tokenizer.hasMoreTokens()) {
            String s = tokenizer.nextToken();
            float w = getStringWidth(s, fontSpec);
            if (w > min) {
                min = w;
            }
        }
        return min;
    }

}
