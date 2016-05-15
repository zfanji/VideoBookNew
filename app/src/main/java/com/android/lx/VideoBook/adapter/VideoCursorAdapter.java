package com.android.lx.VideoBook.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.lx.VideoBook.R;
import com.android.lx.VideoBook.bean.VideoItem;
import com.android.lx.VideoBook.ui.UiInterface;
import com.android.lx.VideoBook.ui.activity.VideoPlayActivity;
import com.android.lx.VideoBook.util.CacheContainer;
import com.android.lx.VideoBook.util.ImageDownloader;
import com.android.lx.VideoBook.util.StringUtil;


/**
 * Created by flykozhang on 2016/5/6.
 */
public class VideoCursorAdapter extends CursorAdapter {
    private static final String TAG = "VideoCursorAdapter";
    private Context mContext;
    private ImageDownloader mImageDownloader;
    private CacheContainer mCacheContainer;
    private int mWidth = 360;
    private int mHeight = 240;
    private UiInterface mUserView;

    public VideoCursorAdapter(Context context, Cursor c,int width,int height) {
        super(context, c);
        this.mContext = context;
        this.mUserView = (UiInterface) context;
        this.mImageDownloader = new ImageDownloader();
        this.mCacheContainer = new CacheContainer(mContext);
        this.mWidth = width;
        this.mHeight = height;
    }
    @Override
    public Cursor swapCursor(Cursor newCursor) {
////        mPhotos.clear();
//        if(newCursor != null && newCursor.moveToFirst())
//            do{
//                mPhotos.add(getSelectedPhotoFromCursor(newCursor));
//            } while(newCursor.moveToNext());
        return super.swapCursor(newCursor);
    }

    @Override
    public int getCount() {
        if(super.getCursor() != null){
            if(super.getCursor().getCount()>0){
                mUserView.noVideo(false);
            }else{
                mUserView.noVideo(true);
            }
            return super.getCursor().getCount();
        }else{
            mUserView.noVideo(false);
            return 0;
        }
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.d(TAG,"newView +++++++="+cursor.getPosition());
        View view = View.inflate(context, R.layout.item,null);
        ViewHoder hoder = new ViewHoder(view);
        mImageDownloader.download(hoder.icon, cursor,mWidth,mHeight);
        view.setTag(hoder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.d(TAG,"bindView ~~~~~~="+cursor.getPosition());
        ViewHoder hoder = (ViewHoder) view.getTag();
        VideoItem item = VideoItem.instanceFromCursor(cursor);
        hoder.main_video_name.setText(item.getTitle());
        hoder.main_video_duration.setText(StringUtil.fromatDuration(item.getDuration()));
        hoder.main_video_size.setText(Formatter.formatFileSize(context,item.getSize()));
       // mImageDownloader.download(hoder.icon, cursor,mWidth,mHeight);
        view.setTag(hoder);
    }

    public void clearCache() {
        mCacheContainer.clear();
    }

    public class ViewHoder {
        private TextView main_video_name,main_video_duration,main_video_size;
        private ImageView icon;
        public ViewHoder(View root) {
            main_video_name = (TextView) root.findViewById(R.id.text_video_title);
            main_video_duration = (TextView) root.findViewById(R.id.text_video_time);
            main_video_size = (TextView) root.findViewById(R.id.text_video_size);
            icon = (ImageView) root.findViewById(R.id.video_image);
        }
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        Log.d(TAG,"runQueryOnBackgroundThread="+constraint);
        String selection = MediaStore.Video.Media.TITLE + " like ? AND " + MediaStore.Video.Media.DATA + " like ?";
        return mContext.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                , null, selection, new String[]{"%"+constraint.toString()+"%", "%"+VideoPlayActivity.ASSIGN_PATH+"%"}, null);
    }

    /**一定要重写该方法，否则AutoCompleteTextView选中某一项后，文本框显示的不是所要的文本*/
    @Override
    public CharSequence convertToString(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
    }

}
