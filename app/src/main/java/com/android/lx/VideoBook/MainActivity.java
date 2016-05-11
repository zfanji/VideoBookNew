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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private GridView mGridView;
    private CacheAdapter gridCacheAdapter;

    private ArrayList<VideoData> videosList;
    private VideoProvider provider;



    private boolean isScreenDirection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        WindowManager wm1 = this.getWindowManager();
        int width1 = wm1.getDefaultDisplay().getWidth();
        int height1 = wm1.getDefaultDisplay().getHeight();
        Log.w(TAG,"width1="+width1+"  height1="+height1);

        mGridView = (GridView) findViewById(R.id.gridView);
        gridCacheAdapter =new CacheAdapter(this,R.layout.item);
        gridCacheAdapter.itemWidth = width1*50/100;

        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.i("info", "横屏");
            gridCacheAdapter.isLandscape = true;
            mGridView.setNumColumns(4);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.i("info", "竖屏");
            gridCacheAdapter.isLandscape = false;
            mGridView.setNumColumns(2);
        }

        provider = new VideoProvider(this);
        videosList = provider.getList();
        for(VideoData each : videosList){
            gridCacheAdapter.addItem(each.getPath(),each.getTitle(),each.getSize(),each.getDuration());
        };

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

        mGridView.setAdapter(gridCacheAdapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 屏幕旋转时调用此方法
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //newConfig.orientation获得当前屏幕状态是横向或者竖向
        //Configuration.ORIENTATION_PORTRAIT 表示竖向
        //Configuration.ORIENTATION_LANDSCAPE 表示横屏
        if(newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(MainActivity.this, "现在是竖屏", Toast.LENGTH_SHORT).show();
            gridCacheAdapter.isLandscape=true;
            mGridView.setNumColumns(2);
            gridCacheAdapter.notifyDataSetChanged();
        }
        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
            Toast.makeText(MainActivity.this, "现在是横屏", Toast.LENGTH_SHORT).show();
            gridCacheAdapter.isLandscape=false;
            mGridView.setNumColumns(4);
            gridCacheAdapter.notifyDataSetChanged();
        }
    }

}