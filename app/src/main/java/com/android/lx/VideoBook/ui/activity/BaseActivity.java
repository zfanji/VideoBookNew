package com.android.lx.VideoBook.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.android.lx.VideoBook.R;
import com.android.lx.VideoBook.ui.UiInterface;


/**
 * Created by flykozhang on 2016/5/6.
 */
public abstract class BaseActivity extends AppCompatActivity implements UiInterface {
    public final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initView();
        initListener();
        initData();
        regCommonBtn();
    }

    /*处理公共的控件初始化和监听*/
    protected void regCommonBtn(){
//        View back = findViewById(R.id.back);
//        if(back!=null){
//            back.setOnClickListener(this);
//        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
//            case R.id.back:
//                finish();
//                break;
            default:
                /*处理子类自己的点击事件*/
                pressClick(v);
                break;
        }
    }


}
