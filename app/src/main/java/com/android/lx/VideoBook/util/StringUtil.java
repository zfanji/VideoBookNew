package com.android.lx.VideoBook.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by flykozhang on 2016/5/6.
 */
public class StringUtil {
    private static final int HOUR = 60 * 60 * 1000;
    private static final int MIN = 60 * 1000;
    private static final int SEC = 1000;

    /*格式化duration*/
    public static String fromatDuration(int duration) {
        int hour = duration / HOUR;
        int min = duration % HOUR / MIN;
        int sec = duration % MIN / SEC;

        if(hour>0){
            /*01:01*/
            return String.format("%02d:%02d:%02d",hour,min,sec);
        }else{
            /*01:01*/
            return String.format("%02d:%02d",min,sec);
        }
    }

    /*格式化系统时间*/
    public static String fromatSystemTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss");
        return simpleDateFormat.format(new Date());
    }

    // This method replaces url to filename.
    // Url has special characters, so We have to change these characters
    // to save bitmap file into device cache directory.

    public static String keyToFilename(String key) {
        String filename = key.replace(":", "_");
        filename = filename.replace("/", "_s_");
        filename = filename.replace("\\", "_bs_");
        filename = filename.replace("&", "_bs_");
        filename = filename.replace("*", "_start_");
        filename = filename.replace("?", "_q_");
        filename = filename.replace("|", "_or_");
        filename = filename.replace(">", "_gt_");
        filename = filename.replace("<", "_lt_");
        return filename;
    }
}
