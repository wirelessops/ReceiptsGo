package co.smartreceipts.android.graphs.entry;

import android.support.annotation.NonNull;

import com.github.mikephil.charting.data.BaseEntry;


public class LabeledGraphEntry extends BaseEntry implements Comparable<LabeledGraphEntry> {


    public LabeledGraphEntry(float value, String label) {
        super(value, label);
    }

    public String getLabel() {
        return (String)super.getData();
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) return false;

        LabeledGraphEntry that = (LabeledGraphEntry) obj;

        if (Float.compare(that.getY(), getY()) != 0) return false;

        String thisLabel = getLabel();
        String thatLabel = (String) that.getData();

        return thisLabel.equals(thatLabel);
    }

    @Override
    public int compareTo(@NonNull LabeledGraphEntry entry) {
        return -Float.compare(getY(), entry.getY());
    }

}
