package com.android.lx.VideoBook.model;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.android.lx.VideoBook.persion.VideoData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2016/5/9.
 */
public class VideoProvider implements AbstructProvider {
    private static final String TAG = "VideoProvider";
    private Context context;
    private static final String ASSIGN_PATH = "/storage/emulated/0";

    public VideoProvider(Context context) {
        this.context = context;
    }
    @Override
    public ArrayList<VideoData> getList() {
        List<VideoData> list = null;
        if (context != null) {
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null,
                    null, null);
            if (cursor != null) {
                list = new ArrayList<VideoData>();
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor
                            .getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    String title = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                    String album = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM));
                    String artist = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
                    String displayName = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                    String mimeType = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                    String path = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    long duration = cursor
                            .getInt(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    long size = cursor
                            .getLong(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                    VideoData video = new VideoData(id, displayName, album, artist,
                            displayName, mimeType, path, size, duration);
//                    Log.d("11111111111", "<br/>id = "+id
//                            +"<br/> , title = "+title
//                            +"<br/> , album = "+album
//                            +"<br/> , artist = "+artist
//                            +"<br/> , displayName = "+displayName
//                            +"<br/> , mimeType = "+mimeType
//                            +"<br/> , path = "+path
//                            +"<br/> , size = "+size
//                            +"<br/> , duration = "+duration);

                    if(path.contains(ASSIGN_PATH))
                    {
                        list.add(video);
                    }
              //      Log.d(TAG,"path="+path);
                }
                cursor.close();
            }
        }
        return (ArrayList<VideoData>) list;
    }
}
