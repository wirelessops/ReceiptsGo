package co.smartreceipts.android.rating.data

class AppRatingModel(
    val canShow: Boolean,
    val isCrashOccurred: Boolean,
    val launchCount: Int,
    val additionalLaunchThreshold: Int,
    val installTime: Long,
    val inAppReviewShown : Boolean,
)