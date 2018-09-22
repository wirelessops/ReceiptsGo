package co.smartreceipts.android.purchases.apis.purchases;

import java.util.Date;

public class PurchaseResponse {

    public MobileAppPurchase mobile_app_purchase;

    public static final class MobileAppPurchase {
        public String id;
        private String user_id;
        private String pay_service;
        private String purchase_id;
        private Date purchase_time;
        private String status;
        private Date created_at;
    }

}
