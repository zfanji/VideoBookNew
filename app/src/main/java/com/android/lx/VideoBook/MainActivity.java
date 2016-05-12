package com.android.lx.VideoBook;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.android.lx.VideoBook.adapter.CacheAdapter;
import com.android.lx.VideoBook.model.VideoProvider;
import com.android.lx.VideoBook.persion.VideoData;
import com.android.lx.VideoBook.util.CacheContainer;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static VideoBookApplication singleton;

    private GridView mGridView;
    private CacheAdapter gridCacheAdapter;

    private ArrayList<VideoData> videosList;
    private VideoProvider provider;
    private CacheContainer cacheContainer;

    private boolean isScreenDirection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        singleton = VideoBookApplication.getInstance();
        cacheContainer = new CacheContainer(this);

        mGridView = (GridView) findViewById(R.id.gridView);
        gridCacheAdapter =new CacheAdapter(this,R.layout.item);
        provider = new VideoProvider(this);
        videosList = new ArrayList<VideoData>();

        setLister();
        initData();
    }


    private synchronized void initData() {
        //刷新配置
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mGridView.setNumColumns(4);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mGridView.setNumColumns(2);
        }

        WindowManager wm1 = this.getWindowManager();
        int width1 = wm1.getDefaultDisplay().getWidth();
        int height1 = wm1.getDefaultDisplay().getHeight();
        gridCacheAdapter.itemWidth = getMinValue(width1, height1) * 50 / 100;
        gridCacheAdapter.itemHeight = gridCacheAdapter.itemWidth * 240 / 360;
        Log.d(TAG,"itemWidth="+gridCacheAdapter.itemWidth+" gridCacheAdapter="+gridCacheAdapter.itemHeight);

        videosList.clear();
        videosList = provider.getList();

        gridCacheAdapter.clearCache();
        MainActivity.singleton.urls.clear();

        for(VideoData each : videosList){
            gridCacheAdapter.addItem(each.getPath(),each.getTitle(),each.getSize(),each.getDuration());
            MainActivity.singleton.urls.add(each);
        };

        mGridView.setAdapter(gridCacheAdapter);
        gridCacheAdapter.notifyDataSetChanged();
    }
    private int getMinValue(int value1,int value2){
        return value1>value2 ? value2:value1;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    private void setLister(){

        mGridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Log.d(TAG, "" + position);
                String path=videosList.get(position).getPath();
                Log.d(TAG,"文件路径:"+path);
                Uri uri = Uri.parse(path);
                //调用系统自带的播放器
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "video/mp4");
                startActivity(intent);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Log.d(TAG,"is Settings");
            return true;
        }else if (id == R.id.action_about) {
            Log.d(TAG,"is About");
            return true;
        }else if (id == R.id.action_refresh){
            Log.d(TAG,"is refresh");
            refreshData();
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshData() {
    }

    /**
     * 屏幕旋转时调用此方法
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int lastIndex = mGridView.getFirstVisiblePosition();
        Log.d(TAG,"mGridView.first()="+mGridView.getFirstVisiblePosition());
        Log.d(TAG,"mGridView.last()="+mGridView.getLastVisiblePosition());
        if(newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
           // Toast.makeText(MainActivity.this, "现在是竖屏", Toast.LENGTH_SHORT).show();
            mGridView.setNumColumns(2);
            mGridView.setSelection(lastIndex);
            gridCacheAdapter.notifyDataSetChanged();
           // mGridView.setSelection();
        }
        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
         // Toast.makeText(MainActivity.this, "现在是横屏", Toast.LENGTH_SHORT).show();
            mGridView.setNumColumns(4);
            mGridView.setSelection(lastIndex);
            gridCacheAdapter.notifyDataSetChanged();
        }

    }

    //返回键
    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis()-exitTime) > 2000){
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}