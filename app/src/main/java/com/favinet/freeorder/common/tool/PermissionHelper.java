package com.favinet.freeorder.common.tool;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;

/**
 * Created by KCH on 2018-04-02.
 */

public class PermissionHelper {

    private static PermissionHelper   permissionHelper;
    private Activity activity;

    private String[] permissions;
    private ArrayList<String> needPermissions = new ArrayList<>();

    private PermissionCallback callback;

    public interface PermissionCallback {
        void onPermissionResult(String[] permissions, int[] grantResults);
    }

    public static PermissionHelper getInstance() {
        if (permissionHelper == null) {
            return permissionHelper = new PermissionHelper();
        } else {
            return permissionHelper;
        }
    }

    public void setPermissionAndActivity(String[] permissions, Activity activity)
    {
        this.permissions = permissions;
        this.activity = activity;
        this.needPermissions.clear();
    }

    public void setPermissions(String[] permissions)
    {
        this.permissions = permissions;
        this.needPermissions.clear();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean checkPermission() {

        boolean accept = true;

        for(String permission : permissions)
        {
            if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
            {
                if(!activity.shouldShowRequestPermissionRationale(permission))
                {
                    needPermissions.add(permission);
                    accept = false;
                }
                else
                {
                    if(permission == Manifest.permission.READ_PHONE_STATE)
                    {
                        needPermissions.add(permission);
                        accept = false;
                    }
                }
            }
        }

        if(accept) activity = null;

        return accept;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean checkPermissionInApp() {

        boolean accept = true;

        for(String permission : permissions)
        {
            if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
            {
                needPermissions.add(permission);
                accept = false;
            }
        }

        if(accept) activity = null;

        return accept;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermission(int requestCode, PermissionCallback callback)
    {
        this.callback = callback;
        activity.requestPermissions(needPermissions.toArray(new String[needPermissions.size()]), requestCode);

        activity = null;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(callback != null) callback.onPermissionResult(permissions, grantResults);
    }

}
