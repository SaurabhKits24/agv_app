package com.reeman.points.request

import com.reeman.points.request.service.ApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

object ApiClient {

    private const val TAG = "ApiClient"

    private val apiService: ApiService

    init {
        val httpClientBuilder = OkHttpClient.Builder()
       httpClientBuilder.addInterceptor(LoggingInterceptor())
        val retrofit = Retrofit.Builder()
            .baseUrl("http://navi.rmbot.cn")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(httpClientBuilder.build())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    fun getApiService(): ApiService = apiService

    private class LoggingInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()

            Timber.tag(TAG).w("Request URL: %s", request.url())
            Timber.tag(TAG).w("Request Method: %s", request.method())

            request.body()?.let {
                val buffer = Buffer()
                it.writeTo(buffer)
                Timber.tag(TAG).w("Request Body: %s", buffer.readUtf8())
            }

            // 执行请求
            val response = chain.proceed(request)

            // 打印响应信息
            Timber.tag(TAG).w("Response Code: %s", response.code())


            return response
        }
    }

}
