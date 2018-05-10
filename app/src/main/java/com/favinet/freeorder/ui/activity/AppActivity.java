package com.favinet.freeorder.ui.activity;

import com.favinet.freeorder.common.activity.BaseActivity;

import butterknife.ButterKnife;

/**
 * Created by KCH on 2018-04-02.
 */

public class AppActivity extends BaseActivity {

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

}
