package co.smartreceipts.android.workers.reports.pdf.misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.workers.reports.pdf.utils.PdfBoxUtils;
import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontSpec;
import co.smartreceipts.android.workers.reports.pdf.utils.HeavyHandedReplaceIllegalCharacters;


public class ColumnWidthCalculator<DataType> {

    private static final float EPSILON = 0.00001f;

    private final List<DataType> mList;
    private final List<Column<DataType>> mColumns;
    private final float mAvailableWidth;
    private final float mCellPadding;
    private final PdfFontSpec mFontHeader;
    private final PdfFontSpec mFontContent;


    /**
     * @param list
     * @param columns
     * @param availableWidth
     * @param fontHeader
     * @param fontContent
     */
    public ColumnWidthCalculator(List<DataType> list,
                                 List<Column<DataType>> columns,
                                 float availableWidth,
                                 float cellPadding,
                                 PdfFontSpec fontHeader,
                                 PdfFontSpec fontContent) {

        mList = list;
        mColumns = columns;
        mAvailableWidth = availableWidth;
        mFontHeader = fontHeader;
        mFontContent = fontContent;
        mCellPadding = cellPadding;
    }


    public float[] calculate() throws IOException, TooManyColumnsException {

        float availableWidthExcludingPadding = mAvailableWidth - 2 * mColumns.size() * mCellPadding;

        float[] widths = new float[mColumns.size()];
        ArrayList<ColumnAttributes> attrs = new ArrayList<ColumnAttributes>(mColumns.size());

        for (int i = 0; i < mColumns.size(); i++) {
            attrs.add(new ColumnAttributes(mColumns.get(i).getHeader(), mList, i));
        }

        // TOO MANY COLUMNS CHECK
        // If columns do not fit with their min mWidth, abort
        float sumCheck = 0.0f;
        for (int i = 0; i < attrs.size(); i++) {
            float m = Math.max(attrs.get(i).mHeaderMinWidth, attrs.get(i).mContentMinWidth);
            sumCheck += m;
        }
        if (sumCheck > availableWidthExcludingPadding) {
            throw new TooManyColumnsException();
        }


        // FIRST ATTEMPT
        // If all columns fit with their max mWidth, assign maxWidth
        // and then redistribute evenly
        for (int i = 0; i < attrs.size(); i++) {
            float m = Math.max(attrs.get(i).mHeaderMaxWidth, attrs.get(i).mContentMaxWidth);
            widths[i] = m + 2 * mCellPadding;
        }

        if (sum(widths) < mAvailableWidth) {
            return distributeExtraSpaceEvenly(widths);
        }

        // SECOND ATTEMPT
        // Wrap titles (maintaining content unwrapped)
        for (int i = 0; i < attrs.size(); i++) {
            // The second condition defensively wraps the title (not necessarily to the minimum
            // header width, but just up to the content's max width)
            if (attrs.get(i).mHeaderBreakable
                    && attrs.get(i).mContentMaxWidth < attrs.get(i).mHeaderMaxWidth) {
                float m = Math.max(attrs.get(i).mHeaderMinWidth, attrs.get(i).mContentMaxWidth);
                widths[i] = m + 2 * mCellPadding;
            }
        }
        if (sum(widths) < mAvailableWidth) {
            return distributeExtraSpaceEvenly(widths);
        }


        // THIRD PASS
        // Wrap contents (and possibly further wrap title)
        boolean[] flex = new boolean[mColumns.size()];
        for (int i = 0; i < attrs.size(); i++) {
            float m = Math.max(attrs.get(i).mContentMinWidth, attrs.get(i).mHeaderMinWidth);
            float newWidth = m + 2 * mCellPadding;
            if (Math.abs(newWidth - widths[i]) > EPSILON) {
                widths[i] = newWidth;
                flex[i] = true;
            }
        }
        if (sum(widths) < mAvailableWidth) {
            return distributeExtraSpaceOnlyToColumns(widths, flex);
        }

        throw new TooManyColumnsException();
    }

    private float[] distributeExtraSpaceOnlyToColumns(float[] widths, boolean[] flex) {
        int nCols = 0;
        for (boolean b : flex) {
            if (b) {
                nCols++;
            }
        }

        float extraSpace = mAvailableWidth - sum(widths);

        for (int i = 0; i < widths.length; i++) {
            if (flex[i]) {
                widths[i] += extraSpace / nCols;
            }
        }

        return widths;
    }

    public float[] distributeExtraSpaceEvenly(float[] widths) {
        float extraSpace = mAvailableWidth - sum(widths);
        for (int j = 0; j < widths.length; j++) {
            widths[j] += extraSpace / mColumns.size();
        }
        return widths;
    }

    private float sum(float[] widths) {
        float sum = 0.0f;
        for (float width : widths) {
            sum += width;
        }
        return sum;

    }


    private class ColumnAttributes {
        float mHeaderMinWidth;
        float mHeaderMaxWidth;
        float mContentMinWidth;
        float mContentMaxWidth;
        boolean mHeaderBreakable;

        public ColumnAttributes(String header, List<DataType> list, int i) throws IOException {
            mHeaderMaxWidth = PdfBoxUtils.getStringWidth(header, mFontHeader);
            mHeaderMinWidth = PdfBoxUtils.getMaxWordWidth(header, mFontHeader);

            float maxOfAllStringWidths = 0.0f;  // the max string mWidth of all values (without breaking up the string)
            float minOfAllStringWidths = Float.MAX_VALUE;  // the min string mWidth of all values (without breaking up the string)
            float maxOfMaxWordWidths = 0.0f;    // the global max of the


            for (DataType dataType : list) {
                final String value = HeavyHandedReplaceIllegalCharacters.getSafeString(mColumns.get(i).getValue(dataType));

                float vWidth = PdfBoxUtils.getStringWidth(value, mFontContent);
                float vMaxWordWidth = PdfBoxUtils.getMaxWordWidth(value, mFontContent);

                if (vWidth > maxOfAllStringWidths) {
                    maxOfAllStringWidths = vWidth;
                }
                if (vWidth < minOfAllStringWidths) {
                    minOfAllStringWidths = vWidth;
                }

                if (vMaxWordWidth > maxOfMaxWordWidths) {
                    maxOfMaxWordWidths = vMaxWordWidth;
                }
            }

            // Finally, test out the footer value to see if that changes things
            final String value = HeavyHandedReplaceIllegalCharacters.getSafeString(mColumns.get(i).getFooter(list));
            float vWidth = PdfBoxUtils.getStringWidth(value, mFontContent);
            float vMaxWordWidth = PdfBoxUtils.getMaxWordWidth(value, mFontContent);

            if (vWidth > maxOfAllStringWidths) {
                maxOfAllStringWidths = vWidth;
            }
            if (vWidth < minOfAllStringWidths) {
                minOfAllStringWidths = vWidth;
            }
            if (vMaxWordWidth > maxOfMaxWordWidths) {
                maxOfMaxWordWidths = vMaxWordWidth;
            }

            mContentMaxWidth = maxOfAllStringWidths;
            mContentMinWidth = maxOfMaxWordWidths;

            mHeaderBreakable = header.contains(" ");
        }
    }
}
