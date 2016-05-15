package com.android.lx.VideoBook.model;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;

import com.android.lx.VideoBook.adapter.VideoCursorAdapter;


/**
 * Created by flykozhang on 2016/5/6.
 */
public class VideoAsyncQueryHandler extends AsyncQueryHandler {
    public VideoAsyncQueryHandler(ContentResolver cr) {
        super(cr);
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        super.onQueryComplete(token, cookie, cursor);
        VideoCursorAdapter adapter = (VideoCursorAdapter) cookie;
        
        adapter.swapCursor(cursor);
    }
}
