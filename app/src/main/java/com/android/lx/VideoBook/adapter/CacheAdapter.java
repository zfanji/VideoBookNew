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
import com.android.lx.VideoBook.util.ImageDownloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * 使用列表缓存过去的Item
 * @author hellogv
 *
 */
public class CacheAdapter extends BaseAdapter {
    private static final String TAG = "CacheAdapter";
    private ImageDownloader downloader;

    public boolean isLandscape=false;
    private Context mContext;
    private ArrayList<Item> mItems = new ArrayList<Item>();
    LayoutInflater inflater;
    private int layoutResourceId;
    public List<Integer> lstPosition=new ArrayList<Integer>();
    public List<View> lstView=new ArrayList<View>();
    public int itemWidth = 360;
    public int itemHeight = 240;
    public boolean isRefreshEnd = true;
  //  List<Integer> lstTimes= new ArrayList<Integer>();
   // long startTime=0;

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
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(String itemImageURL,String itemTitle,long size,long time) {
        mItems.add(new Item(itemImageURL, itemTitle,size,time));
    }

    public int getCount() {
        return mItems.size();
    }

    public Item getItem(int position) {
        return mItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView textTitle;
        ImageView icon;
        TextView size;
        TextView time;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
      //  Log.d(TAG,"getView="+position);
       // startTime=System.nanoTime();
        ViewHolder holder = new ViewHolder();
        if (lstPosition.contains(position) == false) {
            if(lstPosition.size()>75)//这里设置缓存的Item数量
            {
                lstPosition.remove(0);//删除第一项
                lstView.remove(0);//删除第一项
            }
            convertView = inflater.inflate(layoutResourceId, null);
            holder.textTitle = (TextView) convertView.findViewById(R.id.text_video_title);
            holder.icon = (ImageView) convertView.findViewById(R.id.video_image);
            holder.size = (TextView) convertView.findViewById(R.id.text_video_size);
            holder.time = (TextView) convertView.findViewById(R.id.text_video_time);

            holder.size.setText("size:"+String.valueOf(mItems.get(position).size/1024/1024)+"MB");

            long minute = mItems.get(position).time /1000 / 60;
            long second = mItems.get(position).time /1000 % 60;
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

            holder.textTitle.setText(""+position+"("+mItems.get(position).title);


            holder.icon.setMaxWidth(this.itemWidth);
            holder.icon.setMaxHeight(this.itemHeight);
            Log.d(TAG,""+itemWidth+"--"+itemHeight);
            downloader.download(holder.icon, position,itemWidth,itemHeight);
//            new AsyncLoadImage().execute(new Object[] {icon,mItems.get(position).itemImageURL,this.itemWidth,this.itemHeight});

            lstPosition.add(position);//添加最新项
            lstView.add(convertView);//添加最新项
        } else
        {
            convertView = lstView.get(lstPosition.indexOf(position));
        }

        if(lstView.size()==mItems.size()){
            this.isRefreshEnd = true;
        }else {
            this.isRefreshEnd = false;
        }
//        int endTime=(int) (System.nanoTime()-startTime);
//        lstTimes.add(endTime);
//        if(lstTimes.size()==10)
//        {
//            int total=0;
//            for(int i=0;i<lstTimes.size();i++)
//                total=total+lstTimes.get(i);
//
//            Log.e("10个所花的时间：" +total/1000 +" μs",
//                    "所用内存："+Runtime.getRuntime().totalMemory()/1024 +" KB");
//            lstTimes.clear();
//        }


        return convertView;
    }

    /**
     * 异步读取网络图片
     * @author hellogv
     */
    class AsyncLoadImage extends AsyncTask<Object,Object,Void> {
        @Override
        protected Void doInBackground(Object... params) {

            ImageView imageView=(ImageView) params[0];
            String url=(String) params[1];
           // Bitmap bitmap = ReadBitmapById(mContext,R.drawable.ic_launcher);
            int width = (int) params[2];
            int height =(int) params[3];

          //  Log.d(TAG,"width="+width+"height="+height);

            Bitmap bitmap = getVideoThumbnail(url, width, height, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
            publishProgress(new Object[] {imageView, bitmap});

            return null;
        }

        protected void onProgressUpdate(Object... progress) {
            ImageView imageView = (ImageView) progress[0];
            imageView.setImageBitmap((Bitmap) progress[1]);
        }
    }


    /**
     * 以最小内存读取本地资源的图片
     * 但是会导致图片失真
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap ReadBitmapById(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // 获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        Bitmap bitMap = BitmapFactory.decodeStream(is, null, opt);
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitMap;
    }
    /**
     * 获取视频缩略图
     * @param videoPath
     * @param width
     * @param height
     * @param kind
     * @return
     */
    private Bitmap getVideoThumbnail(String videoPath, int width , int height, int kind){
        Bitmap bitmap = null;
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    static public Bitmap getBitmapByUrl(String urlString)
            throws MalformedURLException, IOException {
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(25000);
        connection.setReadTimeout(90000);
        Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream());
        return bitmap;
    }

    public void clearCache(){
        this.mItems.clear();
        this.lstView.clear();
        this.lstPosition.clear();
    }
}