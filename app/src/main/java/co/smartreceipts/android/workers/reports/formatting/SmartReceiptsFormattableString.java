package co.smartreceipts.android.workers.reports.formatting;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.List;

import co.smartreceipts.android.date.DateFormatter;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.ReceiptsTotals;

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
