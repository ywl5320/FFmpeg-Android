package com.ywl5320.wlivefm.http.beans;

import java.util.List;

/**
 * Created by ywl on 2017-7-26.
 */

public class LiveListBean extends BaseBean{

    private List<LiveChannelBean> liveChannel;


    public List<LiveChannelBean> getLiveChannel() {
        return liveChannel;
    }

    public void setLiveChannel(List<LiveChannelBean> liveChannel) {
        this.liveChannel = liveChannel;
    }
}
