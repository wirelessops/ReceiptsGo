package co.smartreceipts.android.purchases.apis.purchases;

public class PurchaseResponse {

    private MobileAppPurchase mobile_app_purchase;

    public static final class MobileAppPurchase {
        private String mobile_app_purchase;
        private String id;
        private String user_id;
        private String pay_service;
        private String purchase_id;
        private long purchase_time;
        private String status;
        private long created_at;
    }

}
