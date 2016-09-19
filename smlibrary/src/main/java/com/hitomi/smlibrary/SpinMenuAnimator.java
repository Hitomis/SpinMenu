package com.hitomi.smlibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * Created by hitomi on 2016/9/19.
 */
public class SpinMenuAnimator {

    private final Interpolator interpolator = new OvershootInterpolator();

    private SpinMenuLayout spinMenuLayout;

    private SpinMenu spinMenu;

    private float diffTranY;

    public SpinMenuAnimator(SpinMenu spinMenu, SpinMenuLayout spinMenuLayout) {
        this.spinMenu = spinMenu;
        this.spinMenuLayout = spinMenuLayout;
    }

    public void openMenuAnimator() {
        ViewGroup showingViewGroup = (ViewGroup) spinMenu.getChildAt(spinMenu.getChildCount() - 1);
        final ViewGroup selectItemLayout = (ViewGroup) spinMenuLayout.getChildAt(spinMenuLayout.getSelectedPosition());
        final ViewGroup showingPager = (ViewGroup) showingViewGroup.findViewWithTag(SpinMenu.TAG_ITEM_PAGER);
        final float scaleRatio = spinMenu.getScaleRatio();
        diffTranY = (showingPager.getHeight() * (1.f -  scaleRatio)) * .5f - selectItemLayout.getTop();

        ObjectAnimator leftTranXAnima = null, rightTranXAnima = null;
        if (spinMenuLayout.getSelectedPosition() - 1 > -1) {
            ViewGroup leftItemLayout = (ViewGroup) spinMenuLayout.getChildAt(spinMenuLayout.getSelectedPosition() - 1);
            leftTranXAnima = ObjectAnimator.ofFloat(leftItemLayout, "translationX", leftItemLayout.getTranslationX(), 0);
        }
        if (spinMenuLayout.getSelectedPosition() + 1 < spinMenuLayout.getChildCount()) {
            ViewGroup rightItemLayout = (ViewGroup) spinMenuLayout.getChildAt(spinMenuLayout.getSelectedPosition() + 1);
            rightTranXAnima = ObjectAnimator.ofFloat(rightItemLayout, "translationX", rightItemLayout.getTranslationX(), 0);
        }

        ObjectAnimator scaleXAnima = ObjectAnimator.ofFloat(
                showingPager, "scaleX", showingPager.getScaleX(), scaleRatio);
        ObjectAnimator scaleYAnima = ObjectAnimator.ofFloat(
                showingPager, "scaleY", showingPager.getScaleY(), scaleRatio);
        ObjectAnimator tranYAnima = ObjectAnimator.ofFloat(
                showingPager, "translationY", showingPager.getTranslationY(), -diffTranY
        );

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(interpolator);
        AnimatorSet.Builder animaBuilder = animatorSet.play(scaleXAnima)
                .with(scaleYAnima)
                .with(tranYAnima);
        if (leftTranXAnima != null) {
            animaBuilder.with(leftTranXAnima);
        }
        if (rightTranXAnima != null) {
            animaBuilder.with(rightTranXAnima);
        }
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

    public void closeMenuAnimator(SMItemLayout chooseItemLayout) {
         // 从 chooseItemLayout 中移除 包含显示 Fragment 的 FrameLayout
        FrameLayout pagerLayout = (FrameLayout) chooseItemLayout.findViewWithTag(SpinMenu.TAG_ITEM_PAGER);
        chooseItemLayout.removeView(pagerLayout);

        // 创建一个用来占位的 FrameLayout
        int pagerWidth = (int) (spinMenu.getWidth() * spinMenu.getScaleRatio());
        int pagerHeight = (int) (spinMenu.getHeight() * spinMenu.getScaleRatio());
        LinearLayout.LayoutParams pagerLinLayParams = new LinearLayout.LayoutParams(pagerWidth, pagerHeight);
        FrameLayout holderLayout = new FrameLayout(chooseItemLayout.getContext());
        holderLayout.setLayoutParams(pagerLinLayParams);

        // 将占位的 FrameLayout 添加到 chooseItemLayout 布局中的第一个位置（第二个位子是 Hint）
        chooseItemLayout.addView(holderLayout, 0);

        // 添加 pagerLayout 添加到 SpinMenu 中
        FrameLayout.LayoutParams pagerFrameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        pagerLayout.setLayoutParams(pagerFrameParams);
        spinMenu.addView(pagerLayout);

        // 放置 pagerLayout 到同一个位置
        int currTranX = (int) (spinMenu.getWidth() * (1.f - spinMenu.getScaleRatio()) * .5f);
        int currTranY = (int) (spinMenu.getHeight() * (1.f - spinMenu.getScaleRatio()) * .5f - diffTranY);
        pagerLayout.setTranslationX(currTranX);
        pagerLayout.setTranslationY(currTranY);
        pagerLayout.setScaleX(spinMenu.getScaleRatio());
        pagerLayout.setScaleY(spinMenu.getScaleRatio());

        // 启动动画
        ObjectAnimator leftTranXAnima = null, rightTranXAnima = null;
        if (spinMenuLayout.getSelectedPosition() - 1 > -1) {
            ViewGroup leftItemLayout = (ViewGroup) spinMenuLayout.getChildAt(spinMenuLayout.getSelectedPosition() - 1);
            leftTranXAnima = ObjectAnimator.ofFloat(leftItemLayout, "translationX", leftItemLayout.getTranslationX(), -SpinMenu.TRAN_SKNEW_VALUE);
        }
        if (spinMenuLayout.getSelectedPosition() + 1 < spinMenuLayout.getChildCount()) {
            ViewGroup rightItemLayout = (ViewGroup) spinMenuLayout.getChildAt(spinMenuLayout.getSelectedPosition() + 1);
            rightTranXAnima = ObjectAnimator.ofFloat(rightItemLayout, "translationX", rightItemLayout.getTranslationX(), SpinMenu.TRAN_SKNEW_VALUE);
        }

        ObjectAnimator scaleXAnima =  ObjectAnimator.ofFloat(pagerLayout, "scaleX", pagerLayout.getScaleX(), 1.f);
        ObjectAnimator scaleYAnima =  ObjectAnimator.ofFloat(pagerLayout, "scaleY", pagerLayout.getScaleX(), 1.f);
        ObjectAnimator tranXAnima = ObjectAnimator.ofFloat(pagerLayout, "translationX", 0, 0);
        ObjectAnimator tranYAnima = ObjectAnimator.ofFloat(pagerLayout, "translationY", -diffTranY, 0);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(interpolator);
        AnimatorSet.Builder animaBuilder = animatorSet.play(scaleXAnima)
                .with(scaleYAnima)
                .with(tranXAnima)
                .with(tranYAnima);
        if (leftTranXAnima != null) {
            animaBuilder.with(leftTranXAnima);
        }
        if (rightTranXAnima != null) {
            animaBuilder.with(rightTranXAnima);
        }
        animatorSet.start();

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

            }
        });
    }
}
