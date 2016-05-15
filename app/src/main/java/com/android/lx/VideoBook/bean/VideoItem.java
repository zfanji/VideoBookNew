package com.android.lx.VideoBook.bean;

import android.database.Cursor;
import android.provider.MediaStore.Video.Media;

import java.io.Serializable;

/**
 * Created by flykozhang on 2016/5/6.
 */
public class VideoItem implements Serializable {
    private String path,title;
    private int duration,size;

    public static VideoItem instanceFromCursor(Cursor cursor){
        VideoItem item = new VideoItem();
        if (cursor == null || cursor.getCount()==0) {
            return item;
        }else {
            item.path = cursor.getString(cursor.getColumnIndex(Media.DATA));
            item.title = cursor.getString(cursor.getColumnIndex(Media.TITLE));
            item.duration = cursor.getInt(cursor.getColumnIndex(Media.DURATION));
            item.size = cursor.getInt(cursor.getColumnIndex(Media.SIZE));
            return item;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "VideoItem{" +
                "path='" + path + '\'' +
                ", title='" + title + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                '}';
    }
}
