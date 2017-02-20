package com.sky.sky.danmakudemo;

import com.sky.sky.danmakudemo.constants.Constants;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by BlueSky on 17/2/17.
 */

public class HttpUtils {
    private static  HttpUtils httpUtils=null;
    private Retrofit retrofit;
    public static HttpUtils getHttpUtils(){
        if (httpUtils==null){
            synchronized (HttpUtils.class){
                if (httpUtils==null){
                    httpUtils=new HttpUtils();
                }
            }
        }
        return httpUtils;
    }

    public HttpUtils() {
        retrofit =new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    public void getVideoList(Subscriber<ResponseBody> subscriber){
        HttpApiService httpApiService = retrofit.create(HttpApiService.class);
        Observable<ResponseBody> videoInfoObservable = httpApiService.getVideoList();
        videoInfoObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);


    }
}
