package co.smartreceipts.android.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AutoCompleteMetadata(
    /**
     *  Checks if the name of object should be shown in auto complete results
     */
    var isNameHiddenFromAutoComplete: Boolean,
    /**
     *  Checks if the comment of object should be shown in auto complete results
     */
    var isCommentHiddenFromAutoComplete: Boolean,
    /**
     *  Checks if the location of object should be shown in auto complete results
     */
    var isLocationHiddenFromAutoComplete: Boolean,
    /**
     *  Checks if the cost center of object should be shown in auto complete results
     */
    var isCostCenterHiddenFromAutoComplete: Boolean
) : Parcelable
