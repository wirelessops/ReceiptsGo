package co.smartreceipts.android.rating.data;

import io.reactivex.Single;

public interface AppRatingStorage {

    Single<AppRatingModel> readAppRatingData();

    void incrementLaunchCount();

    void setDontShowRatingPromptMore();

    void prorogueRatingPrompt(int prorogueLaunches);

    void crashOccurred();
}
