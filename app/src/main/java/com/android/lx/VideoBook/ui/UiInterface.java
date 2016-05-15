package com.android.lx.VideoBook.ui;

import android.view.View;

/**
 * Created by flykozhang on 2016/5/6.
 */
public interface UiInterface extends View.OnClickListener {

    void initData();

    void initListener();

    void initView();

    int getLayoutId();

    void pressClick(View v);



    public void noVideo(boolean isNo);
}
