package com.reeman.agv.plugins;

import android.util.Log;

import com.reeman.commons.constants.Constants;
import com.reeman.agv.request.ServiceFactory;
import com.reeman.commons.model.request.LoginResponse;
import com.reeman.agv.request.service.RobotService;
import com.reeman.agv.request.url.API;
import com.reeman.commons.utils.AESUtil;
import com.reeman.commons.utils.MMKVManager;
import com.reeman.commons.utils.SpManager;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class RetrofitClient {

    private static final String WAN_PATH = "http://navi.rmbot.cn/OpenAPISpring/ros/locations/find";

    private static final Retrofit client;

    private static final OkHttpClient okHttpClient;

    public static final String TAG = RetrofitClient.class.getSimpleName();

    static {
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
            okHttpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory())
                    .hostnameVerifier((hostname, session) -> true)
                    .addInterceptor(chain -> {
                        Request request = chain.request();
                        String accessToken = SpManager.getInstance().getString(Constants.KEY_ACCESS_TOKEN, null);
                        if (accessToken != null) {
                            Headers headers = request.headers().newBuilder().add("Authorization", accessToken).build();
                            request = request.newBuilder().headers(headers).build();
                        }
                        Response response = chain.proceed(request);
                        Log.w(TAG, request + "=====" + response);

                        if (response.code() == 401 && !request.url().toString().contains("tokens")) {
                            Timber.w("token过期，重新登录");
                            Map<String, String> map = new HashMap<>();
                            map.put("account", Constants.DEFAULT_ACCOUNT);
                            map.put("password", Constants.DEFAULT_PASSWORD);
                            RobotService robotService = ServiceFactory.getRobotService();
                            Call<LoginResponse> loginResponseCall = robotService.loginSync(API.tokenAPI(), map);
                            LoginResponse loginResponse = loginResponseCall.execute().body();
                            if (loginResponse == null || loginResponse.data == null || loginResponse.data.result == null)
                                return response;
                            Timber.w("重新登录成功,重新发起请求");
                            response.close();
                            SpManager.getInstance().edit().putString(Constants.KEY_ACCESS_TOKEN, loginResponse.data.result.accessToken).apply();
                            Headers headers = request.headers().newBuilder().add("Authorization", loginResponse.data.result.accessToken).build();
                            Request newRequest = request.newBuilder().headers(headers).build();
                            Log.w("重新请求", newRequest.toString());
                            return chain.proceed(newRequest);
                        } else if (response.code() >=400 && !request.url().toString().contains("tokens") && request.url().toString().contains("navi.rmbot.cn")) {
                            response.close();
                            String s = request.url().toString().replace("navi.rmbot.cn", "slam.rmbot.cn");
                            Request newRequest = request.newBuilder().url(s).build();
                            Response newResponse = chain.proceed(newRequest);
                            Log.w(TAG, newRequest + "=====" + newResponse);
                            return newResponse;
                        }
                        return response;
                    })
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        client = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .baseUrl(WAN_PATH + "/")
                .client(getOkHttpClient())
                .build();
    }


    private RetrofitClient() {

    }

    public static Retrofit getClient() {
        return client;
    }

    public static OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }
}
