package com.hitomi.smlibrary;

import android.content.Context;
import android.support.annotation.IdRes;
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

    private List<String> hintStrList;

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

        @IdRes int smLayoutId = 0x6F060505;
        spinMenuLayout = new SpinMenuLayout(getContext());
        spinMenuLayout.setId(smLayoutId);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        spinMenuLayout.setLayoutParams(layoutParams);
        addView(spinMenuLayout);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (init) {
            // 根据 scaleRatio 去调整菜单中 Fragment 视图的整体大小
            int pagerWidth = (int) (getMeasuredWidth() * scaleRatio);
            int pagerHeight = (int) (getMeasuredHeight() * scaleRatio);

            LinearLayout.LayoutParams pagerLinLayParams = new LinearLayout.LayoutParams(pagerWidth, pagerHeight);
            FrameLayout pagerLayout;
            SMItemLayout smItemLayout;
            TextView tvHint;
            for (int i = 0; i < smItemLayoutList.size(); i++) {
                smItemLayout = smItemLayoutList.get(i);
                pagerLayout = (FrameLayout) smItemLayout.getChildAt(0);
                if (i == 0) { // 初始菜单的时候，默认显示第一个 Fragment
                    // 先移除第一个 Fragment 的布局
                    smItemLayout.removeView(pagerLayout);

                    // 创建一个用来占位的 FrameLayout
                    FrameLayout holderLayout = new FrameLayout(getContext());
                    holderLayout.setLayoutParams(pagerLinLayParams);

                    // 将占位的 FrameLayout 添加到布局中的第一个位置（第二个位子是 Hint）
                    smItemLayout.addView(holderLayout, 0);

                    // 添加 第一个包含 Fragment 的布局到 SpinMenu 中
                    FrameLayout.LayoutParams pagerFrameParams = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
                    pagerLayout.setLayoutParams(pagerFrameParams);
                    addView(pagerLayout);
                } else {
                    pagerLayout.setLayoutParams(pagerLinLayParams); // 重新调整大小

                    if (hintStrList != null && !hintStrList.isEmpty() && i < hintStrList.size()) { // 显示标题
                        tvHint = (TextView) smItemLayout.getChildAt(1);
                        tvHint.setText(hintStrList.get(i));
                    }
                }
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
        pagerAdapter.startUpdate(spinMenuLayout);
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

            // 创建菜单标题 TextView
            TextView tvHint = new TextView(getContext());
            tvHint.setId(pagerCount * 2 + i + 1);
            tvHint.setLayoutParams(hintLinLayParams);

            smItemLayout.addView(framePager);
            smItemLayout.addView(tvHint);
            spinMenuLayout.addView(smItemLayout);

            pagerObjects.add(object);
            smItemLayoutList.add(smItemLayout);
        }
        pagerAdapter.finishUpdate(spinMenuLayout);
    }

    public void setMenuItemScaleValue(float scaleValue) {
        scaleRatio = scaleValue;
    }

    public void setHintTextList(List<String> hintTextList) {
        hintStrList = hintTextList;
    }
}
