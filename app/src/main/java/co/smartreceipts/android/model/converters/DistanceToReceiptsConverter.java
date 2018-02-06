package co.smartreceipts.android.model.converters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.CategoryBuilderFactory;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;

/**
 * An implementation of the {@link ModelConverter} contract, which
 * allows us to print {@link co.smartreceipts.android.model.Distance} values in a receipt table. Distances
 * will be summed up based of a given day.
 *
 * @author williambaumann
 */
public class DistanceToReceiptsConverter implements ModelConverter<Distance, Receipt> {

    private final Context mContext;
    private final String mDateSeparator;

    /**
     * Convenience constructor for this class.
     *
     * @param context - the current application {@link Context}
     * @param preferences - the user's {@link UserPreferenceManager}
     */
    public DistanceToReceiptsConverter(@NonNull Context context, @NonNull UserPreferenceManager preferences) {
        this(context, preferences.get(UserPreference.General.DateSeparator));
    }

    /**
     * Default constructor for this class.
     *
     * @param context - the current application {@link Context}
     * @param dateSeparator - the user's preferred date separator (e.g. "/")
     */
    public DistanceToReceiptsConverter(@NonNull Context context, @NonNull String dateSeparator) {
        mContext = context.getApplicationContext();
        mDateSeparator = dateSeparator;
    }


    @Override
    @NonNull
    public List<Receipt> convert(@NonNull List<Distance> distances) {
        final int size = distances.size();
        final HashMap<String, List<Distance>> distancesPerDay = new HashMap<>();
        // First, let's separate our distances to find what occurs each day
        for (int i = 0; i < size; i++) {
            final Distance distance = distances.get(i);
            final String formattedDate = distance.getFormattedDate(mContext, mDateSeparator);
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
        for (int i = 0; i < distancesThisDay.size(); i++) {
            final Distance distance = distancesThisDay.get(i);
            if (!names.contains(distance.getLocation())) {
                names.add(distance.getLocation());
            }
        }
        if (names.isEmpty()) {
            factory.setName(mContext.getString(R.string.distance));
        } else {
            factory.setName(TextUtils.join("; ", names));
        }
        factory.setTrip(distance0.getTrip());
        factory.setDate(distance0.getDate());
        factory.setFile(null);
        factory.setIsReimbursable(true);
        factory.setTimeZone(distance0.getTimeZone());
        factory.setCategory(new CategoryBuilderFactory().setName(mContext.getString(R.string.distance)).build());
        factory.setCurrency(distance0.getTrip().getTripCurrency());
        factory.setPrice(new PriceBuilderFactory().setPriceables(distancesThisDay, distance0.getTrip().getTripCurrency()).build());

        return factory.build();
    }
}
