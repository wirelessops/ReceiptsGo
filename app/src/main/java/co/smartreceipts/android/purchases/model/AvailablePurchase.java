package co.smartreceipts.android.purchases.model;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class AvailablePurchase  {

    @SerializedName("productId")
    private String productId;

    @SerializedName("type")
    private String type;

    @SerializedName("price")
    private String price;

    @SerializedName("price_amount_micros")
    private long price_amount_micros;

    @SerializedName("price_currency_code")
    private String price_currency_code;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @Nullable
    public InAppPurchase getInAppPurchase() {
        return InAppPurchase.from(productId);
    }

    @Nullable
    public String getType() {
        return type;
    }

    @Nullable
    public String getPrice() {
        return price;
    }

    public long getPriceAmountMicros() {
        return price_amount_micros;
    }

    @Nullable
    public String getPriceCurrencyCode() {
        return price_currency_code;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AvailablePurchase)) return false;

        AvailablePurchase that = (AvailablePurchase) o;

        if (price_amount_micros != that.price_amount_micros) return false;
        if (productId != null ? !productId.equals(that.productId) : that.productId != null)
            return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (price != null ? !price.equals(that.price) : that.price != null) return false;
        if (price_currency_code != null ? !price_currency_code.equals(that.price_currency_code) : that.price_currency_code != null)
            return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        return description != null ? description.equals(that.description) : that.description == null;

    }

    @Override
    public int hashCode() {
        int result = productId != null ? productId.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + (int) (price_amount_micros ^ (price_amount_micros >>> 32));
        result = 31 * result + (price_currency_code != null ? price_currency_code.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AvailablePurchase{" +
                "productId='" + productId + '\'' +
                ", price='" + price + '\'' +
                ", price_currency_code='" + price_currency_code + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
