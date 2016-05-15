package com.android.lx.VideoBook;

import android.app.Application;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.android.lx.VideoBook.adapter.VideoCursorAdapter;
import com.android.lx.VideoBook.util.ImageAsyncTask;

import java.io.File;
import java.util.LinkedHashMap;

public class VideoBookApplication extends Application {
	private static VideoBookApplication singleton;
	protected DisplayMetrics metrics;
	protected int widthSize;		
	protected int widthPixel;
	protected float pixelRate;
	public String directory;

	public LinkedHashMap<String, ImageAsyncTask.BitmapDownloaderTask> taskCache;
	public LinkedHashMap<ImageView, ImageAsyncTask> imageTaskCache;//加载图片任务的请求列表
		
	//
	// This class is to use singleton pattern.
	//
	
	public static VideoBookApplication getInstance() {
    return singleton;
	}	
	
	@Override
	public void onCreate() {
		super.onCreate();
		singleton = this;
		singleton.initializeInstance();
	}
	
	protected void initializeInstance() {

		directory = getFilesDir().getAbsolutePath();
		taskCache = new LinkedHashMap<String, ImageAsyncTask.BitmapDownloaderTask>();
		imageTaskCache = new LinkedHashMap<ImageView, ImageAsyncTask>();

		File file = new File(directory);
		if(!file.exists()) {
			file.mkdir();
		}
	}

}
