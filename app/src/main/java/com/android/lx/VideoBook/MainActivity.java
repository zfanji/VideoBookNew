package com.android.lx.VideoBook;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.lx.VideoBook.adapter.CacheAdapter;
import com.android.lx.VideoBook.model.IUserView;
import com.android.lx.VideoBook.model.VideoProvider;
import com.android.lx.VideoBook.util.DataCleanManager;

import java.io.File;

public class MainActivity extends AppCompatActivity implements IUserView{
    private static final String TAG = "MainActivity";
    private static VideoBookApplication singleton;

    private GridView mGridView;
    private CacheAdapter gridCacheAdapter;
    private TextView mNote;

    private boolean isScreenDirection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        singleton = VideoBookApplication.getInstance();


        mGridView = (GridView) findViewById(R.id.gridView);
        gridCacheAdapter =new CacheAdapter(this,R.layout.item);
        mNote = (TextView) findViewById(R.id.note);

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
        gridCacheAdapter.itemWidth = getMinValue(width1, height1) * 45 / 100;
        gridCacheAdapter.itemHeight = gridCacheAdapter.itemWidth * 240 / 360;
        Log.d(TAG,"itemWidth="+gridCacheAdapter.itemWidth+" gridCacheAdapter="+gridCacheAdapter.itemHeight);

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
                String path=singleton.urls.get(position).getPath();
                Log.d(TAG,"文件路径:"+path);
                Uri uri = Uri.parse(path);
                //调用系统自带的播放器
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "video/mp4");
                startActivity(intent);
            }
        });

        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG,"onItemLongClick="+position);
                showDelDialog(position);
                return false;
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


    protected void showDelDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Delete the file?");

        builder.setMessage(singleton.urls.get(position).getTitle());

        builder.setPositiveButton("Sure", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String path=singleton.urls.get(position).getPath();
                gridCacheAdapter.removeItem(singleton.urls.get(position));
                File delfile = new File(path);
                DataCleanManager.deleteDirectoryOrFile(delfile);
                Uri data = Uri.parse("file://" +path);
                sendBroadcast(new  Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
                Log.d(TAG,"del.....[ "+path);
                gridCacheAdapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            Log.d(TAG,"is action_about");
            this.aboutDialog();
            return true;
        }else if (id == R.id.action_refresh){
            Log.d(TAG,"is refresh");
            gridCacheAdapter.clearCache();
            gridCacheAdapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 屏幕旋转时调用此方法
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int lastIndex = mGridView.getFirstVisiblePosition();
//        Log.d(TAG,"mGridView.first()="+mGridView.getFirstVisiblePosition());
//        Log.d(TAG,"mGridView.last()="+mGridView.getLastVisiblePosition());
        if(newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
           // Toast.makeText(MainActivity.this, "现在是竖屏", Toast.LENGTH_SHORT).show();
            mGridView.setNumColumns(2);
            mGridView.setSelection(lastIndex);
            gridCacheAdapter.notifyDataSetChanged();
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

    private void aboutDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器
        builder.setTitle("About"); //设置标题
        String alert1 = "Version: "+getPackageInfo(this).versionName;
        String alert2 = "Default Video Path："+VideoProvider.ASSIGN_PATH;
        builder.setMessage(alert1 +"\n"+ alert2);
        builder.setIcon(R.drawable.ic_launcher);//设置图标，图片id即可
        //参数都设置完成了，创建并显示出来
        builder.create().show();
    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;

        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);

            return pi;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pi;
    }


    @Override
    public void noVideo(boolean isNo) {
        if(isNo)
            this.mNote.setVisibility(View.VISIBLE);
        else
            this.mNote.setVisibility(View.INVISIBLE);
    }
}