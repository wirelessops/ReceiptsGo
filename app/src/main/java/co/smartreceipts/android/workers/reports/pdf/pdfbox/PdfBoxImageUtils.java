package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.support.annotation.NonNull;

import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;


public class PdfBoxImageUtils {


    /**
     * Scales the <code>ximage</code> up so that it fills the rectangle or down so that it fits
     * inside it, maintaining its aspect ration. Returns a {@link PDRectangle} with the dimensions
     * that the scaled image should have, and positions on the top of the containing
     * <code>rectangle</code>, centered horizontally.
     *
     * @param ximage the {@link PDImageXObject}, which contains our graphic
     * @param boundingRectangle the max bounding {@link PDRectangle}
     * @return a new {@link PDRectangle}, which contains the correct bounds for the image
     */
    @NonNull
    public static PDRectangle scaleImageInsideRectangle(@NonNull PDImageXObject ximage, @NonNull PDRectangle boundingRectangle) {

        final float imageWidthToHeightRatio = ((float) ximage.getWidth())/((float) ximage.getHeight());
        final float rectBoundsWithToHeightRatio = boundingRectangle.getWidth()/boundingRectangle.getHeight();
        float scalingFactor;

        if (ximage.getHeight() < boundingRectangle.getHeight()) {
            if (ximage.getWidth() < boundingRectangle.getWidth()) {
                // In this case, the image is fully smaller (ie both width and height) than the bounding box. As such, we should scale it up
                if (imageWidthToHeightRatio > rectBoundsWithToHeightRatio) {
                    scalingFactor = boundingRectangle.getWidth() / ximage.getWidth();
                } else {
                    scalingFactor = boundingRectangle.getHeight() / ximage.getHeight();
                }
            } else {
                // In this case, the image height < the bounds, but the width > bounds. As such, we should scale down the width
                scalingFactor = boundingRectangle.getWidth() / ximage.getWidth();
            }
        } else { // ximage.getHeight() > rectangle.getHeight
            if (ximage.getWidth() > boundingRectangle.getWidth()) {
                // In this case, the image is fully larger (ie both width and height) than the bounding box. As such, we should scale it down
                if (imageWidthToHeightRatio > rectBoundsWithToHeightRatio) {
                    scalingFactor = boundingRectangle.getWidth() / ximage.getWidth();
                } else {
                    scalingFactor = boundingRectangle.getHeight() / ximage.getHeight();
                }
            } else {
                // In this case, the image height > the bounds, but the width M bounds. As such, we should scale down the height
                scalingFactor = boundingRectangle.getHeight() / ximage.getHeight();
            }
        }


        float scaledImageWidth = ximage.getWidth() * scalingFactor;
        float scaledImageHeight = ximage.getHeight() * scalingFactor;

        float unusedWidth = (boundingRectangle.getWidth() - scaledImageWidth) / 2.0f;
        return new PDRectangle(boundingRectangle.getLowerLeftX() + unusedWidth, boundingRectangle.getLowerLeftY(), scaledImageWidth, scaledImageHeight);
    }
}
