package com.hitomi.smlibrary;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by hitomi on 2016/9/18.
 */
public class SMItemLayout extends LinearLayout{

    public SMItemLayout(Context context) {
        this(context, null);
    }

    public SMItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SMItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(VERTICAL);
    }
}
