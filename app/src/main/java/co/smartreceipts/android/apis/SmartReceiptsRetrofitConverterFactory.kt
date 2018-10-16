package co.smartreceipts.android.apis

import co.smartreceipts.android.apis.gson.SmartReceiptsGsonBuilder
import co.smartreceipts.android.apis.moshi.SmartReceiptsMoshiBuilder
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.lang.reflect.Type
import javax.inject.Inject


class SmartReceiptsRetrofitConverterFactory @Inject constructor(moshiBuilder: SmartReceiptsMoshiBuilder, gsonBuilder: SmartReceiptsGsonBuilder) : Converter.Factory() {

    @Retention(AnnotationRetention.RUNTIME)
    annotation class MoshiType

    @Retention(AnnotationRetention.RUNTIME)
    annotation class GsonType

    private val moshi: MoshiConverterFactory = MoshiConverterFactory.create(moshiBuilder.create())
    private val gson: GsonConverterFactory = GsonConverterFactory.create(gsonBuilder.create())

    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<ResponseBody, *>? {
        for (annotation in annotations) {

            if (annotation.annotationClass == MoshiType::class) {
                return moshi.responseBodyConverter(type, annotations, retrofit)
            }

            if (annotation.annotationClass == GsonType::class) {
                return gson.responseBodyConverter(type, annotations, retrofit)
            }

        }

        return null
    }
}