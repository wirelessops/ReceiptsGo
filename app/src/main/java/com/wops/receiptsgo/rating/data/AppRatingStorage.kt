package com.wops.receiptsgo.rating.data

import io.reactivex.Single

interface AppRatingStorage {

    fun readAppRatingData(): Single<AppRatingModel>

    fun incrementLaunchCount()

    fun setDontShowRatingPromptMore()

    fun prorogueRatingPrompt(prorogueLaunches: Int)

    fun crashOccurred()

    fun setInAppReviewShown()
}
