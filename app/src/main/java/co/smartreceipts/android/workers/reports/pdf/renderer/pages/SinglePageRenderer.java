package co.smartreceipts.android.workers.reports.pdf.renderer.pages;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.io.IOException;

import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxWriter;
import co.smartreceipts.android.workers.reports.pdf.renderer.Renderer;

public class SinglePageRenderer extends Renderer {

    private final Renderer renderer;

    public SinglePageRenderer(@NonNull Renderer renderer) {
        this.renderer = Preconditions.checkNotNull(renderer);
    }

    @NonNull
    @Override
    public Renderer copy() {
        final Renderer rendererCopy = this.renderer.copy();
        final SinglePageRenderer copy = new SinglePageRenderer(rendererCopy);
        copy.width = this.width;
        copy.height = this.height;
        copy.getRenderingConstraints().setConstraints(this.getRenderingConstraints());
        copy.getRenderingFormatting().setFormatting(this.getRenderingFormatting());
        return copy;
    }

    @Override
    public void measure() throws IOException {
        renderer.measure();
    }

    @Override
    public void render(@NonNull PdfBoxWriter writer) throws IOException {
        writer.newPage();
        renderer.render(writer);
    }
}
