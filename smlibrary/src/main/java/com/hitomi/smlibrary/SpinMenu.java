package com.hitomi.smlibrary;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by hitomi on 2016/9/18.
 */
public class SpinMenu extends FrameLayout {

    private SpinMenuLayout spinMenuLayout;

    private PagerAdapter pagerAdapter;

    private List pagerObjects;

    public SpinMenu(Context context) {
        this(context, null);
    }

    public SpinMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpinMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        pagerObjects = new ArrayList();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        spinMenuLayout = new SpinMenuLayout(getContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        spinMenuLayout.setLayoutParams(layoutParams);
        addView(spinMenuLayout);
    }

    public void setFragmentAdapter(PagerAdapter adapter) {
        if (pagerAdapter != null) {
            pagerAdapter.startUpdate(spinMenuLayout);
            for (int i = 0; i < adapter.getCount(); i++) {
                pagerAdapter.destroyItem((ViewGroup) spinMenuLayout.getChildAt(i + 1), i, pagerObjects.get(i));
            }
            pagerAdapter.finishUpdate(spinMenuLayout);
        }

        pagerAdapter.startUpdate(spinMenuLayout);
        pagerAdapter = adapter;
        LinearLayout.LayoutParams linLayParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            SMItemLayout itemLayout = new SMItemLayout(getContext());
            itemLayout.setId(i + 1);
            itemLayout.setLayoutParams(linLayParams);
            spinMenuLayout.addView(itemLayout);

            Object object = pagerAdapter.instantiateItem(itemLayout, i);
            pagerObjects.add(object);
        }
        pagerAdapter.finishUpdate(spinMenuLayout);
    }
}
