package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.workers.reports.formatting.SmartReceiptsFormattableString;
import co.smartreceipts.android.workers.reports.pdf.colors.PdfColorStyle;
import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontSpec;
import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontStyle;
import co.smartreceipts.android.workers.reports.pdf.utils.HeavyHandedReplaceIllegalCharacters;


public class DefaultPdfBoxPageDecorations implements PdfBoxPageDecorations {

    private static final float HEADER_HEIGHT = 15.0f;
    private static final float HEADER_LINE_HEIGHT = 4.0f;

    private static final float FOOTER_LINE_HEIGHT = 2.0f;
    private static final float FOOTER_PADDING = 12.0f;
    private static final float FOOTER_HEIGHT = 15.0f;

    private final PdfBoxContext pdfBoxContext;
    private final String footerText;

    DefaultPdfBoxPageDecorations(@NonNull PdfBoxContext pdfBoxContext, @NonNull Trip trip) {
        Preconditions.checkNotNull(trip);
        this.pdfBoxContext = Preconditions.checkNotNull(pdfBoxContext);

        final SmartReceiptsFormattableString formattableString = new SmartReceiptsFormattableString(pdfBoxContext.getPreferences().get(UserPreference.PlusSubscription.PdfFooterString),
                trip, pdfBoxContext.getPreferences(), pdfBoxContext.getDateFormatter());
        footerText = HeavyHandedReplaceIllegalCharacters.getSafeString(formattableString.toString());
    }

    /**
     * Prints out a colored rectangle of height <code>HEADER_LINE_HEIGHT</code>
     * on the top part of the space reserved for the header, which has height
     * <code>HEADER_HEIGHT</code>.
     * <code>HEADER_LINE_HEIGHT</code> must be smaller than <code>HEADER_HEIGHT</code>
     * so that there is some padding space left naturally before the page content starts.
     * @param contentStream
     * @throws IOException
     */
    @Override
    public void writeHeader(@NonNull PDPageContentStream contentStream) throws IOException {

        PDRectangle rect = new PDRectangle(
                pdfBoxContext.getPageMarginHorizontal(),
                pdfBoxContext.getPageSize().getHeight() - pdfBoxContext.getPageMarginVertical() - HEADER_LINE_HEIGHT,
                pdfBoxContext.getPageSize().getWidth() - 2 * pdfBoxContext.getPageMarginHorizontal(),
                HEADER_LINE_HEIGHT
        );
        contentStream.setNonStrokingColor(pdfBoxContext.getColorManager().getColor(PdfColorStyle.Outline));
        contentStream.addRect(rect.getLowerLeftX(), rect.getLowerLeftY(), rect.getWidth(), rect.getHeight());
        contentStream.fill();
        contentStream.setNonStrokingColor(pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default));
    }


    /**
     * Footer with height <code>FOOTER_HEIGHT</code> which consists of some padding of height
     * <code>FOOTER_PADDING</code>, a rectangle of height <code>FOOTER_LINE_HEIGHT</code>, followed
     * by a text message.
     * @param contentStream
     * @throws IOException
     */
    @Override
    public void writeFooter(@NonNull PDPageContentStream contentStream) throws IOException {
        PDRectangle rect = new PDRectangle(
                pdfBoxContext.getPageMarginHorizontal(),
                pdfBoxContext.getPageMarginVertical() - FOOTER_LINE_HEIGHT,
                pdfBoxContext.getPageSize().getWidth() - 2 * pdfBoxContext.getPageMarginHorizontal(),
                FOOTER_LINE_HEIGHT
        );
        contentStream.setNonStrokingColor(pdfBoxContext.getColorManager().getColor(PdfColorStyle.Outline));
        contentStream.addRect(rect.getLowerLeftX(), rect.getLowerLeftY(), rect.getWidth(), rect.getHeight());
        contentStream.fill();
        contentStream.setNonStrokingColor(pdfBoxContext.getColorManager().getColor(PdfColorStyle.Default));

        final PdfFontSpec fontSpec = pdfBoxContext.getFontManager().getFont(PdfFontStyle.Footer);
        contentStream.beginText();
        contentStream.newLineAtOffset(pdfBoxContext.getPageMarginHorizontal(),
                pdfBoxContext.getPageMarginVertical() - FOOTER_PADDING - FOOTER_LINE_HEIGHT);
        contentStream.setFont(fontSpec.getFont(), fontSpec.getSize());
        contentStream.showText(footerText);
        contentStream.endText();
    }

    @Override
    public float getHeaderHeight() {
        return HEADER_HEIGHT;
    }

    @Override
    public float getFooterHeight() {
        return FOOTER_HEIGHT;
    }
}
