package co.smartreceipts.android.apis.okhttp

import android.content.Context
import co.smartreceipts.android.BuildConfig
import co.smartreceipts.core.identity.store.IdentityStore
import co.smartreceipts.analytics.log.Logger
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor


/**
 * Builds an [OkHttpClient] based on our configuration needs. We leverage this factory in order to
 * allow easy integration with Dagger's Lazy function so that our call to [Context.getCacheDir] can
 * be performed in the background
 */
class SmartReceiptsOkHttpClientFactory(private val context: Context,
                                       private val identityStore: IdentityStore
) {


    fun newInstance() : OkHttpClient {
        val okHttpClientBuilder = OkHttpClient.Builder()
        okHttpClientBuilder.addInterceptor(SmartReceiptsAuthenticatedRequestInterceptor(identityStore))
        okHttpClientBuilder.addInterceptor(TrafficStatsRequestInterceptor())
        if (BuildConfig.DEBUG) {
            // If we're using a debug build, add logging
            val loggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Logger.debug(HttpLoggingInterceptor::class.java, message)
                }
            })
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpClientBuilder.addInterceptor(loggingInterceptor)
        }
        okHttpClientBuilder.cache(Cache(context.cacheDir, CACHE_SIZE_BYTES.toLong()))

        return okHttpClientBuilder.build()
    }

    companion object {
        private const val CACHE_SIZE_BYTES = 1024 * 1024 * 5 // 5MB
    }
}