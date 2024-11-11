package com.wops.receiptsgo.apis

import com.wops.receiptsgo.apis.gson.SmartReceiptsGsonBuilder
import com.wops.receiptsgo.apis.moshi.SmartReceiptsMoshiBuilder
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

        for (annotation in annotations) {
            if (annotation.annotationClass == MoshiType::class) {
                return moshi.responseBodyConverter(type, annotations, retrofit)
            }
        }

        return gson.responseBodyConverter(type, annotations, retrofit)
    }

    override fun requestBodyConverter(
        type: Type, parameterAnnotations: Array<Annotation>, methodAnnotations: Array<Annotation>, retrofit: Retrofit
    ): Converter<*, RequestBody>? {

        for (annotation in methodAnnotations) {
            if (annotation.annotationClass == MoshiType::class) {
                return moshi.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
            }
        }

        return gson.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
    }
}