package co.smartreceipts.android.model.converters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.smartreceipts.android.R;
import co.smartreceipts.android.date.DateFormatter;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.CategoryBuilderFactory;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;

/**
 * An implementation of the {@link ModelConverter} contract, which
 * allows us to print {@link co.smartreceipts.android.model.Distance} values in a receipt table. Distances
 * will be summed up based of a given day.
 */
public class DistanceToReceiptsConverter implements ModelConverter<Distance, Receipt> {

    private final Context context;
    private final DateFormatter dateFormatter;

    /**
     * Default constructor for this class.
     *
     * @param context - the current application {@link Context}
     * @param dateFormatter - the {@link DateFormatter} for the user's preferred date settings
     */
    public DistanceToReceiptsConverter(@NonNull Context context, @NonNull DateFormatter dateFormatter) {
        this.context = context.getApplicationContext();
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
    }

    @Override
    @NonNull
    public List<Receipt> convert(@NonNull List<Distance> distances) {
        final int size = distances.size();
        final HashMap<String, List<Distance>> distancesPerDay = new HashMap<>();
        // First, let's separate our distances to find what occurs each day
        for (int i = 0; i < size; i++) {
            final Distance distance = distances.get(i);
            final String formattedDate = dateFormatter.getFormattedDate(distance.getDisplayableDate());
            if (distancesPerDay.containsKey(formattedDate)) {
                distancesPerDay.get(formattedDate).add(distance);
            }
            else {
                final List<Distance> distanceList = new ArrayList<>();
                distanceList.add(distance);
                distancesPerDay.put(formattedDate, distanceList);
            }
        }

        final List<Receipt> receipts = new ArrayList<>(distancesPerDay.keySet().size());
        for (Map.Entry<String, List<Distance>> entry : distancesPerDay.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                receipts.add(generateReceipt(entry.getValue()));
            }
        }
        return receipts;
    }

    @NonNull
    private Receipt generateReceipt(@NonNull List<Distance> distancesThisDay) {
        if (distancesThisDay.isEmpty()) {
            throw new IllegalArgumentException("distancesThisDay must not be empty");
        }

        // Set up default values for everything
        final Distance distance0 = distancesThisDay.get(0);
        final ReceiptBuilderFactory factory = new ReceiptBuilderFactory(-1); // Randomize the id
        final ArrayList<String> names = new ArrayList<>();
        final ArrayList<String> comments = new ArrayList<>();
        for (int i = 0; i < distancesThisDay.size(); i++) {
            final Distance distance = distancesThisDay.get(i);
            if (!names.contains(distance.getLocation())) {
                names.add(distance.getLocation());
            }
            if (!TextUtils.isEmpty(distance.getComment()) && !comments.contains(distance.getComment())) {
                comments.add(distance.getComment());
            }
        }
        if (names.isEmpty()) {
            factory.setName(context.getString(R.string.distance));
        } else {
            factory.setName(TextUtils.join("; ", names));
        }
        if (!comments.isEmpty()) {
            factory.setComment(TextUtils.join("; ", comments));
        }
        factory.setTrip(distance0.getTrip());
        factory.setDate(distance0.getDate());
        factory.setFile(null);
        factory.setIsReimbursable(true);
        factory.setTimeZone(distance0.getTimeZone());
        factory.setCategory(new CategoryBuilderFactory().setName(context.getString(R.string.distance)).build());
        factory.setCurrency(distance0.getTrip().getTripCurrency());
        factory.setPrice(new PriceBuilderFactory().setPriceables(distancesThisDay, distance0.getTrip().getTripCurrency()).build());

        return factory.build();
    }
}
