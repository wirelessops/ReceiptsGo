package co.smartreceipts.android.workers.reports.pdf.renderer.grid;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import co.smartreceipts.analytics.log.Logger;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxWriter;
import co.smartreceipts.android.workers.reports.pdf.renderer.Renderer;
import co.smartreceipts.android.workers.reports.pdf.renderer.constraints.HeightConstraint;
import co.smartreceipts.android.workers.reports.pdf.renderer.constraints.WidthConstraint;
import co.smartreceipts.android.workers.reports.pdf.renderer.constraints.XPositionConstraint;
import co.smartreceipts.android.workers.reports.pdf.renderer.constraints.YPositionConstraint;
import co.smartreceipts.android.workers.reports.pdf.renderer.formatting.Padding;

public class GridRenderer extends Renderer {

    protected final List<GridRowRenderer> rowRenderers = new ArrayList<>();
    protected final List<GridRowRenderer> newLineRows = new ArrayList<>();

    public GridRenderer(float width, float height) {
        this(new WidthConstraint(width), new HeightConstraint(height));
    }

    public GridRenderer(@NonNull WidthConstraint widthConstraint, @NonNull HeightConstraint heightConstraint) {
        this.getRenderingConstraints().addConstraint(widthConstraint);
        this.getRenderingConstraints().addConstraint(heightConstraint);
    }

    public void addRow(@NonNull GridRowRenderer rowRenderer) {
        rowRenderers.add(rowRenderer);
    }

    public void addRows(@NonNull List<GridRowRenderer> rows) {
        rowRenderers.addAll(rows);
    }

    @NonNull
    @Override
    public Renderer copy() {
        final List<GridRowRenderer> renderers = new ArrayList<>();
        final List<GridRowRenderer> newLines = new ArrayList<>();
        for (final GridRowRenderer renderer : this.rowRenderers) {
            final GridRowRenderer rowCopy = (GridRowRenderer) renderer.copy();
            renderers.add(rowCopy);
            if (newLines.contains(renderer)) {
                newLines.add(rowCopy);
            }
        }
        final GridRenderer copy = new GridRenderer(width, this.height);
        copy.rowRenderers.addAll(renderers);
        copy.newLineRows.addAll(newLines);
        copy.height = this.height;
        copy.width = this.width;
        copy.getRenderingConstraints().setConstraints(this.getRenderingConstraints());
        copy.getRenderingFormatting().setFormatting(this.getRenderingFormatting());
        return copy;
    }

    @Override
    public void measure() throws IOException {
        final float widthConstraint = Preconditions.checkNotNull(getRenderingConstraints().getConstraint(WidthConstraint.class));
        final float heightConstraint = Preconditions.checkNotNull(getRenderingConstraints().getConstraint(HeightConstraint.class));

        final Float padding = getRenderingFormatting().getFormatting(Padding.class);
        if (padding != null) {
            for (final GridRowRenderer rowRenderer : rowRenderers) {
                rowRenderer.getRenderingFormatting().addFormatting(new Padding(padding));
            }
        }

        // First - measure out "required" space for rows that have constraints or are set to WRAP
        float heightOfRowsWithoutMatchParent = 0;
        int matchParentRowCount = 0;
        for (final GridRowRenderer rowRenderer : rowRenderers) {
            Preconditions.checkArgument(rowRenderer.getWidth() == MATCH_PARENT, "All grid rows must currently match the parent size");
            rowRenderer.getRenderingConstraints().addConstraint(new WidthConstraint(widthConstraint));
            if (rowRenderer.getHeight() != MATCH_PARENT) {
                // If we're using a constraint or WRAP_CONTENT
                rowRenderer.measure();
                heightOfRowsWithoutMatchParent += rowRenderer.getHeight();
            } else {
                matchParentRowCount++;
            }
        }

        // Next - split the remaining space evenly between the remaining rows
        final float matchParentSpacePerItem = (heightConstraint - heightOfRowsWithoutMatchParent) / matchParentRowCount;
        for (final GridRowRenderer rowRenderer : rowRenderers) {
            if (rowRenderer.getHeight() == MATCH_PARENT) {
                rowRenderer.getRenderingConstraints().addConstraint(new HeightConstraint(matchParentSpacePerItem));
                rowRenderer.measure();
            }
        }

        // Finally - apply the x/y coords based on the heights
        final float x = Preconditions.checkNotNull(getRenderingConstraints().getConstraint(XPositionConstraint.class, 0f));
        float y = Preconditions.checkNotNull(getRenderingConstraints().getConstraint(YPositionConstraint.class, 0f));

        List<GridRowRenderer> gridRowRendererLinkedList = new LinkedList<>(rowRenderers);
        for (ListIterator<GridRowRenderer> listIterator = gridRowRendererLinkedList.listIterator(); listIterator.hasNext(); ) {
            final GridRowRenderer rowRenderer = listIterator.next();
            if (y + rowRenderer.getHeight() >= heightConstraint && listIterator.hasNext()) {
                Logger.info(this, "Identified a page breaking table. Marking it as such");
                y = 0;
                if (rowRenderer.getAssociatedHeader() != null) {
                    final GridRowRenderer newPageHeader = (GridRowRenderer) rowRenderer.getAssociatedHeader().copy();
                    newPageHeader.getRenderingConstraints().addConstraint(new XPositionConstraint(x));
                    newPageHeader.getRenderingConstraints().addConstraint(new YPositionConstraint(y));
                    newPageHeader.measure();
                    rowRenderers.add(rowRenderers.indexOf(rowRenderer), newPageHeader);
                    y += newPageHeader.getHeight();
                    newLineRows.add(newPageHeader);
                } else {
                    newLineRows.add(rowRenderer);
                }
            }
            rowRenderer.getRenderingConstraints().addConstraint(new XPositionConstraint(x));
            rowRenderer.getRenderingConstraints().addConstraint(new YPositionConstraint(y));
            y += rowRenderer.getHeight();

            // Run a final measure pass based on these new coordinates
            rowRenderer.measure();
        }
    }

    @Override
    public void render(@NonNull PdfBoxWriter writer) throws IOException {
        for (final GridRowRenderer rowRenderer : rowRenderers) {
            if (newLineRows.contains(rowRenderer)) {
                writer.newPage();
            }
            rowRenderer.render(writer);
        }
    }
}
