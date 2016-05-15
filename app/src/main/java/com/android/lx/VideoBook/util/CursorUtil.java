package com.android.lx.VideoBook.util;

import android.database.Cursor;
import android.util.Log;

/**
 * Created by flykozhang on 2016/5/6.
 */
public class CursorUtil {
    private static final String TAG = "CursorUtil";

    /*打印cursor*/
    public static void pinotCursor(Cursor cursor) {
        Log.e(TAG, "pinotCursor: 查询到的数据条数为 == " + cursor.getCount());
        while (cursor.moveToNext()) {
            Log.e(TAG, "pinotCursor: =========================");
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                Log.e(TAG, "pinotCursor: name == " + cursor.getColumnName(i) + ": value ==" + cursor.getString(i));
            }
        }
    }
}
