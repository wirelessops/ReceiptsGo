package co.smartreceipts.android.workers.reports.pdf.renderer.formatting;

import androidx.annotation.NonNull;

import com.tom_roush.pdfbox.util.awt.AWTColor;

public class BackgroundColor extends AbstractFormatting<AWTColor> {

    public BackgroundColor(@NonNull AWTColor color) {
        super(color, AWTColor.class);
    }
}
