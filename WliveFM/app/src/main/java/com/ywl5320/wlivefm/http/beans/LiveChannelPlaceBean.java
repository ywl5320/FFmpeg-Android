package com.ywl5320.wlivefm.http.beans;

import java.util.List;

/**
 * Created by ywl5320 on 2017/7/27.
 */

public class LiveChannelPlaceBean extends BaseBean{

    private List<PlaceBean> liveChannelPlace;

    public List<PlaceBean> getLiveChannelPlace() {
        return liveChannelPlace;
    }

    public void setLiveChannelPlace(List<PlaceBean> liveChannelPlace) {
        this.liveChannelPlace = liveChannelPlace;
    }
}
