package com.wops.receiptsgo.workers.reports.pdf.renderer.grid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.tom_roush.harmony.awt.AWTColor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wops.receiptsgo.workers.reports.pdf.pdfbox.PdfBoxWriter;
import com.wops.receiptsgo.workers.reports.pdf.renderer.Renderer;
import com.wops.receiptsgo.workers.reports.pdf.renderer.constraints.HeightConstraint;
import com.wops.receiptsgo.workers.reports.pdf.renderer.constraints.WidthConstraint;
import com.wops.receiptsgo.workers.reports.pdf.renderer.constraints.XPositionConstraint;
import com.wops.receiptsgo.workers.reports.pdf.renderer.constraints.YPositionConstraint;
import com.wops.receiptsgo.workers.reports.pdf.renderer.formatting.Alignment;
import com.wops.receiptsgo.workers.reports.pdf.renderer.formatting.BackgroundColor;
import com.wops.receiptsgo.workers.reports.pdf.renderer.formatting.Padding;

public class GridRowRenderer extends Renderer {

    private final List<? extends Renderer> columns;
    private GridRowRenderer header;

    public GridRowRenderer(@NonNull Renderer renderer) {
        this(Collections.singletonList(Preconditions.checkNotNull(renderer)));
    }

    public GridRowRenderer(@NonNull List<? extends Renderer> columns) {
        this.columns = new ArrayList<>(Preconditions.checkNotNull(columns));

        this.width = MATCH_PARENT;

        float layoutHeight = WRAP_CONTENT;
        for (final Renderer column : columns) {
            // We'll use match parent if any of them are set to that
            if (column.getHeight() == MATCH_PARENT) {
                layoutHeight = MATCH_PARENT;
                break;
            }
        }
        this.height = layoutHeight;
    }

    public void associateHeaderRow(@Nullable GridRowRenderer header) {
        this.header = header;
    }

    @Nullable
    public GridRowRenderer getAssociatedHeader() {
        return header;
    }

    @NonNull
    @Override
    public Renderer copy() {
        final List<Renderer> renderers = new ArrayList<>();
        for (final Renderer renderer : this.columns) {
            renderers.add(renderer.copy());
        }
        final GridRowRenderer copy = new GridRowRenderer(renderers);
        copy.header = this.header;
        copy.height = this.height;
        copy.width = this.width;
        copy.getRenderingConstraints().setConstraints(this.getRenderingConstraints());
        copy.getRenderingFormatting().setFormatting(this.getRenderingFormatting());
        return copy;
    }

    @Override
    public void measure() throws IOException {
        final float x = Preconditions.checkNotNull(getRenderingConstraints().getConstraint(XPositionConstraint.class, 0f));
        final float y = Preconditions.checkNotNull(getRenderingConstraints().getConstraint(YPositionConstraint.class, 0f));
        final float widthConstraint = Preconditions.checkNotNull(getRenderingConstraints().getConstraint(WidthConstraint.class));
        final Float heightConstraint = getRenderingConstraints().getConstraint(HeightConstraint.class);

        final Float padding = getRenderingFormatting().getFormatting(Padding.class);
        if (padding != null) {
            for (final Renderer renderer : columns) {
                renderer.getRenderingFormatting().addFormatting(new Padding(padding));
            }
        }

        final AWTColor backgroundColor = getRenderingFormatting().getFormatting(BackgroundColor.class);
        if (backgroundColor != null) {
            for (final Renderer renderer : columns) {
                renderer.getRenderingFormatting().addFormatting(new BackgroundColor(backgroundColor));
            }
        }

        final Alignment.Type alignment = getRenderingFormatting().getFormatting(Alignment.class);
        if (alignment != null) {
            for (final Renderer renderer : columns) {
                renderer.getRenderingFormatting().addFormatting(new Alignment(alignment));
            }
        }

        float unconstrainedWidth = widthConstraint;
        int constrainedByWidthColumnCount = 0;
        for (int i = 0; i < columns.size(); i++) {
            final Float columnWidthConstraint = columns.get(i).getRenderingConstraints().getConstraint(WidthConstraint.class);
            if (columnWidthConstraint != null) {
                unconstrainedWidth -= columnWidthConstraint;
                constrainedByWidthColumnCount++;
            }
        }

        final WidthConstraint remainingColumnsWidthConstraint;
        if (constrainedByWidthColumnCount != columns.size()) {
            final float unconstrainedPerColumnWidthConstraint = unconstrainedWidth / (columns.size() - constrainedByWidthColumnCount);
            remainingColumnsWidthConstraint = new WidthConstraint(unconstrainedPerColumnWidthConstraint);
        } else {
            remainingColumnsWidthConstraint = null;
        }

        // First - measure out all our widths
        float measuredHeight = -1;
        float currentXPosition = x;
        for (int i = 0; i < columns.size(); i++) {
            final Renderer column = columns.get(i);
            // TODO: Refactor this if we use dynamic formatting for the first tables
            if (heightConstraint != null) {
                column.getRenderingConstraints().addConstraint(new HeightConstraint(heightConstraint));
            }
            if (!column.getRenderingConstraints().hasConstraint(WidthConstraint.class)) {
                column.getRenderingConstraints().addConstraint(remainingColumnsWidthConstraint);
            }

            column.getRenderingConstraints().addConstraint(new XPositionConstraint(currentXPosition));
            column.getRenderingConstraints().addConstraint(new YPositionConstraint(y));
            column.measure();

            final Float columnWidthConstraint = column.getRenderingConstraints().getConstraint(WidthConstraint.class);
            currentXPosition += columnWidthConstraint;
            measuredHeight = Math.max(measuredHeight, column.getHeight());
        }

        this.width = widthConstraint;
        this.height = measuredHeight;
    }

    @Override
    public void render(@NonNull PdfBoxWriter writer) throws IOException {
        final AWTColor backgroundColor = getRenderingFormatting().getFormatting(BackgroundColor.class);
        if (backgroundColor != null) {
            float x = Preconditions.checkNotNull(getRenderingConstraints().getConstraint(XPositionConstraint.class));
            float y = Preconditions.checkNotNull(getRenderingConstraints().getConstraint(YPositionConstraint.class));
            writer.printRectangle(backgroundColor, x, y, width, height);
        }
        for (final Renderer column : columns) {
            column.render(writer);
        }
    }

}
