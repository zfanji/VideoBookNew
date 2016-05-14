package com.android.lx.VideoBook;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.UserDictionary;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;

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
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.lx.VideoBook.adapter.CacheAdapter;
import com.android.lx.VideoBook.model.IUserView;
import com.android.lx.VideoBook.model.VideoProvider;
import com.android.lx.VideoBook.persion.VideoData;
import com.android.lx.VideoBook.util.DataCleanManager;

import java.io.File;

public class MainActivity extends AppCompatActivity implements IUserView,SearchView.OnQueryTextListener,SearchView.OnSuggestionListener {
    private static final String TAG = "MainActivity";
    private static VideoBookApplication singleton;

    private GridView mGridView;
    private CacheAdapter gridCacheAdapter;
    private SimpleCursorAdapter simpleCursorAdapter;
    private TextView mNote;
    private SearchView searchView;
    private boolean isScreenDirection;

    private Cursor cursor;
    /**
     * Base container format for suggested words
     */

    String[] COLUMNS = new String[] {"_id","titlexxxxxxxxxxxxxxxxxx"};
    /**
     * List item format for suggested words
     */
    private final int[] COLUMNS_LIST_ITEM =
            new int[]{android.R.id.text1,android.R.id.button2};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        singleton = VideoBookApplication.getInstance();


        mGridView = (GridView) findViewById(R.id.gridView);
        gridCacheAdapter = new CacheAdapter(this, R.layout.item);

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
        Log.d(TAG, "itemWidth=" + gridCacheAdapter.itemWidth + " gridCacheAdapter=" + gridCacheAdapter.itemHeight);

        mGridView.setAdapter(gridCacheAdapter);
        gridCacheAdapter.notifyDataSetChanged();
    }

    private int getMinValue(int value1, int value2) {
        return value1 > value2 ? value2 : value1;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
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
        searchView.setOnSuggestionListener(this);
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

    private void setLister() {

        mGridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Log.d(TAG, "" + position);
                String path = singleton.urls.get(position).getPath();
                Log.d(TAG, "文件路径:" + path);
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
                Log.d(TAG, "onItemLongClick=" + position);
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
                String path = singleton.urls.get(position).getPath();
                gridCacheAdapter.removeItem(singleton.urls.get(position));
                File delfile = new File(path);
                DataCleanManager.deleteDirectoryOrFile(delfile);
                Uri data = Uri.parse("file://" + path);
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
                Log.d(TAG, "del.....[ " + path);
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
            Log.d(TAG, "is action_about");
            this.aboutDialog();
            return true;
        } else if (id == R.id.action_refresh) {
            Log.d(TAG, "is refresh");
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
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Toast.makeText(MainActivity.this, "现在是竖屏", Toast.LENGTH_SHORT).show();
            mGridView.setNumColumns(2);
            mGridView.setSelection(lastIndex);
            gridCacheAdapter.notifyDataSetChanged();
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
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
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
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

    private void aboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);  //先得到构造器
        builder.setTitle("About"); //设置标题
        String alert1 = "Version: " + getPackageInfo(this).versionName;
        String alert2 = "Default Video Path：" + VideoProvider.ASSIGN_PATH;
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


    @Override
    public boolean onSuggestionSelect(int position) {
        return false;
    }

    @Override
    public boolean onSuggestionClick(int position) {
        /**
         *
         * Similar to the {@link #onQueryTextSubmit} below, this method handles suggestion clicks,
         * via the search field drop down.
         *
         * Handle new intents or other actions with the user's suggested queries in here,
         * when a suggestion is clicked.
         *
         */
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d(TAG,"onQueryTextSubmit!!!!!!"+query);
        mGridView.setFilterText(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if (!query.isEmpty()) {
            Log.d(TAG,"onQueryTextChange!!!!!!"+query);

        } else {
            return false;
        }
        return true;
    }


    /**
     * Checks if the word exists in the dictionary database before adding it.
     *
     * @param query he current word being searched by the user.
     */
    private boolean wordExists(String query) {

        ContentResolver resolver = getContentResolver();
        String[] columns = new String[]{MediaStore.Video.Media.BUCKET_ID,  MediaStore.Video.Media.DATA};

        cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                columns, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            Log.w(TAG, "Word: '" + query + "' already exists and will not be re-inserted.");
            return true;
        }

        return false;
    }


    /**
     * Performs and displays search suggestions once the first few letters, entered by the user,
     * matches existing words already in the database.
     *
     * @param query the current word being searched by the user.
     */
    private void performSuggestions(String query) {

        Object[] temp = new Object[] {0,"defaultxxxxxxxxxxxxxxxxxx" };

        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        String addressText;
        VideoData data;
        int index=0;
        for(int i = 0; i < singleton.urls.size(); i++) {
            data = singleton.urls.get(i);
            if(data.getTitle().contains(query)){
                temp[0] = index;
                temp[1] = data.getTitle();
                cursor.addRow(temp);
                index++;
            }
        }

        if (cursor != null) {
            simpleCursorAdapter = new SimpleCursorAdapter(this.getBaseContext(),
                    android.R.layout.simple_list_item_1, cursor, COLUMNS, COLUMNS_LIST_ITEM);
        } else {
            cursor.close();
            Log.w(TAG, "Cursor came back with nothing. Cursor has been closed.");
        }

        searchView.setSuggestionsAdapter(simpleCursorAdapter);
    }

}