package com.android.lx.VideoBook.util;

import android.content.res.Configuration;
import android.content.res.Resources;

/**
 * Created by zhumj on 2016/5/10.
 */
public class ScreenChange {
    public static boolean isScreenLANDSCAPE(Resources resources) {

        Configuration mConfiguration = resources.getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation ; //获取屏幕方向

        if(ori == mConfiguration.ORIENTATION_LANDSCAPE){
            //横屏
            return true;
        }else if(ori == mConfiguration.ORIENTATION_PORTRAIT){
            //竖屏
            return false;
        }
        return false;
    }
}
