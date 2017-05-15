package co.smartreceipts.android.ad;

import android.app.Activity;
import android.support.annotation.NonNull;

public interface AdPresenter {

    void onActivityCreated(@NonNull Activity activity);

    void onResume();

    void onPause();

    void onDestroy();

    void onSuccessPlusPurchase();
}
