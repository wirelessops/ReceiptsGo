package com.wops.receiptsgo.workers.reports.pdf.renderer.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.harmony.awt.AWTColor;

import com.wops.receiptsgo.date.DateFormatter;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.settings.catalog.UserPreference;
import com.wops.receiptsgo.workers.reports.pdf.fonts.PdfFontSpec;
import com.wops.receiptsgo.workers.reports.pdf.renderer.text.TextRenderer;

public class ReceiptLabelTextRenderer extends TextRenderer {


    public ReceiptLabelTextRenderer(@NonNull Receipt receipt,
                                    @NonNull Context context,
                                    @NonNull PDDocument pdDocument,
                                    @NonNull UserPreferenceManager userPreferenceManager,
                                    @NonNull DateFormatter dateFormatter,
                                    @NonNull AWTColor color,
                                    @NonNull PdfFontSpec fontSpec) {
        super(context, pdDocument, new TextFormatter(userPreferenceManager, dateFormatter).buildLegendForImage(receipt), color, fontSpec);
    }

    @VisibleForTesting
    static class TextFormatter {

        private static final String SEP = " \u2022 ";

        private final UserPreferenceManager userPreferenceManager;
        private final DateFormatter dateFormatter;

        public TextFormatter(@NonNull UserPreferenceManager userPreferenceManager,
                             @NonNull DateFormatter dateFormatter) {
            this.userPreferenceManager = Preconditions.checkNotNull(userPreferenceManager);
            this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        }

        @NonNull
        private String buildLegendForImage(@NonNull Receipt receipt) {
            final int num = (userPreferenceManager.get(UserPreference.ReportOutput.PrintUserIdByPdfPhoto)) ?
                    receipt.getId() : receipt.getIndex();

            final String extra = (userPreferenceManager.get(UserPreference.ReportOutput.PrintReceiptCommentByPdfPhoto)
                    && !TextUtils.isEmpty(receipt.getComment()))
                    ? SEP + receipt.getComment()
                    : "";

            return num + SEP + receipt.getName() + SEP
                    + dateFormatter.getFormattedDate(receipt.getDisplayableDate()) + extra;
        }
    }
}
