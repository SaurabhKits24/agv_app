package com.reeman.agv.elevator.request;


import com.reeman.commons.constants.Constants;
import com.reeman.agv.elevator.BuildConfig;
import com.reeman.agv.elevator.request.service.ApiService;
import com.reeman.commons.utils.AESUtil;
import com.reeman.commons.utils.MMKVManager;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class ApiClient {

    private final String TAG = BuildConfig.ELEVATOR_DIR;
    private final ApiService apiService;
    private static volatile ApiClient instance;

    public ApiClient() {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15,TimeUnit.SECONDS)
                .addInterceptor(new RetryInterceptor(1, 2))
                .addInterceptor(new CustomHeaderInterceptor())
                .addInterceptor(new LoggingInterceptor());
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            httpClientBuilder.sslSocketFactory(sslContext.getSocketFactory());
            httpClientBuilder.hostnameVerifier((hostname, session) -> true); // 忽略主机名验证
        } catch (Exception e) {
            e.printStackTrace();
        }
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.ELEVATOR_URL)
//                .baseUrl("")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(httpClientBuilder.build())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static ApiClient getInstance() {
        if (instance == null) {
            synchronized (ApiClient.class) {
                if (instance == null) {
                    instance = new ApiClient();
                }
            }
        }
        return instance;
    }

    public ApiService getApiService() {
        return apiService;
    }


    private static class CustomHeaderInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request.Builder requestBuilder = originalRequest.newBuilder();
            requestBuilder.header("SECURITY-KEY", Constants.DEFAULT_SECURITY_KEY);
            requestBuilder.header("TENANT_ID", Constants.DEFAULT_TENANT_ID);
            Request newRequest = requestBuilder.build();
            return chain.proceed(newRequest);
        }
    }

    public class LoggingInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            // 打印请求信息
            Timber.tag(TAG).w("Request URL: %s", request.url());
            Timber.tag(TAG).w("Request Method: %s", request.method());
            Timber.tag(TAG).v("Request Headers: %s", request.headers());

            // 打印请求体信息
            if (request.body() != null) {
                Buffer buffer = new Buffer();
                request.body().writeTo(buffer);
                Timber.tag(TAG).w("Request Body: %s", buffer.readUtf8());
            }

            // 执行请求
            Response response = chain.proceed(request);

            // 打印响应信息
            Timber.tag(TAG).w("Response Code: %s", response.code());
            Timber.tag(TAG).v("Response Headers: %s", response.headers());

            // 打印响应体信息
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // 读取整个响应体
                Buffer buffer = source.buffer();
                Timber.tag(TAG).w("Response Body: %s", buffer.clone().readUtf8());
            }

            return response;
        }
    }

    private class RetryInterceptor implements Interceptor {

        private final int maxRetries;
        private final int retryDelaySeconds;

        public RetryInterceptor(int maxRetries, int retryDelaySeconds) {
            this.maxRetries = maxRetries;
            this.retryDelaySeconds = retryDelaySeconds;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = null;
            IOException exception = null;
            for (int retry = 0; retry < maxRetries; retry++) {
                try {
                    response = chain.proceed(request);
                    if (response.isSuccessful()) {
                        break;
                    }
                } catch (IOException e) {
                    exception = e;
                }

                try {
                    Timber.tag(TAG).w("请求失败,网络错误,自动重试中...");
                    Thread.sleep(retry * retryDelaySeconds * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (response == null && exception != null) {
                throw exception;
            }

            return response;
        }
    }
}
