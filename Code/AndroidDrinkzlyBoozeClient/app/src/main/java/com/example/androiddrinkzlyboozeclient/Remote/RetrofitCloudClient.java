package com.example.androiddrinkzlyboozeclient.Remote;

//import retrofit2.Retfofit;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class RetrofitCloudClient {
    private static Retrofit instance;
    public static Retrofit getInstance(){
        if(instance==null)
            instance = new Retrofit.Builder()
                    .baseUrl("")
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        return instance;
    }

}
