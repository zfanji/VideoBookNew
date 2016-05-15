package com.android.lx.VideoBook.ui.activity;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.lx.VideoBook.R;
import com.android.lx.VideoBook.adapter.VideoCursorAdapter;
import com.android.lx.VideoBook.bean.VideoItem;
import com.android.lx.VideoBook.model.VideoAsyncQueryHandler;
import com.android.lx.VideoBook.util.DataCleanManager;
import com.android.lx.VideoBook.util.StringUtil;

import java.io.File;

/**
 * Created by flykozhang on 2016/5/6.
 */
public class VideoPlayActivity extends BaseActivity implements AdapterView.OnItemClickListener,SearchView.OnQueryTextListener{
    public static final String ASSIGN_PATH = "/storage/emulated/0/VideoBook";
    public static final String SELECTION_PATH = MediaStore.Video.Media.DATA + " LIKE '%" + ASSIGN_PATH + "%'";

    private GridView mGridView;
    private VideoCursorAdapter mAdapter;
    private VideoItem mItem;
    private SearchView searchView;
    private TextView mNote;
    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchViewItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchViewItem.getActionView();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);

        MenuItemCompat.setOnActionExpandListener(searchViewItem,
                new MenuItemCompat.OnActionExpandListener() {

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        searchView.clearFocus();
                        return true;
                    }
                });

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            Log.d(TAG, "is action_about");
            this.aboutDialog();
            return true;
        } else if (id == R.id.action_refresh) {
            Log.d(TAG, "is refresh");
            mAdapter.clearCache();
            mAdapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void initData() {
        mItem = (VideoItem) getIntent().getSerializableExtra("item");
        //刷新配置
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mGridView.setNumColumns(4);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mGridView.setNumColumns(2);
        }

        ContentResolver contentResolver = this.getContentResolver();
        VideoAsyncQueryHandler asyncQueryHandler = new VideoAsyncQueryHandler(contentResolver);
        asyncQueryHandler.startQuery(0, mAdapter, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION, MediaStore.Video.Media.SIZE, MediaStore.Video.Media.TITLE}, SELECTION_PATH, null, null);

        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemLongClick=" + position);
                showDelDialog(position);
                return false;
            }

        });
    }

    @Override
    public void initListener() {
        mGridView.setOnItemClickListener(this);
        int[] widthHeigh=getWidthHeight();
        mAdapter = new VideoCursorAdapter(this, null,widthHeigh[0],widthHeigh[1]);
        mGridView.setTextFilterEnabled(true);
        mGridView.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mGridView = (GridView) findViewById(R.id.gridView);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);
        mNote = (TextView) findViewById(R.id.note);
    }


    @Override
    public void pressClick(View v) {
        Log.d(TAG,"pressClick~~~~");
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d(TAG,"onQueryTextSubmit!!!!!!"+query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if (!query.isEmpty()) {
            mGridView.setFilterText(query);
            Log.d(TAG,"onQueryTextChange!!!!!!"+query);

        } else {
            mGridView.clearTextFilter();
            return false;
        }
        return true;
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG,"position="+position);
        Cursor cursor = (Cursor) mAdapter.getItem(position);
        String path = cursor
                .getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
        Uri uri = Uri.parse(path);
        //调用系统自带的播放器
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "video/mp4");
        startActivity(intent);
    }

    //返回键
    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            mGridView.clearTextFilter();
            if ((System.currentTimeMillis() - exitTime) > 2000) {
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

    private int[] getWidthHeight(){
        WindowManager wm1 = this.getWindowManager();
        int width1 = wm1.getDefaultDisplay().getWidth();
        int height1 = wm1.getDefaultDisplay().getHeight();
        width1 = getMinValue(width1, height1) * 45 / 100;
        return new int[]{width1, width1 * 240 / 360};
    }
    private int getMinValue(int value1, int value2) {
        return value1 > value2 ? value2 : value1;
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
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Toast.makeText(MainActivity.this, "现在是竖屏", Toast.LENGTH_SHORT).show();
            mGridView.setNumColumns(2);
            mGridView.setSelection(lastIndex);
            mAdapter.notifyDataSetChanged();
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Toast.makeText(MainActivity.this, "现在是横屏", Toast.LENGTH_SHORT).show();
            mGridView.setNumColumns(4);
            mGridView.setSelection(lastIndex);
            mAdapter.notifyDataSetChanged();
        }

    }

    protected void showDelDialog(int position) {
        Cursor cursor = (Cursor) mAdapter.getItem(position);
        final String path = cursor
                .getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
        final String name = cursor
                .getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));

        AlertDialog.Builder builder = new AlertDialog.Builder(VideoPlayActivity.this);
        builder.setTitle("Delete the file?");

        builder.setMessage(name);

        builder.setPositiveButton("Sure", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
               // String path = singleton.urls.get(position).getPath();
              //  gridCacheAdapter.removeItem(singleton.urls.get(position));
                File delfile = new File(path);
                DataCleanManager.deleteDirectoryOrFile(delfile);
                Uri data = Uri.parse("file://" + path);
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
                Log.d(TAG, "del.....[ " + path);
               // gridCacheAdapter.notifyDataSetChanged();
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

    private void aboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);  //先得到构造器
        builder.setTitle("About"); //设置标题
        String alert1 = "Version: " + getPackageInfo(this).versionName;
        String alert2 = "Default Video Path：" + this.ASSIGN_PATH;
        builder.setMessage(alert1 + "\n" + alert2);
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
        if (isNo)
            this.mNote.setVisibility(View.VISIBLE);
        else
            this.mNote.setVisibility(View.INVISIBLE);
    }
}
