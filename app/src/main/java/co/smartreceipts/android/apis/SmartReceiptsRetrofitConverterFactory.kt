package co.smartreceipts.android.apis

import co.smartreceipts.android.apis.gson.SmartReceiptsGsonBuilder
import co.smartreceipts.android.apis.moshi.SmartReceiptsMoshiBuilder
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.lang.reflect.Type
import javax.inject.Inject


class SmartReceiptsRetrofitConverterFactory @Inject constructor(
    moshiBuilder: SmartReceiptsMoshiBuilder,
    gsonBuilder: SmartReceiptsGsonBuilder
) : Converter.Factory() {

    @Retention(AnnotationRetention.RUNTIME)
    annotation class MoshiType

    private val moshi: MoshiConverterFactory = MoshiConverterFactory.create(moshiBuilder.create())
    private val gson: GsonConverterFactory = GsonConverterFactory.create(gsonBuilder.create())

    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<ResponseBody, *>? {
        var isMoshi = false

        for (annotation in annotations) {
            if (annotation.annotationClass == MoshiType::class) {
                isMoshi = true
            }
        }

        return if (isMoshi) {
            moshi.responseBodyConverter(type, annotations, retrofit)
        } else {
            gson.responseBodyConverter(type, annotations, retrofit)
        }
    }

    override fun requestBodyConverter(
        type: Type, parameterAnnotations: Array<Annotation>, methodAnnotations: Array<Annotation>, retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        var isMoshi = false

        for (annotation in parameterAnnotations) {
            if (annotation.annotationClass == MoshiType::class) {
                isMoshi = true
            }
        }

        return if (isMoshi) {
            moshi.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
        } else {
            gson.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
        }
    }
}