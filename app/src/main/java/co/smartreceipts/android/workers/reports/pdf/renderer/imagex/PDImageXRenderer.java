package co.smartreceipts.android.workers.reports.pdf.renderer.imagex;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;

import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxImageUtils;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxWriter;
import co.smartreceipts.android.workers.reports.pdf.renderer.Renderer;
import co.smartreceipts.android.workers.reports.pdf.renderer.constraints.HeightConstraint;
import co.smartreceipts.android.workers.reports.pdf.renderer.constraints.WidthConstraint;
import co.smartreceipts.android.workers.reports.pdf.renderer.constraints.XPositionConstraint;
import co.smartreceipts.android.workers.reports.pdf.renderer.constraints.YPositionConstraint;
import co.smartreceipts.android.workers.reports.pdf.renderer.formatting.Padding;

/**
 * Renders a {@link PDImageXObject} using a {@link PdfBoxWriter}.
 */
public class PDImageXRenderer extends Renderer {

    private final PDImageXFactory pdImageXFactory;

    public PDImageXRenderer(@NonNull PDImageXFactory pdImageXFactory) {
        this.pdImageXFactory = Preconditions.checkNotNull(pdImageXFactory);

        this.height = MATCH_PARENT;
        this.width = MATCH_PARENT;
    }

    @NonNull
    @Override
    public Renderer copy() {
        final PDImageXRenderer copy = new PDImageXRenderer(this.pdImageXFactory);
        copy.height = this.height;
        copy.width = this.width;
        copy.getRenderingConstraints().setConstraints(this.getRenderingConstraints());
        copy.getRenderingFormatting().setFormatting(this.getRenderingFormatting());
        return copy;
    }

    @Override
    public void measure() throws IOException {
        final float heightConstraint = Preconditions.checkNotNull(getRenderingConstraints().getConstraint(HeightConstraint.class));
        final float widthConstraint = Preconditions.checkNotNull(getRenderingConstraints().getConstraint(WidthConstraint.class));

        this.height = heightConstraint;
        this.width = widthConstraint;
    }

    @Override
    public void render(@NonNull PdfBoxWriter writer) throws IOException {
        float x = Preconditions.checkNotNull(getRenderingConstraints().getConstraint(XPositionConstraint.class));
        float y = Preconditions.checkNotNull(getRenderingConstraints().getConstraint(YPositionConstraint.class));
        final float padding = getRenderingFormatting().getFormatting(Padding.class, 0f);

        final PDImageXObject imageXObject = pdImageXFactory.get();

        float availableHeight = height - 2 * padding;
        float availableWidth = width - 2 * padding;
        final PDRectangle rectangle = new PDRectangle(x + padding, y + padding, availableWidth, availableHeight);
        final PDRectangle resizedRec = PdfBoxImageUtils.scaleImageInsideRectangle(imageXObject, rectangle);

        writer.printPDImageXObject(imageXObject, resizedRec.getLowerLeftX(), resizedRec.getLowerLeftY(), resizedRec.getWidth(), resizedRec.getHeight());
    }
}
