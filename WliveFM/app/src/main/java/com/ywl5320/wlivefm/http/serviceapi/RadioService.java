package com.ywl5320.wlivefm.http.serviceapi;




import com.ywl5320.wlivefm.http.beans.HomePageBean;
import com.ywl5320.wlivefm.http.beans.LiveChannelBean;
import com.ywl5320.wlivefm.http.beans.LiveChannelPlaceBean;
import com.ywl5320.wlivefm.http.beans.LiveListBean;
import com.ywl5320.wlivefm.http.beans.LivePageBean;
import com.ywl5320.wlivefm.http.beans.TokenBean;
import com.ywl5320.wlivefm.http.httpentity.HttpResult;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by ywl on 2016/5/19.
 */
public interface RadioService {

    /**
     * 获取 token
     * @return
     */
    @POST("gettoken")
    Observable<HttpResult<TokenBean>> getToken();


    /**
     * 获取banner
     * @param token
     * @return
     */
    @GET("recommend/getrecommendedpage")
    Observable<HttpResult<HomePageBean>> getHomePage(@Query("token") String token);

    /**
     * 获取banner
     * @param token
     * @return
     */
    @GET("channels/getlivepage")
    Observable<HttpResult<LivePageBean>> getLivePage(@Query("token") String token);

    /**
     * 根据Id获取直播列表
     * @param token
     * @param channelPlaceId
     * @param limit
     * @param offset
     * @return
     */
    @GET("channels/getlivebyparam")
    Observable<HttpResult<LiveListBean>> getLiveByParam(@Query("token") String token, @Query("channelPlaceId") String channelPlaceId, @Query("limit") int limit, @Query("offset") int offset);

    /**
     * 获取省市台编号
     * @param token
     * @return
     */
    @GET("channels/getliveplace")
    Observable<HttpResult<LiveChannelPlaceBean>> getLivePlace(@Query("token") String token);
}
