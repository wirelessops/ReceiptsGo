package com.wops.receiptsgo.workers.reports.pdf.misc;

import androidx.annotation.NonNull;

import com.tom_roush.harmony.awt.AWTColor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.wops.receiptsgo.workers.reports.pdf.utils.PdfBoxUtils;
import com.wops.receiptsgo.workers.reports.pdf.fonts.PdfFontSpec;
import com.wops.receiptsgo.workers.reports.pdf.utils.HeavyHandedReplaceIllegalCharacters;

public class FixedWidthTextCell implements FixedWidthCell {

    private final float mWidth;
    private final String mText;
    private final PdfFontSpec mFontSpec;
    private final AWTColor mColor;
    private final float mCellPadding;

    private List<String> lines;

    public FixedWidthTextCell(float width, float cellPadding, String text,
                              @NonNull PdfFontSpec fontSpec,
                              @NonNull AWTColor color) {
        mWidth = width;
        mCellPadding = cellPadding;
        mText = HeavyHandedReplaceIllegalCharacters.getSafeString(text);
        mFontSpec = fontSpec;
        mColor = color;
    }

    private void breakUpString(String text) throws IOException {
        lines = new ArrayList<>();

        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        StringBuilder sb = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            sb.append(token).append(" ");
            String currentLine = sb.toString();
            if (PdfBoxUtils.getStringWidth(currentLine, mFontSpec) > mWidth - mCellPadding * 2) {
                if (sb.lastIndexOf(token) > 0) {
                    // Remove the token (i.e. last word) from the current line and place it as the start of the next
                    lines.add(sb.substring(0, sb.lastIndexOf(token)));
                    sb = new StringBuilder();
                    sb.append(token).append(" ");
                } else {
                    // If the token takes up the entire line, use it as the entire line
                    lines.add(sb.toString().trim());
                    sb = new StringBuilder();
                }
            }
        }
        if (!sb.toString().isEmpty()) {
            lines.add(sb.toString().trim());
        }
    }


    public float getWidth() {
        return mWidth;
    }

    @Override
    public float getHeight() {
        try {
            breakUpString(mText);
            return PdfBoxUtils.getFontHeight(mFontSpec) * lines.size() + 2 * mCellPadding;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

    }

    public List<String> getLines() throws IOException {
        // TODO, that's not very cool... Should we initialize lines in the constructor?
        if (lines == null) {
            breakUpString(mText);
        }
        return lines;
    }

    public PdfFontSpec getFontSpec() {
        return mFontSpec;
    }

    public AWTColor getColor() {
        return mColor;
    }

    @Override
    public float getCellPadding() {
        return mCellPadding;
    }
}
