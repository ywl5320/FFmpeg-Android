package com.ywl5320.wlivefm.http.beans;

import java.util.List;

/**
 * Created by ywl5320 on 2017/7/21.
 */

public class HomePageBean extends BaseBean{

    private List<ScrollImgBean> scrollImg;

    public List<ScrollImgBean> getScrollImg() {
        return scrollImg;
    }

    public void setScrollImg(List<ScrollImgBean> scrollImg) {
        this.scrollImg = scrollImg;
    }
}
