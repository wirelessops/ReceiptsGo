package co.smartreceipts.android.graphs;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.graphs.entry.LabeledGraphEntry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class GraphEntryTest {

    private final static String LABEL = "some label";

    @Test
    public void sortLabeled() {
        final LabeledGraphEntry e1 = new LabeledGraphEntry(9.5f, LABEL);
        final LabeledGraphEntry e2 = new LabeledGraphEntry(10f, LABEL);
        final LabeledGraphEntry e3 = new LabeledGraphEntry(0f, LABEL);

        final List<LabeledGraphEntry> entries = new ArrayList<>();
        entries.add(e1);
        entries.add(e2);
        entries.add(e3);

        Collections.sort(entries);

        assertEquals(e2, entries.get(0));
        assertEquals(e1, entries.get(1));
        assertEquals(e3, entries.get(2));
    }

    @Test
    public void equalsLabeled() {
        final LabeledGraphEntry e1 = new LabeledGraphEntry(9.5f, LABEL);
        final LabeledGraphEntry e2 = new LabeledGraphEntry(10f, LABEL);
        final LabeledGraphEntry e3 = new LabeledGraphEntry(10f, "some label");
        final LabeledGraphEntry e4 = new LabeledGraphEntry(10f, "some another label");

        assertFalse(e1.equals(e2));
        assertFalse(e2.equals(e4));
        assertEquals(e2, e3);
    }
}
