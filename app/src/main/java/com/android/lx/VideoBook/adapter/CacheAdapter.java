package com.android.lx.VideoBook.adapter;

/**
 * Created by Micheal on 2016/5/11.
 */

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.lx.VideoBook.R;
import com.android.lx.VideoBook.VideoBookApplication;
import com.android.lx.VideoBook.model.IUserView;
import com.android.lx.VideoBook.model.VideoProvider;
import com.android.lx.VideoBook.persion.VideoData;
import com.android.lx.VideoBook.util.CacheContainer;
import com.android.lx.VideoBook.util.ImageDownloader;

import java.util.LinkedList;

/**
 * 使用列表缓存过去的Item
 * @author hellogv
 *
 */
public class CacheAdapter extends BaseAdapter {
    private static final String TAG = "CacheAdapter";
    private IUserView mUserView;
    private VideoProvider provider;
    private CacheContainer cacheContainer;

    private static VideoBookApplication singleton;
    private ImageDownloader downloader;

    private Context mContext;
    private LayoutInflater inflater;
    private int layoutResourceId;
    private LinkedList<VideoData> videosListNew;
    private LinkedList<VideoData> videosListOld;

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
        this.mUserView = (IUserView) c;
        this.layoutResourceId = layoutResourceId;
        this.downloader = new ImageDownloader();
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.singleton = VideoBookApplication.getInstance();
        this.provider = new VideoProvider(mContext);
        this.cacheContainer = new CacheContainer(mContext);
        this.singleton.urls = provider.getList();

        this.videosListNew = (LinkedList<VideoData>) this.singleton.urls.clone();
        this.videosListOld =  (LinkedList<VideoData>) this.singleton.urls.clone();

    }
    public void clearCache() {
        cacheContainer.clear();
    }
    @Override
    public void notifyDataSetChanged() {
        compareList();
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if(singleton.urls.size()>0)
            mUserView.noVideo(false);
        else
            mUserView.noVideo(true);

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
       // Log.d(TAG,"getView="+position);
        convertView = this.singleton.urls.get(position).getSaveView();
        if(convertView==null){
        //    Log.d(TAG,"convertView is null----> "+position);
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

            holder.textTitle.setText(singleton.urls.get(position).getDisplayName());

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


    private void compareList(){
        videosListNew.clear();
        videosListNew = provider.getList();

        if(videosListOld.isEmpty() && videosListNew.isEmpty()){
            Log.d(TAG,"都没有！！");
            this.removeAllItem();
        }else if(videosListOld.isEmpty() && videosListNew.size()>0){
            //如果之前无任何连接设备，新列表全部添加
            Log.d(TAG,"全部添加");
            for(VideoData each:videosListNew){
                this.addItem(each);
            }
        }else if(videosListNew.isEmpty() && videosListOld.size()>0){
            //如果新列表为空，老列表设备全部删除
            Log.d(TAG,"全部删除");
            this.removeAllItem();
        }else if(videosListNew.size()>0 && videosListOld.size()>0 ){
            Log.d(TAG,"都不为空");
            //添加新设备
            for(int i=0;i<videosListNew.size();i++){
                boolean addflag=true;

                for(int j=0;j<videosListOld.size();j++){
                    if(videosListNew.get(i).getPath().equals(videosListOld.get(j).getPath())){
                        addflag=false;
                        break;
                    }
                }
                if(addflag){
                    Log.d(TAG,"添加"+videosListNew.get(i).getPath());
                    this.addItem(videosListNew.get(i));
                }
            }

            //删除设备
            for(int i=0;i<videosListOld.size();i++){
                boolean delflag=true;
                for(int j=0;j<videosListNew.size();j++){
                    if(videosListOld.get(i).getPath().equals(videosListNew.get(j).getPath())){
                        delflag=false;
                        break;
                    }
                }
                if(delflag){
                    Log.d(TAG,"删除"+videosListOld.get(i).getPath());
                    this.removeItem(videosListOld.get(i));
                }
            }
        }
        videosListOld = (LinkedList<VideoData>) this.singleton.urls.clone();
        if(this.singleton.urls.isEmpty()){
            Log.d(TAG,"清空了");
            videosListOld.clear();
        }
    }

    public void removeAllItem(){
        this.singleton.urls.clear();
    }
}