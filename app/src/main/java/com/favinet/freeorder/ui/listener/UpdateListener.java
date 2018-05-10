package com.favinet.freeorder.ui.listener;


import android.support.v4.app.Fragment;

/**
 * Created by KCH on 2018-04-02.
 */

public interface UpdateListener {


    void addFragment(int menuId);

    void fragmentBackPressed();

    void addFragment(Fragment fragment, int menuId);

    void notifyDataSetChanged();

}
