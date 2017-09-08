package co.smartreceipts.android.workers.reports.pdf.renderer.text;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontSpec;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxImageUtils;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxWriter;
import co.smartreceipts.android.workers.reports.pdf.renderer.Renderer;
import co.smartreceipts.android.workers.reports.pdf.renderer.constraints.WidthConstraint;
import co.smartreceipts.android.workers.reports.pdf.renderer.constraints.XPositionConstraint;
import co.smartreceipts.android.workers.reports.pdf.renderer.constraints.YPositionConstraint;
import co.smartreceipts.android.workers.reports.pdf.renderer.formatting.Alignment;
import co.smartreceipts.android.workers.reports.pdf.renderer.formatting.BackgroundColor;
import co.smartreceipts.android.workers.reports.pdf.renderer.formatting.Color;
import co.smartreceipts.android.workers.reports.pdf.renderer.formatting.Font;
import co.smartreceipts.android.workers.reports.pdf.renderer.formatting.Padding;
import wb.android.image.ImageUtils;

/**
 * A fallback implementation, which should be used in conjunction with the {@link TextRenderer} class
 * in order to handle situations in which our base pdf doesn't have a font with which to render a
 * character glyph (common amongst non-Western languages, since we preset Roboto and not the Noto
 * fonts).
 */
public class FallbackTextRenderer extends Renderer {

    private static final int WIDTH_SCALE_FACTOR = 3;
    private static final int UI_THREAD_WAITING_TIME_SECONDS = 5;

    private static int HEIGHT_MEASURE_SPEC = ViewGroup.LayoutParams.WRAP_CONTENT;

    private final Context context;
    private final PDDocument pdDocument;
    private final String string;
    private final Handler uiThreadRunner = new Handler(Looper.getMainLooper());

    public FallbackTextRenderer(@NonNull Context context, @NonNull PDDocument pdDocument, @NonNull String string) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.pdDocument = Preconditions.checkNotNull(pdDocument);
        this.string = Preconditions.checkNotNull(string);

        this.width = WRAP_CONTENT;
        this.height = WRAP_CONTENT;
    }

    @NonNull
    @Override
    public Renderer copy() {
        final FallbackTextRenderer copy = new FallbackTextRenderer(this.context, this.pdDocument, this.string);
        copy.height = this.height;
        copy.width = this.width;
        copy.getRenderingConstraints().setConstraints(this.getRenderingConstraints());
        copy.getRenderingFormatting().setFormatting(this.getRenderingFormatting());
        return copy;
    }

    @Override
    public void measure() throws IOException {
        final PdfFontSpec fontSpec = Preconditions.checkNotNull(getRenderingFormatting().getFormatting(Font.class));
        final Float widthConstraint = getRenderingConstraints().getConstraint(WidthConstraint.class);
        final float padding = getRenderingFormatting().getFormatting(Padding.class, 0f);

        final TextView unscaledTextView = createTextView(fontSpec.getSize());

        final int widthMeasureSpec;
        if (widthConstraint != null) {
            // Note: we remove the padding, since we manage that internally for this fallback view
            widthMeasureSpec = View.MeasureSpec.makeMeasureSpec((int) (widthConstraint - 2 * padding), View.MeasureSpec.EXACTLY);
        } else {
            widthMeasureSpec = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        synchronouslyRunUiThreadOperation(new Runnable() {
            @Override
            public void run() {
                unscaledTextView.measure(widthMeasureSpec, HEIGHT_MEASURE_SPEC);
                unscaledTextView.layout(0, 0, unscaledTextView.getMeasuredWidth(), unscaledTextView.getMeasuredHeight());
            }
        });

        width = unscaledTextView.getWidth();
        height = unscaledTextView.getHeight();
    }

    @Override
    public void render(@NonNull PdfBoxWriter writer) throws IOException {
        final PdfFontSpec fontSpec = Preconditions.checkNotNull(getRenderingFormatting().getFormatting(Font.class));
        final Float widthConstraint = getRenderingConstraints().getConstraint(WidthConstraint.class);
        final float x = Preconditions.checkNotNull(getRenderingConstraints().getConstraint(XPositionConstraint.class));
        final float y = Preconditions.checkNotNull(getRenderingConstraints().getConstraint(YPositionConstraint.class));
        final float padding = getRenderingFormatting().getFormatting(Padding.class, 0f);

        final TextView textView = createTextView(fontSpec.getSize() * WIDTH_SCALE_FACTOR);

        final int widthMeasureSpec;
        if (widthConstraint != null) {
            widthMeasureSpec = View.MeasureSpec.makeMeasureSpec((int) ((widthConstraint - 2 * padding) * WIDTH_SCALE_FACTOR), View.MeasureSpec.EXACTLY);
        } else {
            widthMeasureSpec = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        final AtomicReference<Bitmap> bitmapReference = new AtomicReference<>();
        final AtomicReference<IOException> ioExceptionReference = new AtomicReference<>();
        try {
            synchronouslyRunUiThreadOperation(new Runnable() {
                @Override
                public void run() {
                    textView.measure(widthMeasureSpec, HEIGHT_MEASURE_SPEC);
                    textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());
                    Bitmap bitmap = ImageUtils.drawView(textView);
                    bitmap = ImageUtils.applyWhiteBackground(bitmap);
                    try {
                        bitmap = ImageUtils.changeCodec(bitmap, Bitmap.CompressFormat.JPEG, 100);
                        bitmapReference.set(bitmap);
                    } catch (IOException e) {
                        ioExceptionReference.set(e);
                    }
                }
            });

            final IOException ioException = ioExceptionReference.get();
            if (ioException != null) {
                throw ioException;
            } else {
                final Bitmap bitmap = bitmapReference.get();
                final PDImageXObject imageXObject = JPEGFactory.createFromImage(pdDocument, bitmap, 1);
                float availableHeight = height;
                float availableWidth = width;
                final PDRectangle rectangle = new PDRectangle(x + padding, y + padding, availableWidth, availableHeight);
                final PDRectangle resizedRec = PdfBoxImageUtils.scaleImageInsideRectangle(imageXObject, rectangle);
                writer.printPDImageXObject(imageXObject, resizedRec.getLowerLeftX(), resizedRec.getLowerLeftY(), resizedRec.getWidth(), resizedRec.getHeight());
            }
        } finally {
            final Bitmap bitmap = bitmapReference.get();
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    /**
     * All the {@link android.widget.TextView} stuff has to be run on the main thread, but this requires some fanciful
     * thread management. To handle (pun intended) in a relatively simple manner, we use a {@link Handler}
     * and {@link CountDownLatch} to ensure the UI operation completes before continuing.
     *
     * @param runnable the {@link Runnable} operation to perform
     * @throws IOException if this operation fails to complete in under {@link #UI_THREAD_WAITING_TIME_SECONDS}
     */
    private void synchronouslyRunUiThreadOperation(@NonNull final Runnable runnable) throws IOException {
        final CountDownLatch uiOperationLatch = new CountDownLatch(1);
        final Runnable runnableWrapper = new Runnable() {
            @Override
            public void run() {
                runnable.run();
                uiOperationLatch.countDown();
            }
        };
        uiThreadRunner.post(runnableWrapper);
        try {
            uiOperationLatch.await(UI_THREAD_WAITING_TIME_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IOException("Failed to load the pdf", e);
        }
    }

    @NonNull
    private TextView createTextView(int fontSizePx) {
        final AWTColor color = Preconditions.checkNotNull(getRenderingFormatting().getFormatting(Color.class));
        final AWTColor backgroundColor = getRenderingFormatting().getFormatting(BackgroundColor.class);
        final Alignment.Type alignment = getRenderingFormatting().getFormatting(Alignment.class, Alignment.Type.Centered);

        final TextView textView = new TextView(context);
        textView.setText(string);
        textView.setTextColor(color.color);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSizePx);
        if (alignment == Alignment.Type.Centered) {
            textView.setGravity(Gravity.CENTER);
        } else {
            textView.setGravity(Gravity.START);
        }
        if (backgroundColor != null) {
            textView.setBackgroundColor(backgroundColor.color);
        }
        return textView;
    }

    @VisibleForTesting
    public static void setHeightMeasureSpec(int measureSpec) {
        HEIGHT_MEASURE_SPEC = measureSpec;
    }

    @VisibleForTesting
    public static void resetHeightMeasureSpec() {
        HEIGHT_MEASURE_SPEC = ViewGroup.LayoutParams.WRAP_CONTENT;
    }
}
