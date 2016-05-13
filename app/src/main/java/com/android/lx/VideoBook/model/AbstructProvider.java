package com.android.lx.VideoBook.model;


import com.android.lx.VideoBook.persion.VideoData;

import java.util.LinkedList;

/**
 * Created by admin on 2016/5/9.
 */
public interface AbstructProvider {
    public LinkedList<VideoData> getList();
}
