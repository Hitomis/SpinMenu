package com.hitomi.smlibrary;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by hitomi on 2016/9/13.<br/>
 *
 * github : https://github.com/Hitomis<br/>
 *
 * email : 196425254@qq.com
 */
public class SpinMenuItem extends LinearLayout {

    private static final float SCALE_RATIO = .38f;

    private float defaultHintTop;

    public SpinMenuItem(Context context) {
        this(context, null);
    }

    public SpinMenuItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpinMenuItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        defaultHintTop = screenWidth * .5f * .2f;

        setOrientation(VERTICAL);
    }

    @Override
    protected void onLayout(boolean b, int left, int top, int right, int bottom) {
        final int childCount = getChildCount();
        if (childCount <= 0) return;


    }
}
