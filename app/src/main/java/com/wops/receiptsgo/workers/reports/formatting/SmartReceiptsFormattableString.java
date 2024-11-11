package com.wops.receiptsgo.workers.reports.formatting;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.List;

import com.wops.receiptsgo.date.DateFormatter;
import com.wops.receiptsgo.model.Distance;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.settings.catalog.UserPreference;
import com.wops.receiptsgo.workers.reports.pdf.pdfbox.ReceiptsTotals;

public class SmartReceiptsFormattableString {

    private static final String REPORT_NAME = "%REPORT_NAME%";
    private static final String USER_ID = "%USER_ID%";
    private static final String REPORT_START = "%REPORT_START%";
    private static final String REPORT_END = "%REPORT_END%";
    private static final String REPORT_GROSS_TOTAL = "%REPORT_GROSS_TOTAL%";

    private final String string;
    private final Trip trip;
    private final UserPreferenceManager preferences;
    private final DateFormatter dateFormatter;
    private final ReceiptsTotals totals;

    public SmartReceiptsFormattableString(@NonNull String string,
                                          @NonNull Trip trip,
                                          @NonNull UserPreferenceManager preferences,
                                          @NonNull DateFormatter dateFormatter,
                                          @NonNull List<Receipt> receipts,
                                          @NonNull List<Distance> distances) {
        this.string = Preconditions.checkNotNull(string);
        this.trip = Preconditions.checkNotNull(trip);
        this.preferences = Preconditions.checkNotNull(preferences);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.totals = new ReceiptsTotals(trip, Preconditions.checkNotNull(receipts), Preconditions.checkNotNull(distances), preferences);
    }

    @Override
    public String toString() {
        return string
                .replace(REPORT_NAME, trip.getName())
                .replace(USER_ID, preferences.get(UserPreference.ReportOutput.UserId))
                .replace(REPORT_START, dateFormatter.getFormattedDate(trip.getStartDisplayableDate()))
                .replace(REPORT_END, dateFormatter.getFormattedDate(trip.getEndDisplayableDate()))
                .replace(REPORT_GROSS_TOTAL, totals.getGrandTotalPrice().getCurrencyFormattedPrice());
    }
}
