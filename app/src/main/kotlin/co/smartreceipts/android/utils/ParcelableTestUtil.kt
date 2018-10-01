package co.smartreceipts.android.utils

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.VisibleForTesting

/**
 * From https://proandroiddev.com/testing-parcelize-114510f44c9a
 */

@VisibleForTesting
inline fun <reified R : Parcelable> R.testParcel(): R {
    val bytes = marshallParcelable(this)
    return unmarshallParcelable(bytes)
}

@VisibleForTesting
inline fun <reified R : Parcelable> marshallParcelable(parcelable: R): ByteArray {
    val bundle = Bundle().apply { putParcelable(R::class.java.name, parcelable) }
    return marshall(bundle)
}

@VisibleForTesting
fun marshall(bundle: Bundle): ByteArray =
    Parcel.obtain().use {
        it.writeBundle(bundle)
        it.marshall()
    }

@VisibleForTesting
inline fun <reified R : Parcelable> unmarshallParcelable(bytes: ByteArray): R = unmarshall(bytes)
    .readBundle()
    .run {
        classLoader = R::class.java.classLoader
        getParcelable(R::class.java.name)
    }

@VisibleForTesting
fun unmarshall(bytes: ByteArray): Parcel =
    Parcel.obtain().apply {
        unmarshall(bytes, 0, bytes.size)
        setDataPosition(0)
    }

@VisibleForTesting
private fun <T> Parcel.use(block: (Parcel) -> T): T =
    try {
        block(this)
    } finally {
        this.recycle()
    }