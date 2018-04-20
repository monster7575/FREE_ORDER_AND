package com.example.sampleandroid.common.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.Toast;

import com.example.sampleandroid.R;

import com.example.sampleandroid.common.dialog.ProgressDialog;
import com.example.sampleandroid.data.config.Constants;
import com.example.sampleandroid.data.parcel.IntentData;
import com.example.sampleandroid.ui.listener.UpdateListener;


import java.util.Hashtable;
import java.util.List;

/**
 * Created by KCH on 2018-04-02.
 */

public class BaseActivity extends AppCompatActivity implements UpdateListener {

    private static volatile Activity mCurrentActivity = null;
    private ProgressDialog progressDlg;
    private long backKeyPressedTime;
    protected Activity context;
    public IntentData inData = new IntentData();
    protected Fragment curFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        // 세로만 지원
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Intent
        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey(Constants.INTENT_DATA_KEY)) {
            inData = intent.getParcelableExtra(Constants.INTENT_DATA_KEY);
        }

        progressDlg = new ProgressDialog(this, R.style.Theme_CustomProgressDialog);
        progressDlg.setContentView(R.layout.progress_dialog_material);


        startTransition();
    }

    public boolean backPressed()
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        if(count > 0)
        {
            fragmentManager.popBackStack();
            return true;
        }
        else
            return false;
    }

    @Override
    public void finish() {
        super.finish();
        endTransition();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void startIndicator(final String msg) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(!progressDlg.isShowing() && !isFinishing())
                {
                    if (msg.length() > 0) {
//						message.setText(msg);
                        progressDlg.setCancelable(false);
                    } else {
//						message.setText("");
                        progressDlg.setCancelable(true);
                    }
                    progressDlg.show();
                }
            }
        });
    }

    public void stopIndicator() {

        if(!isFinishing() && progressDlg != null && progressDlg.isShowing())
        {
            progressDlg.dismiss();
        }
    }

    public static Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public static void setCurrentActivity(Activity currentActivity) {
        mCurrentActivity = currentActivity;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ActivityManager actM = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> listm = actM.getRunningTasks(1);
            int iNumActivity = listm.get(0).numActivities;

            if (iNumActivity == 1) {
                if (System.currentTimeMillis() > backKeyPressedTime + 1500) {
                    backKeyPressedTime = System.currentTimeMillis();
                    Toast.makeText(this, getString(R.string.app_end), Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (System.currentTimeMillis() <= backKeyPressedTime + 1500) {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }

                return false;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    protected void startTransition() {
        switch (inData.aniType) {
            case Constants.VIEW_ANIMATION.ANI_END_ENTER:
                this.overridePendingTransition(R.anim.end_enter, R.anim.hold);
                break;
            case Constants.VIEW_ANIMATION.ANI_FADE:
                this.overridePendingTransition(R.anim.fadein, R.anim.hold);
                break;
            case Constants.VIEW_ANIMATION.ANI_FLIP:
                this.overridePendingTransition(R.anim.slide_left_in, R.anim.fadeout);
                break;
            case Constants.VIEW_ANIMATION.ANI_SLIDE_DOWN_IN:
                this.overridePendingTransition(R.anim.slide_up_in, R.anim.hold);
                break;
            case Constants.VIEW_ANIMATION.ANI_SLIDE_UP_IN:
                this.overridePendingTransition(R.anim.slide_down_in, R.anim.hold);
                break;

            case Constants.VIEW_ANIMATION.ANI_SLIDE_LEFT_IN:
                this.overridePendingTransition(R.anim.slide_left_in, R.anim.hold);
                break;

            case Constants.VIEW_ANIMATION.ANI_SLIDE_RIGHT_IN:
                this.overridePendingTransition(R.anim.slide_right_in, R.anim.fadeout);
                break;
        }
    }
    protected void endTransition() {
        switch (inData.aniType) {
            case Constants.VIEW_ANIMATION.ANI_END_ENTER:
                this.overridePendingTransition(R.anim.hold, R.anim.fadeout);
                break;
            case Constants.VIEW_ANIMATION.ANI_FADE:
                this.overridePendingTransition(R.anim.fadeout, R.anim.hold);
                break;
            case Constants.VIEW_ANIMATION.ANI_FLIP:
                this.overridePendingTransition(R.anim.fadein, R.anim.slide_right_out);
                break;

            case Constants.VIEW_ANIMATION.ANI_SLIDE_DOWN_IN:
                this.overridePendingTransition(R.anim.slide_down_out, R.anim.slide_down_out);
                break;

            case Constants.VIEW_ANIMATION.ANI_SLIDE_LEFT_IN:
                this.overridePendingTransition(R.anim.hold, R.anim.slide_right_out);
                break;

            case Constants.VIEW_ANIMATION.ANI_SLIDE_RIGHT_IN:
                this.overridePendingTransition(R.anim.fadein, R.anim.slide_left_out);
                break;
        }
    }

    private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

    protected static Typeface getCacheAsset(Context c, String assetPath) {
        synchronized (cache) {
            if (!cache.containsKey(assetPath)) {
                try {
                    Typeface t = Typeface.createFromAsset(c.getAssets(), assetPath);
                    cache.put(assetPath, t);
                } catch (Exception e) {
                    //Logger.log(Logger.LogState.E, "Could not get typeface '" + assetPath + "' because " + e.getMessage());
                    return null;
                }
            }
            return cache.get(assetPath);
        }
    }

    @Override
    public void addFragment(int menuId) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        /*
        if(menuId == Constants.FRAGMENT_MENUID.SINGUP)
        {
            curFragment = SignUpFragment.newInstance(menuId);
            fragmentTransaction.replace(R.id.content, curFragment, String.valueOf(menuId));
        }
        else if(menuId == Constants.FRAGMENT_MENUID.LOGIN)
        {
            curFragment = LoginFragment.newInstance(menuId);
            fragmentTransaction.replace(R.id.content, curFragment, String.valueOf(menuId));
        }
        else if(menuId == Constants.FRAGMENT_MENUID.RESET_PASSWORD)
        {
            curFragment = ResetPasswordFragment.newInstance(menuId);
            fragmentTransaction.replace(R.id.content, curFragment, String.valueOf(menuId));
        }
        */
        fragmentTransaction.commitAllowingStateLoss();

    }

    @Override
    public void fragmentBackPressed() {

    }

    @Override
    public void addFragment(Fragment fragment, int menuId) {

        curFragment = fragment;

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //fragmentTransaction.setCustomAnimations()
      //  fragmentTransaction.add(R.id.content, fragment, String.valueOf(menuId));
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commitAllowingStateLoss();

    }

    @Override
    public void notifyDataSetChanged() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearReferences();
    }

    private void clearReferences() {
        Activity currActivity = getCurrentActivity();
        if (currActivity != null && currActivity.equals(this)) {
            setCurrentActivity(null);
        }
    }
}
