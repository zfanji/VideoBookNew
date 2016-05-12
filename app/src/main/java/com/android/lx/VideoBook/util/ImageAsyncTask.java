package com.android.lx.VideoBook.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.android.lx.VideoBook.VideoBookApplication;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

// This class actually downloads bitmaps.

public class ImageAsyncTask {
	private static final String TAG = "ImageAsyncTask";
	private VideoBookApplication singleton;
	private BitmapDownloaderTask task;
	private String url, fileName;
	
	public ImageAsyncTask() {
		this.singleton = VideoBookApplication.getInstance();
	}
	
	public void download(ImageView imageView, int position,int width,int height) {
		
		// taskCache saves task related url.
		// If task is not null, remove the task and create new task. 
		// After then start downloading bitmap image.
		
		url = singleton.urls.get(position).getPath();
		task = singleton.taskCache.get(url);
		fileName = singleton.keyToFilename(url);
	//	Log.d(TAG,"文件名: "+fileName);
		if(task != null) {
			task.cancel(true);
			singleton.taskCache.remove(url);				
		}
		
		this.task = new BitmapDownloaderTask(imageView,width,height);
		task.execute(url);
		singleton.taskCache.put(url, task);										
	}
	
	public void cancel(boolean cancel) {	
		task.cancel(cancel);
		singleton.taskCache.remove(url);
	}

	public boolean isCancelled() {
		if(task != null) {
			return task.isCancelled();
		}
		
		return true;
	}
	
  public class BitmapDownloaderTask extends AsyncTask<String, Integer, Bitmap> {
    private String url;
	private int mWidth = 360;
	private int mHeight = 240;
    private final WeakReference<ImageView> imageViewReference;

    public BitmapDownloaderTask(ImageView imageView,int width,int height) {
		mWidth = width;
		mHeight = height;
        imageViewReference = new WeakReference<ImageView>(imageView);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        url = (String)params[0];
        // Before downloading a bitmap, find a bitmap using url in the memory Cache.
        // If you can't find int the memory Cache, find it in the disk Cache.
        // If you can't find in the disk Cache, then you really download.
        
      	if(CacheContainer.getMemory(url) != null) {
      		return (Bitmap)CacheContainer.getMemory(url);
      	}
      	
      	if(CacheContainer.getDisk(fileName) != null) {
      		return (Bitmap)CacheContainer.getDisk(fileName);
      	}
      	
        return getBitmap(url,mWidth,mHeight);
    }

		@Override
    protected void onProgressUpdate(Integer... progress) {
    }

		@Override
    protected void onPostExecute(Bitmap bitmap) {
			if(bitmap != null) {
				ImageView imageView = imageViewReference.get();
				imageView.setImageBitmap(bitmap);						
				
				// Save the bitmap into memory and disk cache.
				
				synchronized (CacheContainer.getMemoryCache()) {
			     if (CacheContainer.getMemory(url) == null) {
			    //	 Log.d(TAG, "Here is Memory Put");
			    	 CacheContainer.putMemory(url, bitmap);			     
			     }			     
				}
				
				synchronized (CacheContainer.getDiskCache()) {
			     if (CacheContainer.getDisk(fileName) == null) {
			    //	 Log.d(TAG, "Here is Disk Put");
			    	 CacheContainer.putDisk(url, singleton.directory + "/" + fileName);			     
			     }			     
				}
				
			}
    }
  }

  public Bitmap getBitmap(String url,int width,int height) {

	//Log.w(TAG,"getBitmap~~~~~~"+url);
    try {
		System.setProperty("http.keepAlive", "false");
//        URL aURL = new URL(url);
//        URLConnection conn = aURL.openConnection();
//        conn.connect();
//        InputStream is = conn.getInputStream();

		Bitmap bitmap = getVideoThumbnail(url, width,height, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
		InputStream is = Bitmap2IS(bitmap);
        BufferedInputStream bis = new BufferedInputStream(is);

        File file = new File(singleton.directory + "/" + fileName);
        
      	OutputStream out = null;
    		try {
    			out = new FileOutputStream(file);
    	    try {
    	        byte[] buffer = new byte[1024 * 1024];
    	        int bytesRead = 0;
    	        while ((bytesRead = bis.read(buffer, 0, buffer.length)) >= 0) {
    	            out.write(buffer, 0, bytesRead);
    	        }
    	    } finally {
    	        out.close();
    	    }	
    		}
    		catch (FileNotFoundException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} 
    		
        bis.close();
        is.close();      		

    } catch (Exception e) {
        e.printStackTrace();
    }
    
    return showImage(singleton.directory + "/" + fileName);
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
	//	Log.d(TAG,"width="+width+" height="+height);
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	private InputStream Bitmap2IS(Bitmap bm){
		//Log.d(TAG,"转前bm="+bm.getHeight());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		InputStream sbs = new ByteArrayInputStream(baos.toByteArray());
		return sbs;
	}
  public static Bitmap showImage(String dir) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inInputShareable = true;
    options.inDither=false;
	options.inPreferredConfig = Bitmap.Config.RGB_565;
	options.inTempStorage=new byte[32 * 1024];
    options.inPurgeable = true;
    options.inJustDecodeBounds = false;
    options.inSampleSize = 1;//不要缩放
    
    File file = new File(dir);
    FileInputStream fs=null;
    try {
        fs = new FileInputStream(file);
    } catch (FileNotFoundException e) {
        //TODO do something intelligent
        e.printStackTrace();
    }
    
    Bitmap bm = null;

    try {
        if(fs!=null) bm= BitmapFactory.decodeFileDescriptor(fs.getFD(), null, options);
    } catch (IOException e) {
        //TODO do something intelligent
        e.printStackTrace();
    } finally{ 
        if(fs!=null) {
            try {
                fs.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
	 // Log.d(TAG,"转后bm="+bm.getHeight());
    return bm;
  }    
    
}
