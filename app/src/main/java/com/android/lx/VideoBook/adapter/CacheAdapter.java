package com.android.lx.VideoBook.adapter;

/**
 * Created by Micheal on 2016/5/11.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.lx.VideoBook.R;
import com.android.lx.VideoBook.VideoBookApplication;
import com.android.lx.VideoBook.persion.VideoData;
import com.android.lx.VideoBook.util.ImageDownloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 使用列表缓存过去的Item
 * @author hellogv
 *
 */
public class CacheAdapter extends BaseAdapter {
    private static final String TAG = "CacheAdapter";

    private static VideoBookApplication singleton;
    private ImageDownloader downloader;

    private Context mContext;
    private LayoutInflater inflater;
    private int layoutResourceId;
   // public List<Integer> lstPosition=new ArrayList<Integer>();
   // public LinkedList<View> lstView=new LinkedList<View>();
    public int itemWidth = 360;
    public int itemHeight = 240;

    public class Item {
        public String itemImageURL;
        public String title;
        public long size;
        public long time;

        public Item(String itemImageURL, String itemTitle,long size,long time) {
            this.itemImageURL = itemImageURL;
            this.title = itemTitle;
            this.size = size;
            this.time = time;
        }

    }

    public CacheAdapter(Context c,int layoutResourceId) {
        this.mContext = c;
        this.layoutResourceId = layoutResourceId;
        this.downloader = new ImageDownloader();
        this.singleton = VideoBookApplication.getInstance();
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return singleton.urls.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return singleton.urls.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }


    private class ViewHolder {
        TextView textTitle;
        ImageView icon;
        TextView size;
        TextView time;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG,"getView="+position);
        convertView = this.singleton.urls.get(position).getSaveView();
        if(convertView==null){
            Log.d(TAG,"convertView is null----> "+position);
            ViewHolder holder = new ViewHolder();

            convertView = inflater.inflate(layoutResourceId, null);
            holder.textTitle = (TextView) convertView.findViewById(R.id.text_video_title);
            holder.icon = (ImageView) convertView.findViewById(R.id.video_image);
            holder.size = (TextView) convertView.findViewById(R.id.text_video_size);
            holder.time = (TextView) convertView.findViewById(R.id.text_video_time);

            holder.size.setText("size:"+String.valueOf(singleton.urls.get(position).getSize()/1024/1024)+"MB");

            long minute = singleton.urls.get(position).getDuration() /1000 / 60;
            long second = singleton.urls.get(position).getDuration() /1000 % 60;
            String str_min;
            String str_sec;
            if (minute<10){
                str_min = "0"+String.valueOf(minute);
            }else {
                str_min = String.valueOf(minute);
            }
            if (second<10){
                str_sec = "0"+String.valueOf(second);
            }else {
                str_sec = String.valueOf(second);
            }
            holder.time.setText("mins:"+str_min + ":" + str_sec);

            holder.textTitle.setText(""+position+"("+singleton.urls.get(position).getDisplayName());

            holder.icon.setMaxWidth(this.itemWidth);
            holder.icon.setMaxHeight(this.itemHeight);
            Log.d(TAG,""+itemWidth+"--"+itemHeight);
            downloader.download(holder.icon, position,itemWidth,itemHeight);

            this.singleton.urls.get(position).setSaveView(convertView);
        }

        return convertView;
    }

    public void addItem(VideoData obj){
        Log.d(TAG,"add "+obj.getPath());
        this.singleton.urls.add(obj);
    }

    public void removeItem(VideoData obj){
        Log.d(TAG,"remove "+obj.getPath());
        this.singleton.urls.remove(obj);
    }

    public void removeAllItem(){
        this.singleton.urls.clear();
    }
}