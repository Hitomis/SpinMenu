package com.hitomi.smlibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

/**
 * Created by hitomi on 2016/9/19.
 */
public class SpinMenuAnimator {

    private final Interpolator interpolator = new OvershootInterpolator();

    private SpinMenuLayout spinMenuLayout;

    private SpinMenu spinMenu;

    public SpinMenuAnimator(SpinMenu spinMenu, SpinMenuLayout spinMenuLayout) {
        this.spinMenu = spinMenu;
        this.spinMenuLayout = spinMenuLayout;
    }

    public void openMenuAnimator() {
        ViewGroup showingViewGroup = (ViewGroup) spinMenu.getChildAt(spinMenu.getChildCount() - 1);
        final ViewGroup selectItemLayout = (ViewGroup) spinMenuLayout.getChildAt(spinMenuLayout.getSelectedPosition());
        final ViewGroup showingPager = (ViewGroup) showingViewGroup.findViewWithTag(SpinMenu.TAG_ITEM_PAGER);
        final float scaleRatio = spinMenu.getScaleRatio();
        float endTranY = (showingPager.getHeight() * (1.f -  scaleRatio)) * .5f - selectItemLayout.getTop();

        ObjectAnimator scaleXAnima = ObjectAnimator.ofFloat(
                showingPager, "scaleX", showingPager.getScaleX(), scaleRatio);
        ObjectAnimator scaleYAnima = ObjectAnimator.ofFloat(
                showingPager, "scaleY", showingPager.getScaleY(), scaleRatio);
        ObjectAnimator tranYAnima = ObjectAnimator.ofFloat(
                showingPager, "translationY", showingPager.getTranslationY(), -endTranY
        );

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(interpolator);
        animatorSet.play(scaleXAnima).with(scaleYAnima).with(tranYAnima);
        animatorSet.start();

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // 从 SpinMenu 中移除 showingPager
                spinMenu.removeView(showingPager);

                // 从 selectChild 中移除之前用来占位的 FrameLayout
                selectItemLayout.removeViewAt(0);

                // 将 showingPager 添加到 selectItemLayout 中

                LinearLayout.LayoutParams pagerParams = new LinearLayout.LayoutParams(
                        (int) (showingPager.getWidth() * scaleRatio),
                        (int) (showingPager.getHeight() * scaleRatio)
                );
                selectItemLayout.addView(showingPager, 0, pagerParams);

                showingPager.setTranslationY(0);
                showingPager.setScaleX(1.f);
                showingPager.setScaleY(1.f);
            }
        });
    }
}
