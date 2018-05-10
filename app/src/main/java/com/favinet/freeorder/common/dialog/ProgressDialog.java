package com.favinet.freeorder.common.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.favinet.freeorder.R;

/**
 * Created by KCH on 2018-04-02.
 */

public class ProgressDialog extends Dialog {


    private ImageView progressWheel;
    private AnimationDrawable frameAnimation;

    public ProgressDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    public void setContentView(final int layoutResID) {
        super.setContentView(layoutResID);

        progressWheel = (ImageView) this.findViewById(R.id.progress_wheel);
        frameAnimation = (AnimationDrawable) progressWheel.getBackground();
    }
}
