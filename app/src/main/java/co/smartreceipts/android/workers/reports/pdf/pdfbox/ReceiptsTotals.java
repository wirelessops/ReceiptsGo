package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;

/**
 * Encapsulates the summations for the various receipt totals that we can present to the end user via
 * the PDF reports. It is expected that this implementation will properly handle the pre-tax vs
 * post-tax calculations as well.
 */
public class ReceiptsTotals {

    private final Price receiptsWithOutTaxPrice;
    private final Price taxPrice;
    private final Price receiptsWithTaxPrice;
    private final Price distancePrice;
    private final Price reimbursableGrandTotalPrice;
    private final Price grandTotalPrice;

    public ReceiptsTotals(@NonNull Trip trip, @NonNull List<Receipt> receipts, @NonNull List<Distance> distances, @NonNull UserPreferenceManager preferences) {
        Preconditions.checkNotNull(trip);
        Preconditions.checkNotNull(receipts);
        Preconditions.checkNotNull(distances);
        Preconditions.checkNotNull(preferences);

        final ArrayList<Price> receiptsWithOutTaxTotal = new ArrayList<>();
        final ArrayList<Price> taxesTotal = new ArrayList<>();
        final ArrayList<Price> receiptsWithTaxTotal = new ArrayList<>();
        final ArrayList<Price> distanceTotal = new ArrayList<>();
        final ArrayList<Price> reimbursableGrandTotal = new ArrayList<>();
        final ArrayList<Price> grandTotal = new ArrayList<>();

        // Sum up our receipt totals for various conditions
        final int len = receipts.size();
        for (int i = 0; i < len; i++) {
            final Receipt receipt = receipts.get(i);
            if (!preferences.get(UserPreference.Receipts.OnlyIncludeReimbursable) || receipt.isReimbursable()) {
                grandTotal.add(receipt.getPrice());
                taxesTotal.add(receipt.getTax());
                receiptsWithOutTaxTotal.add(receipt.getPrice());
                receiptsWithTaxTotal.add(receipt.getPrice());
                if (preferences.get(UserPreference.Receipts.UsePreTaxPrice)) {
                    // Add the taxes to these two, since we're in pre tax mode (ie the price doesn't include the tax)
                    grandTotal.add(receipt.getTax());
                    receiptsWithTaxTotal.add(receipt.getTax());
                } else {
                    // In post-tax mode, we'll add the tax as a negative value (ie subtract it)
                    receiptsWithOutTaxTotal.add(new PriceBuilderFactory().setCurrency(receipt.getTax().getCurrency()).setPrice(receipt.getTax().getPrice().multiply(new BigDecimal(-1))).build());
                }

                // Add reimbursable totals
                if (receipt.isReimbursable()) {
                    reimbursableGrandTotal.add(receipt.getPrice());
                    if (preferences.get(UserPreference.Receipts.UsePreTaxPrice)) {
                        reimbursableGrandTotal.add(receipt.getTax());
                    }
                }
            }
        }

        // Sum up our distance totals
        for (int i = 0; i < distances.size(); i++) {
            final Distance distance = distances.get(i);
            distanceTotal.add(distance.getPrice());
            grandTotal.add(distance.getPrice());
            reimbursableGrandTotal.add(distance.getPrice());
        }

        final PriceCurrency tripCurrency = trip.getTripCurrency();
        grandTotalPrice = new PriceBuilderFactory().setPrices(grandTotal, tripCurrency).build();
        receiptsWithTaxPrice = new PriceBuilderFactory().setPrices(receiptsWithTaxTotal, tripCurrency).build();
        reimbursableGrandTotalPrice = new PriceBuilderFactory().setPrices(reimbursableGrandTotal, tripCurrency).build();
        receiptsWithOutTaxPrice = new PriceBuilderFactory().setPrices(receiptsWithOutTaxTotal, tripCurrency).build();
        taxPrice = new PriceBuilderFactory().setPrices(taxesTotal, tripCurrency).build();
        distancePrice = new PriceBuilderFactory().setPrices(distanceTotal, tripCurrency).build();
    }

    /**
     * @return the total {@link Price} of all of our receipts, excluding the tax value
     */
    @NonNull
    public Price getReceiptsWithOutTaxPrice() {
        return receiptsWithOutTaxPrice;
    }

    /**
     * @return the total tax {@link Price} of all of our receipts
     */
    @NonNull
    public Price getTaxPrice() {
        return taxPrice;
    }

    /**
     * @return the total {@link Price} of all of our receipts, include the tax value
     */
    @NonNull
    public Price getReceiptsWithTaxPrice() {
        return receiptsWithTaxPrice;
    }

    /**
     * @return the total {@link Price} of all of our distances
     */
    @NonNull
    public Price getDistancePrice() {
        return distancePrice;
    }

    /**
     * @return the grand total {@link Price} of all of our receipts (including taxes) and distances
     */
    @NonNull
    public Price getGrandTotalPrice() {
        return grandTotalPrice;
    }

    /**
     * @return the reimbursable total {@link Price} of all of our receipts (including taxes) and distances
     */
    @NonNull
    public Price getReimbursableGrandTotalPrice() {
        return reimbursableGrandTotalPrice;
    }
}
