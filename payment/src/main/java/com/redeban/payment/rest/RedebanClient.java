package com.redeban.payment.rest;

import android.content.Context;

import com.redeban.payment.util.RedebanUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.redeban.payment.util.RedebanUtils.SERVER_DEV_URL;
import static com.redeban.payment.util.RedebanUtils.SERVER_PROD_URL;

/**
 * Created by mmucito on 13/09/17.
 */

public class RedebanClient {

    private static Retrofit retrofit = null;
    static OkHttpClient.Builder builder = new OkHttpClient().newBuilder();

    public static Retrofit getClient(Context mContext, boolean is_dev, final String REDEBAN_CLIENT_APP_CODE, final String REDEBAN_CLIENT_APP_KEY) {
        if (retrofit==null) {
            String SERVER_URL;
            if (is_dev){
                SERVER_URL = SERVER_DEV_URL;
            }else{
                SERVER_URL = SERVER_PROD_URL;
            }

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            builder.connectTimeout(30, TimeUnit.SECONDS);
            builder.readTimeout(30, TimeUnit.SECONDS);
            builder.writeTimeout(30, TimeUnit.SECONDS);

            builder.addInterceptor(new Interceptor() {
                @Override public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request().newBuilder().addHeader("Content-Type", "application/json")
                            .addHeader("Auth-Token", RedebanUtils.getAuthToken(REDEBAN_CLIENT_APP_CODE, REDEBAN_CLIENT_APP_KEY))
                            .build();
                    return chain.proceed(request);
                }
            });
            if (is_dev)
                builder.addInterceptor(logging);

            OkHttpClient client = builder.build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(SERVER_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}

