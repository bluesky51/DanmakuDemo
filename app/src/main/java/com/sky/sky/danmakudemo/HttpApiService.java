package com.sky.sky.danmakudemo;

import com.sky.sky.danmakudemo.constants.Constants;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by BlueSky on 17/2/17.
 */

public interface HttpApiService {

    @GET(Constants.PATH)
    Observable<ResponseBody> getVideoList();
}