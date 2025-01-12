package com.wops.receiptsgo.workers.reports.pdf.renderer.formatting;

import androidx.annotation.NonNull;

import com.tom_roush.harmony.awt.AWTColor;

public class BackgroundColor extends AbstractFormatting<AWTColor> {

    public BackgroundColor(@NonNull AWTColor color) {
        super(color, AWTColor.class);
    }
}
