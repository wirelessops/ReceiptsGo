package com.wops.receiptsgo.workers.reports.pdf.renderer.formatting;

import androidx.annotation.NonNull;

import com.tom_roush.harmony.awt.AWTColor;

public class Color extends AbstractFormatting<AWTColor> {

    public Color(@NonNull AWTColor color) {
        super(color, AWTColor.class);
    }
}
