package com.wops.receiptsgo.workers.reports.pdf.colors;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.tom_roush.harmony.awt.AWTColor;

import java.util.HashMap;
import java.util.Map;

public class PdfColorManager {

    private final Map<PdfColorStyle, AWTColor> colors = new HashMap<>();

    public PdfColorManager() {
        colors.put(PdfColorStyle.Outline, new AWTColor(0, 122, 255));
        colors.put(PdfColorStyle.TableHeader, new AWTColor(204, 228, 255));
        colors.put(PdfColorStyle.TableCell, new AWTColor(239, 239, 244));
        colors.put(PdfColorStyle.Default, AWTColor.BLACK);
    }

    @NonNull
    public AWTColor getColor(@NonNull PdfColorStyle style) {
        return Preconditions.checkNotNull(colors.get(style));
    }
}
