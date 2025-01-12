package com.wops.receiptsgo.workers.reports.pdf.renderer.empty;

import androidx.annotation.NonNull;

import java.io.IOException;

import com.wops.receiptsgo.workers.reports.pdf.pdfbox.PdfBoxWriter;
import com.wops.receiptsgo.workers.reports.pdf.renderer.Renderer;
import com.wops.receiptsgo.workers.reports.pdf.renderer.constraints.HeightConstraint;
import com.wops.receiptsgo.workers.reports.pdf.renderer.constraints.WidthConstraint;

public class EmptyRenderer extends Renderer {

    public EmptyRenderer() {
        this(WRAP_CONTENT, WRAP_CONTENT);
    }

    public EmptyRenderer(float width, float height) {
        this.width = width;
        this.height = height;
    }

    @NonNull
    @Override
    public Renderer copy() {
        return new EmptyRenderer(this.width, this.height);
    }

    @Override
    public void measure() throws IOException {
        final Float heightConstraint = getRenderingConstraints().getConstraint(HeightConstraint.class);
        final Float widthConstraint = getRenderingConstraints().getConstraint(WidthConstraint.class);
        
        if (heightConstraint != null) {
            this.height = heightConstraint;
        } else if (this.height < 0) {
            this.height = 0;
        }

        if (widthConstraint != null) {
            this.width = widthConstraint;
        } else if (this.width < 0) {
            this.width = 0;
        }
    }

    @Override
    public void render(@NonNull PdfBoxWriter writer) throws IOException {
        // Intentional no-op
    }
}
