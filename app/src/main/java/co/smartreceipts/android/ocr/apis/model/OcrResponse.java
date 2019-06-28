package co.smartreceipts.android.ocr.apis.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class OcrResponse implements Serializable {

    @SerializedName("totalAmount")
    private OcrResponseField<Double> totalAmount;

    @SerializedName("taxAmount")
    private OcrResponseField<Double> taxAmount;

    @SerializedName("currency")
    private OcrResponseField<String> currency;

    @SerializedName("date")
    private OcrResponseField<String> date;

    @SerializedName("merchantName")
    private OcrResponseField<String> merchantName;

    @SerializedName("merchantTypes")
    private OcrResponseField<List<String>> merchantTypes;

    @SerializedName("confidenceLevel")
    private Double confidenceLevel;

    @SerializedName("error")
    private String error;

    public OcrResponse() {}

    public OcrResponse(OcrResponseField<Double> totalAmount, OcrResponseField<Double> taxAmount,
                       OcrResponseField<String> currency, OcrResponseField<String> date,
                       OcrResponseField<String> merchantName, OcrResponseField<List<String>> merchantTypes,
                       Double confidenceLevel, String error) {
        this.totalAmount = totalAmount;
        this.taxAmount = taxAmount;
        this.currency = currency;
        this.date = date;
        this.merchantName = merchantName;
        this.merchantTypes = merchantTypes;
        this.confidenceLevel = confidenceLevel;
        this.error = error;
    }

    @Nullable
    public OcrResponseField<Double> getTotalAmount() {
        return totalAmount;
    }

    @Nullable
    public OcrResponseField<Double> getTaxAmount() {
        return taxAmount;
    }

    @Nullable
    public OcrResponseField<String> getCurrency() {
        return currency;
    }

    @Nullable
    public OcrResponseField<String> getDate() {
        return date;
    }

    @Nullable
    public OcrResponseField<String> getMerchant() {
        return merchantName;
    }

    @Nullable
    public OcrResponseField<List<String>> getMerchantTypes() {
        return merchantTypes;
    }

    @Nullable
    public Double getConfidenceLevel() {
        return confidenceLevel;
    }

    @Nullable
    public String getError() {
        return error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OcrResponse)) return false;

        OcrResponse that = (OcrResponse) o;

        if (totalAmount != null ? !totalAmount.equals(that.totalAmount) : that.totalAmount != null)
            return false;
        if (taxAmount != null ? !taxAmount.equals(that.taxAmount) : that.taxAmount != null)
            return false;
        if (currency != null ? !currency.equals(that.currency) : that.currency != null)
            return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (merchantName != null ? !merchantName.equals(that.merchantName) : that.merchantName != null)
            return false;
        if (merchantTypes != null ? !merchantTypes.equals(that.merchantTypes) : that.merchantTypes != null)
            return false;
        if (confidenceLevel != null ? !confidenceLevel.equals(that.confidenceLevel) : that.confidenceLevel != null)
            return false;
        return error != null ? error.equals(that.error) : that.error == null;

    }

    @Override
    public int hashCode() {
        int result = totalAmount != null ? totalAmount.hashCode() : 0;
        result = 31 * result + (taxAmount != null ? taxAmount.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (merchantName != null ? merchantName.hashCode() : 0);
        result = 31 * result + (merchantTypes != null ? merchantTypes.hashCode() : 0);
        result = 31 * result + (confidenceLevel != null ? confidenceLevel.hashCode() : 0);
        result = 31 * result + (error != null ? error.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OcrResponse{" +
                "totalAmount=" + totalAmount +
                ", taxAmount=" + taxAmount +
                ", currency='" + currency + '\'' +
                ", date='" + date + '\'' +
                ", confidenceLevel=" + confidenceLevel +
                ", error='" + error + '\'' +
                '}';
    }
}
