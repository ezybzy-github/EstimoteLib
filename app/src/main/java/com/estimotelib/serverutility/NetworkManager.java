package com.estimotelib.serverutility;

import android.content.Context;

import com.estimotelib.api.API;
import com.estimotelib.api.EstimoteLibApi;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkManager implements API {
    Retrofit retrofit;
    public EstimoteLibApi mEstimoteLibApi;

    private static NetworkManager ourInstance = null;

    public static NetworkManager getInstance(final Context context) {
        if (ourInstance == null) {
            ourInstance = new NetworkManager(context);
        }
        return ourInstance;
    }

    private NetworkManager(final Context context) {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(0, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        mEstimoteLibApi = retrofit.create(EstimoteLibApi.class);
    }

    public EstimoteLibApi getmPykupzAPI() {
        return mEstimoteLibApi;
    }
}
