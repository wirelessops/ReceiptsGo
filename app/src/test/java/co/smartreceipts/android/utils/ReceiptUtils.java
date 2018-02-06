package co.smartreceipts.android.utils;

import android.content.Context;

import junit.framework.Assert;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.TimeZone;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.CategoryBuilderFactory;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;

public class ReceiptUtils {

    public static class Constants {
        public static final int ID = 0;
        public static final int INDEX = 4;
        public static final Category CATEGORY = new CategoryBuilderFactory().setName("Lunch").setCode("LNCH").build();
        public static final String COMMENT = "";
        public static final String CURRENCY_CODE = "USD";
        public static final PriceCurrency CURRENCY = PriceCurrency.getInstance(CURRENCY_CODE);
        public static final long DATE_MILLIS = 1409703721000L; // 09/02/2014 @ 8:22EDT
        public static final Date DATE = new Date(DATE_MILLIS);
        public static final String SLASH_FORMATTED_DATE = "Sep/02/2014";
        public static final String DASH_FORMATTED_DATE = "Sep-02-2014";
        public static final String EXTRA1 = "extra1";
        public static final String EXTRA2 = "extra2";
        public static final String EXTRA3 = "extra3";
        public static final File IMAGE_FILE = new File("/Android/data/wb.receipts/files/Report/img.jpg");
        public static final String IMAGE_FILE_NAME = "img.jpg";
        public static final File PDF_FILE = new File("/Android/data/wb.receipts/files/Report/pdf.pdf");
        public static final String PDF_FILE_NAME = "pdf.pdf";
        public static final boolean IS_REIMBURSABLE = true;
        public static final boolean IS_FULLPAGE = false;
        public static final boolean IS_SELECTED = true;
        public static final String NAME = "Name";
        public static final double PRICE = 12.55d;
        public static final String DECIMAL_FORMATTED_PRICE = "12.55";
        public static final String CURRENCY_FORMATTED_PRICE = "$12.55";
        public static final double TAX = 0.37d;
        public static final String DECIMAL_FORMATTED_TAX = "0.37";
        public static final String CURRENCY_FORMATTED_TAX = "$0.37";
        public static final TimeZone TIMEZONE = TimeZone.getDefault();
        public static final String TIMEZONE_CODE = TIMEZONE.getID();
    }

    public static ReceiptBuilderFactory newDefaultReceiptBuilderFactory(Context context) {
        final File img = createRoboElectricStubFile(context, Constants.IMAGE_FILE_NAME);
        final ReceiptBuilderFactory factory = new ReceiptBuilderFactory(Constants.ID);
        factory.setTrip(TripUtils.newDefaultTrip());
        factory.setName(Constants.NAME);
        factory.setPrice(Constants.PRICE);
        factory.setTax(Constants.TAX);
        factory.setCurrency(Constants.CURRENCY);
        factory.setDate(Constants.DATE);
        factory.setTimeZone(Constants.TIMEZONE);
        factory.setCategory(Constants.CATEGORY);
        factory.setComment(Constants.COMMENT);
        factory.setIsReimbursable(Constants.IS_REIMBURSABLE);
        factory.setIsFullPage(Constants.IS_FULLPAGE);
        factory.setFile(img);
        factory.setIndex(Constants.INDEX);
        factory.setExtraEditText1(Constants.EXTRA1);
        factory.setExtraEditText2(Constants.EXTRA2);
        factory.setExtraEditText3(Constants.EXTRA3);
        // TODO: Add Payment Method DefaultBuilderHere
        return factory;
    }



    public static Receipt newDefaultReceipt(Context context) {
        return newDefaultReceiptBuilderFactory(context).build();
    }

    public static File createRoboElectricStubFile(Context context, String filename) {
        final File root = context.getExternalFilesDir(null);
        final File newFile = new File(root, filename);
        try {
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            return newFile;
        }
        catch (IOException e) {
            return root; // Stub fallback case
        }
    }

    public static void assertFieldEquality(Receipt receipt1, Receipt receipt2) {
        assertFieldEqualityHelper(receipt1, receipt2);
        Assert.assertEquals(receipt1.getTrip(), receipt2.getTrip());
        Assert.assertEquals(receipt1.getDate(), receipt2.getDate());
    }

    public static void assertFieldEqualityIgnoringParent(Receipt receipt1, Receipt receipt2) {
        assertFieldEqualityHelper(receipt1, receipt2);
        Assert.assertEquals(receipt1.getDate(), receipt2.getDate());
    }

    public static void assertFieldEqualityWithDateFuzzing(Receipt receipt1, Receipt receipt2) {
        assertFieldEqualityHelper(receipt1, receipt2);
        Assert.assertEquals(receipt1.getTrip(), receipt2.getTrip());
        Assert.assertTrue(Math.abs(receipt1.getDate().getTime() - receipt2.getDate().getTime()) < 100L);
    }

    private static void assertFieldEqualityHelper(Receipt receipt1, Receipt receipt2) {
        Assert.assertEquals(receipt1.getComment(), receipt2.getComment());
        Assert.assertEquals(receipt1.getCategory(), receipt2.getCategory());
        Assert.assertEquals(receipt1.getExtraEditText1(), receipt2.getExtraEditText1());
        Assert.assertEquals(receipt1.getExtraEditText2(), receipt2.getExtraEditText2());
        Assert.assertEquals(receipt1.getExtraEditText3(), receipt2.getExtraEditText3());
        Assert.assertEquals(receipt1.getFile(), receipt2.getFile());
        Assert.assertEquals(receipt1.isReimbursable(), receipt2.isReimbursable());
        Assert.assertEquals(receipt1.isFullPage(), receipt2.isFullPage());
        Assert.assertEquals(receipt1.isSelected(), receipt2.isSelected());
        Assert.assertEquals(receipt1.getName(), receipt2.getName());
        Assert.assertEquals(receipt1.getPrice(), receipt2.getPrice());
        Assert.assertEquals(receipt1.getTax(), receipt2.getTax());
        Assert.assertEquals(receipt1.getTimeZone(), receipt2.getTimeZone());
        Assert.assertEquals(receipt1.getPaymentMethod(), receipt2.getPaymentMethod());
    }

    /**
     * We do not guarantee that indices are set, but this test still performs the full set
     */
    public static void assertFieldEqualityPlusIdAndIndex(Receipt receipt1, Receipt receipt2) {
        assertFieldEquality(receipt1, receipt2);
        Assert.assertEquals(receipt1.getId(), receipt2.getId());
        Assert.assertEquals(receipt1.getIndex(), receipt2.getIndex());
    }

    public static void assertFieldEqualityWithDateFuzzingPlusIdAndIndex(Receipt receipt1, Receipt receipt2) {
        assertFieldEqualityWithDateFuzzing(receipt1, receipt2);
        Assert.assertEquals(receipt1.getId(), receipt2.getId());
        Assert.assertEquals(receipt1.getIndex(), receipt2.getIndex());
    }

}
