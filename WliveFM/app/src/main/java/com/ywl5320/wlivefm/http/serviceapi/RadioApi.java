package com.ywl5320.wlivefm.http.serviceapi;


import com.ywl5320.wlivefm.http.beans.HomePageBean;
import com.ywl5320.wlivefm.http.beans.LiveChannelBean;
import com.ywl5320.wlivefm.http.beans.LiveChannelPlaceBean;
import com.ywl5320.wlivefm.http.beans.LiveListBean;
import com.ywl5320.wlivefm.http.beans.LivePageBean;
import com.ywl5320.wlivefm.http.beans.TokenBean;
import com.ywl5320.wlivefm.http.service.BaseApi;
import com.ywl5320.wlivefm.http.service.HttpMethod;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;

/**
 * Created by ywl on 2016/5/19.
 */
public class RadioApi extends BaseApi {

    public static RadioApi radioApi;
    public RadioService radioService;
    public RadioApi()
    {
        radioService = HttpMethod.getInstance().createApi(RadioService.class);
    }

    public static RadioApi getInstance()
    {
        if(radioApi == null)
        {
            radioApi = new RadioApi();
        }
        return radioApi;
    }
    /*-------------------------------------获取的方法-------------------------------------*/

    public void getToken(Observer<TokenBean> subscriber)
    {
        Observable observable = radioService.getToken()
                .map(new HttpResultFunc<TokenBean>());

        toSubscribe(observable, subscriber);
    }

    public void getHomePage(String token, Observer<HomePageBean> subscriber)
    {
        Observable observable = radioService.getHomePage(token)
                .map(new HttpResultFunc<HomePageBean>());

        toSubscribe(observable, subscriber);
    }

    public void getLivePage(String token, Observer<LivePageBean> subscriber)
    {
        Observable observable = radioService.getLivePage(token)
                .map(new HttpResultFunc<LivePageBean>());

        toSubscribe(observable, subscriber);
    }

    public void getLiveByParam(String token, String channelPlaceId, int limit, int offset, Observer<LiveListBean> subscriber)
    {
        Observable observable = radioService.getLiveByParam(token, channelPlaceId, limit, offset)
                .map(new HttpResultFunc<LiveListBean>());

        toSubscribe(observable, subscriber);
    }

    public void getLivePlace(String token, Observer<LiveChannelPlaceBean> subscriber)
    {
        Observable observable = radioService.getLivePlace(token)
                .map(new HttpResultFunc<LiveChannelPlaceBean>());

        toSubscribe(observable, subscriber);
    }

}
