package com.hitomi.smlibrary;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by hitomi on 2016/9/18.
 */
public class SpinMenu extends FrameLayout {

    static final float SCALE_RATIO = .36f;

    private SpinMenuLayout spinMenuLayout;

    private PagerAdapter pagerAdapter;

    private List pagerObjects;

    private List<SMItemLayout> smItemLayoutList;

    private boolean init;

    private float scaleRatio = SCALE_RATIO;

    public SpinMenu(Context context) {
        this(context, null);
    }

    public SpinMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpinMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init = true;
        pagerObjects = new ArrayList();
        smItemLayoutList = new ArrayList<>();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        spinMenuLayout = new SpinMenuLayout(getContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        spinMenuLayout.setLayoutParams(layoutParams);
        addView(spinMenuLayout);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (init) {
            int pagerWidth = (int) (getMeasuredWidth() * scaleRatio);
            int pagerHeight = (int) (getMeasuredHeight() * scaleRatio);

            LinearLayout.LayoutParams pagerLinLayParams = new LinearLayout.LayoutParams(pagerWidth, pagerHeight);
            FrameLayout pagerLayout;
            for (int i = 0; i < smItemLayoutList.size(); i++) {
                pagerLayout = (FrameLayout) smItemLayoutList.get(i).getChildAt(0);
                pagerLayout.setLayoutParams(pagerLinLayParams);
            }
            init = false;
        }
    }

    public void setFragmentAdapter(PagerAdapter adapter) {
        if (pagerAdapter != null) {
            pagerAdapter.startUpdate(spinMenuLayout);
            for (int i = 0; i < adapter.getCount(); i++) {
                pagerAdapter.destroyItem((ViewGroup) spinMenuLayout.getChildAt(i + 1), i, pagerObjects.get(i));
            }
            pagerAdapter.finishUpdate(spinMenuLayout);
        }

        pagerAdapter = adapter;
        int pagerCount = pagerAdapter.getCount();

        ViewGroup.LayoutParams itemLinLayParams = new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        LinearLayout.LayoutParams pagerLinLayParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        LinearLayout.LayoutParams hintLinLayParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        hintLinLayParams.topMargin = 15;
        hintLinLayParams.gravity = Gravity.CENTER;
        for (int i = 0; i < pagerCount; i++) {
            // 创建菜单父容器布局
            SMItemLayout smItemLayout = new SMItemLayout(getContext());
            smItemLayout.setId(i + 1);
            smItemLayout.setLayoutParams(itemLinLayParams);

            // 创建 Fragment 容器
            FrameLayout framePager = new FrameLayout(getContext());
            framePager.setId(pagerCount + i + 1);
            framePager.setLayoutParams(pagerLinLayParams);
            Object object = pagerAdapter.instantiateItem(framePager, i);

            // 创建菜单标题 View
            TextView tvHint = new TextView(getContext());
            tvHint.setId(pagerCount * 2 + i + 1);
            tvHint.setLayoutParams(hintLinLayParams);
            tvHint.setText("测试");

            smItemLayout.addView(framePager);
            smItemLayout.addView(tvHint);
            spinMenuLayout.addView(smItemLayout);

            pagerObjects.add(object);
            smItemLayoutList.add(smItemLayout);
        }
        pagerAdapter.finishUpdate(spinMenuLayout);
    }
}
