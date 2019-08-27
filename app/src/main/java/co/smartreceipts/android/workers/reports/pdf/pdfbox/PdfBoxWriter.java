package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.harmony.awt.AWTColor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.workers.reports.pdf.colors.PdfColorStyle;
import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontSpec;
import co.smartreceipts.android.workers.reports.pdf.utils.HeavyHandedReplaceIllegalCharacters;


/**
 * This writer class directly interacts with the underlying PdfBox layer in order to abstract that
 * away and provide simple writing functionality. Please note that by default, PdfBox treats the
 * coordinate (0, 0) as the bottom-left of the page. Since I find it more meaningful to treat (0, 0)
 * as the top-left of the page (ie the same as Android), all public methods in this class will
 * also implicitly handle this translation.
 */
public class PdfBoxWriter {
    private final PDDocument mDocument;
    private final PdfBoxContext mContext;
    private final PdfBoxPageDecorations mPageDecorations;
    private final List<PDPage> mPages;

    private float currentYPosition;
    private float topOfPageYPosition = -1;
    private float leftOfPageXPosition = 0;
    private PDPageContentStream contentStream;

    public PdfBoxWriter(@NonNull PDDocument doc,
                        @NonNull PdfBoxContext context,
                        @NonNull PdfBoxPageDecorations pageDecorations) throws IOException {
        mDocument = doc;
        mContext = context;
        mPageDecorations = pageDecorations;
        mPages = new ArrayList<>();
    }

    /**
     * Creates a new PDF page with the decorated header and footer
     *
     * @throws IOException if this operation fails
     */
    public void newPage() throws IOException {
        if (contentStream != null) {
            contentStream.close();
        }

        PDPage page = new PDPage(mContext.getPageSize());
        mPages.add(page);
        contentStream = new PDPageContentStream(mDocument, page);
        mPageDecorations.writeHeader(contentStream);
        currentYPosition = page.getMediaBox().getHeight() - mContext.getPageMarginVertical() - mPageDecorations.getHeaderHeight();
        leftOfPageXPosition = mContext.getPageMarginHorizontal();
        topOfPageYPosition = currentYPosition;

        mPageDecorations.writeFooter(contentStream);
    }

    /**
     * Prints a Rectangle in a particular color
     *
     * @param color the {@link AWTColor} to draw
     * @param x the x-coordinate location, where 0 is the left of the page
     * @param y the y-coordinate location, where 0 is the top of the page
     * @param width the width that this image requires
     * @param height the height that this image requires
     *
     * @throws IOException if something fails during the printing process
     */
    public void printRectangle(@NonNull AWTColor color, float x, float y, float width, float height) throws IOException {
        final PDRectangle rect = new PDRectangle(leftOfPageXPosition + x, swapYCoordinate(y) - height, width, height);
        contentStream.setNonStrokingColor(color);
        contentStream.addRect(rect.getLowerLeftX(), rect.getLowerLeftY(), rect.getWidth(), rect.getHeight());
        contentStream.fill();

        // Reset to default afterwards
        contentStream.setNonStrokingColor(mContext.getColorManager().getColor(PdfColorStyle.Default));
    }

    /**
     * Prints a {@link PDImageXObject} (usually an image or another PDF) inside our PDF
     *
     * @param image the {@link PDImageXObject} to print
     * @param x the x-coordinate location, where 0 is the left of the page
     * @param y the y-coordinate location, where 0 is the top of the page
     * @param width the width that this image requires
     * @param height the height that this image requires
     *
     * @throws IOException if something fails during the printing process
     */
    public void printPDImageXObject(@NonNull PDImageXObject image, float x, float y, float width, float height) throws IOException {
        contentStream.drawImage(image, leftOfPageXPosition + x, swapYCoordinate(y) - height, width, height);
    }

    /**
     * Prints a given string in the desired font/color combination at the specified x/y coordinates
     *
     * @param string the {@link String} to print
     * @param font the desired {@link PdfFontSpec} to write the text in
     * @param color the preferred {@link AWTColor} to use
     * @param x the x-coordinate location, where 0 is the left of the page
     * @param y the y-coordinate location, where 0 is the top of the page
     *
     * @throws IOException if something fails during the printing process
     */
    public void printText(@NonNull String string, @NonNull PdfFontSpec font, @NonNull AWTColor color, float x, float y) throws IOException {
        contentStream.setFont(font.getFont(), font.getSize());
        contentStream.setNonStrokingColor(color);
        contentStream.beginText();
        contentStream.newLineAtOffset(leftOfPageXPosition + x, swapYCoordinate(y));
        contentStream.showText(HeavyHandedReplaceIllegalCharacters.getSafeString(string));
        contentStream.endText();
    }

    /**
     * Writes the file and closes our stream
     *
     * @throws IOException if we fail to write this item
     */
    public void writeAndClose() throws IOException {
        if (contentStream != null) {
            contentStream.close();
        }

        for (PDPage page : mPages) {
            mDocument.addPage(page);
        }
    }

    /**
     * Please note that by default, PdfBox treats the coordinate (0, 0) as the bottom-left of the
     * page. Since I find it more meaningful to treat (0, 0) as the top-left of the page (ie the
     * same as Android), we can use this method to perform the internal swap that PDF box requires.
     *
     * @param y the y-coordinate location, where 0 is the TOP of the page
     * @return y the y-coordinate location, where 0 is the BOTTOM of the page
     */
    private float swapYCoordinate(float y) {
        Preconditions.checkArgument(topOfPageYPosition > 0, "You cannot write any values until creating a new page");
        return topOfPageYPosition - y;
    }

}
