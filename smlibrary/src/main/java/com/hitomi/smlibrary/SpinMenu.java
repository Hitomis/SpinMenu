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

    static final String TAG_ITEM_PAGER = "tag_item_pager";

    static final String TAG_ITEM_HINT = "tag_item_hint";

    static final float SCALE_RATIO = .36f;

    static final float TRAN_SKNEW_VALUE = 160;

    private SpinMenuLayout spinMenuLayout;

    private SpinMenuAnimator spinMenuAnimator;

    private PagerAdapter pagerAdapter;

    private List pagerObjects;

    private List<SMItemLayout> smItemLayoutList;

    private List<String> hintStrList;

    private boolean init;

    private boolean isOpen;

    private float scaleRatio = SCALE_RATIO;

    private OnSpinSelectedListener onSpinSelectedListener = new OnSpinSelectedListener() {
        @Override
        public void onSpinSelectedListener(int position) {
//            Toast.makeText(getContext(), "" + position, Toast.LENGTH_SHORT).show();

        }
    };

    private com.hitomi.smlibrary.onMenuSelectedListener onMenuSelectedListener = new onMenuSelectedListener() {
        @Override
        public void onMenuSelectedListener(SMItemLayout smItemLayout) {
            if (isOpen) {
                closeMenu(smItemLayout);
            }
        }
    };

    public SpinMenu(Context context) {
        this(context, null);
    }

    public SpinMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpinMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init = true;
        isOpen = false;
        pagerObjects = new ArrayList();
        smItemLayoutList = new ArrayList<>();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        @IdRes final int smLayoutId = 0x6F060505;
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        spinMenuLayout = new SpinMenuLayout(getContext());
        spinMenuLayout.setId(smLayoutId);
        spinMenuLayout.setLayoutParams(layoutParams);
        spinMenuLayout.setOnSpinSelectedListener(onSpinSelectedListener);
        spinMenuLayout.setOnMenuSelectedListener(onMenuSelectedListener);
        addView(spinMenuLayout);

        spinMenuAnimator = new SpinMenuAnimator(this, spinMenuLayout);
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
                pagerLayout = (FrameLayout) smItemLayout.findViewWithTag(TAG_ITEM_PAGER);
                if (i == 0) { // 初始菜单的时候，默认显示第一个 Fragment
                    // 先移除第一个包含 Fragment 的布局
                    smItemLayout.removeView(pagerLayout);

                    // 创建一个用来占位的 FrameLayout
                    FrameLayout holderLayout = new FrameLayout(getContext());
                    holderLayout.setLayoutParams(pagerLinLayParams);

                    // 将占位的 FrameLayout 添加到布局中的第一个位置（第二个位子是 Hint）
                    smItemLayout.addView(holderLayout, 0);

                    // 添加 第一个包含 Fragment 的布局添加到 SpinMenu 中
                    FrameLayout.LayoutParams pagerFrameParams = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
                    pagerLayout.setLayoutParams(pagerFrameParams);
                    addView(pagerLayout);
                } else {
                    pagerLayout.setLayoutParams(pagerLinLayParams); // 重新调整大小
                }
                // 显示标题
                if (hintStrList != null && !hintStrList.isEmpty() && i < hintStrList.size()) {
                    tvHint = (TextView) smItemLayout.findViewWithTag(TAG_ITEM_HINT);
                    tvHint.setText(hintStrList.get(i));
                }
                // 位于菜单中当前显示 Fragment 两边的 SMItemlayout 左右移动 TRAN_SKNEW_VALUE 个距离
                if (spinMenuLayout.getSelectedPosition() + 1 == i) {
                    smItemLayout.setTranslationX(TRAN_SKNEW_VALUE);
                } else if (spinMenuLayout.getSelectedPosition() - 1 == i) {
                    smItemLayout.setTranslationX(-TRAN_SKNEW_VALUE);
                } else {
                    smItemLayout.setTranslationX(0);
                }
            }
            init = false;
        }
    }

    public void setFragmentAdapter(PagerAdapter adapter) {
        if (pagerAdapter != null) {
            pagerAdapter.startUpdate(spinMenuLayout);
            for (int i = 0; i < adapter.getCount(); i++) {
                ViewGroup pager = (ViewGroup) spinMenuLayout.getChildAt(i).findViewWithTag(TAG_ITEM_PAGER);
                pagerAdapter.destroyItem(pager, i, pagerObjects.get(i));
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
            framePager.setTag(TAG_ITEM_PAGER);
            framePager.setLayoutParams(pagerLinLayParams);
            Object object = pagerAdapter.instantiateItem(framePager, i);

            // 创建菜单标题 TextView
            TextView tvHint = new TextView(getContext());
            tvHint.setId(pagerCount * 2 + i + 1);
            tvHint.setTag(TAG_ITEM_HINT);
            tvHint.setLayoutParams(hintLinLayParams);

            smItemLayout.addView(framePager);
            smItemLayout.addView(tvHint);
            spinMenuLayout.addView(smItemLayout);

            pagerObjects.add(object);
            smItemLayoutList.add(smItemLayout);
        }
        pagerAdapter.finishUpdate(spinMenuLayout);
    }

    public void openMenu() {
//        if (!isOpen) {
            spinMenuAnimator.openMenuAnimator();
            isOpen = !isOpen;
//        }
    }

    public void closeMenu(SMItemLayout chooseItemLayout) {
        if (isOpen) {
            spinMenuAnimator.closeMenuAnimator(chooseItemLayout);
            isOpen = !isOpen;
        }
    }

    public void setMenuItemScaleValue(float scaleValue) {
        scaleRatio = scaleValue;
    }

    public void setHintTextList(List<String> hintTextList) {
        hintStrList = hintTextList;
    }

    public float getScaleRatio() {
        return scaleRatio;
    }
}
