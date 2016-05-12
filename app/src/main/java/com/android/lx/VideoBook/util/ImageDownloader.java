package com.android.lx.VideoBook.util;

import android.widget.ImageView;

import com.android.lx.VideoBook.VideoBookApplication;

public class ImageDownloader {
	private VideoBookApplication singleton;
	
	public ImageDownloader() {
		this.singleton = VideoBookApplication.getInstance();
	}
	
	public void download(ImageView imageView, int position,int width,int height) {
		
		//
		// If task already exists, remove the task in the imageTaskCache.
		// After removing, create imageAsyncTask object and add the imageTaskCache.
		// GridView reuses it's rows. So we asynchronously use each row.
		// This is why the imageView is key and the task is value.
		//
		
		ImageAsyncTask task = singleton.imageTaskCache.get(imageView);
		if(task != null) {			
				task.cancel(true);
				singleton.imageTaskCache.remove(imageView);
				imageView.setImageBitmap(null);
				task = null;
		}
		
		task = new ImageAsyncTask();
		task.download(imageView, position,width,height);
		singleton.imageTaskCache.put(imageView, task);			
		
	}
}
