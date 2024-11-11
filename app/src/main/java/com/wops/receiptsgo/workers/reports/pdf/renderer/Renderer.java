package com.wops.receiptsgo.workers.reports.pdf.renderer;

import androidx.annotation.NonNull;
import android.view.ViewGroup;

import java.io.IOException;

import com.wops.receiptsgo.workers.reports.pdf.pdfbox.PdfBoxWriter;
import com.wops.receiptsgo.workers.reports.pdf.renderer.constraints.HeightConstraint;
import com.wops.receiptsgo.workers.reports.pdf.renderer.constraints.RenderingConstraints;
import com.wops.receiptsgo.workers.reports.pdf.renderer.constraints.WidthConstraint;
import com.wops.receiptsgo.workers.reports.pdf.renderer.formatting.RenderingFormatting;

/**
 * An abstract super class for Pdf rendering. When interacting with this, users should first call
 * {@link #measure()} and then {@link #render(PdfBoxWriter)} to actually draw the content
 */
public abstract class Renderer {

    public static final int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;
    public static final int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;

    private final RenderingConstraints renderingConstraints = new RenderingConstraints();
    private final RenderingFormatting renderingFormatting = new RenderingFormatting();

    protected float width = WRAP_CONTENT;
    protected float height = WRAP_CONTENT;

    /**
     * @return the current set up constraints, which should be applied to this renderer
     */
    @NonNull
    public RenderingConstraints getRenderingConstraints() {
        return renderingConstraints;
    }

    /**
     * @return the current set up constraints, which should be applied to this renderer
     */
    @NonNull
    public RenderingFormatting getRenderingFormatting() {
        return renderingFormatting;
    }

    /**
     * @return The current width needed for this renderer (note: this may not be the measured height)
     */
    public float getWidth() {
        return getRenderingConstraints().getConstraint(WidthConstraint.class, width);
    }

    /**
     * @return The current height needed for this renderer (note: this may not be the measured height)
     */
    public float getHeight() {
        return getRenderingConstraints().getConstraint(HeightConstraint.class, height);
    }

    /**
     * @return a new deep copy of this {@link Renderer} instance
     */
    @NonNull
    public abstract Renderer copy();

    /**
     * Request that we measure our data to determine the size that will be required
     *
     * @throws IOException if the measuring fails
     */
    public abstract void measure() throws IOException;

    /**
     * Requests that we render this content using this {@link PdfBoxWriter}
     *
     * @param writer the writer to use to render this data
     * @throws IOException if the rendering fails
     */
    public abstract void render(@NonNull PdfBoxWriter writer) throws IOException;
}
