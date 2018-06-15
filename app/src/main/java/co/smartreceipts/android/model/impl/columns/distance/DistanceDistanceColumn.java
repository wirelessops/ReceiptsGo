package co.smartreceipts.android.model.impl.columns.distance;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.List;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.sync.model.SyncState;

public final class DistanceDistanceColumn extends AbstractColumnImpl<Distance> {

    public DistanceDistanceColumn(int id, @NonNull String name, @NonNull SyncState syncState) {
        super(id, name, syncState);
    }

    @Override
    public String getValue(@NonNull Distance distance) {
        return distance.getDecimalFormattedDistance();
    }

    @Override
    @NonNull
    public String getFooter(@NonNull List<Distance> distances) {
        BigDecimal distance = BigDecimal.ZERO;
        for (int i = 0; i < distances.size(); i++) {
            distance = distance.add(distances.get(i).getDistance());
        }
        return ModelUtils.getDecimalFormattedValue(distance);
    }
}
