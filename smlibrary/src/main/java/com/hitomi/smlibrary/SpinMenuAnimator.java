package com.hitomi.smlibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

/**
 * 菜单打开/关闭动画 <br/>
 *
 * Created by hitomi on 2016/9/19. <br/>
 *
 * github : https://github.com/Hitomis <br/>
 *
 * email : 196425254@qq.com
 */
public class SpinMenuAnimator {

    private final Interpolator interpolator = new OvershootInterpolator();

    private SpinMenuLayout spinMenuLayout;

    private SpinMenu spinMenu;

    private OnSpinMenuStateChangeListener onSpinMenuStateChangeListener;

    private float diffTranY;

    public SpinMenuAnimator(SpinMenu spinMenu, SpinMenuLayout spinMenuLayout, OnSpinMenuStateChangeListener listener) {
        this.spinMenu = spinMenu;
        this.spinMenuLayout = spinMenuLayout;
        this.onSpinMenuStateChangeListener = listener;
    }

    public void openMenuAnimator() {
        // 打开菜单之前，将菜单状态更新为 MENU_STATE_OPEN 并且显示 SpinMenuLayout
        spinMenu.updateMenuState(SpinMenu.MENU_STATE_OPEN);
        spinMenuLayout.setVisibility(View.VISIBLE);

        ViewGroup selectItemLayout = (ViewGroup) spinMenuLayout.getChildAt(spinMenuLayout.getSelectedPosition());
        final ViewGroup showingPager = (ViewGroup) spinMenu.getChildAt(spinMenu.getChildCount() - 1);
        final ViewGroup selectContainer = (ViewGroup) selectItemLayout.findViewWithTag(SpinMenu.TAG_ITEM_CONTAINER);
        final float scaleRatio = spinMenu.getScaleRatio();
        diffTranY = (showingPager.getHeight() * (1.f -  scaleRatio)) * .5f - selectItemLayout.getTop();

        // 获取当前菜单中间位置左边的菜单项，并设置右移动画
        ObjectAnimator leftTranXAnima = null, rightTranXAnima = null;
        if (spinMenuLayout.getSelectedPosition() - 1 > -1) {
            ViewGroup leftItemLayout = (ViewGroup) spinMenuLayout.getChildAt(spinMenuLayout.getSelectedPosition() - 1);
            leftTranXAnima = ObjectAnimator.ofFloat(leftItemLayout, "translationX", leftItemLayout.getTranslationX(), 0);
        } else if (spinMenuLayout.isCyclic() && spinMenuLayout.getSelectedPosition() == 0) {
            ViewGroup leftItemLayout = (ViewGroup) spinMenuLayout.getChildAt(spinMenuLayout.getMenuItemCount() - 1);
            leftTranXAnima = ObjectAnimator.ofFloat(leftItemLayout, "translationX", leftItemLayout.getTranslationX(), 0);
        }

        // 获取当前菜单中间位置右边的菜单项，并设置左移动画
        if (spinMenuLayout.getSelectedPosition() + 1 < spinMenuLayout.getChildCount()) {
            ViewGroup rightItemLayout = (ViewGroup) spinMenuLayout.getChildAt(spinMenuLayout.getSelectedPosition() + 1);
            rightTranXAnima = ObjectAnimator.ofFloat(rightItemLayout, "translationX", rightItemLayout.getTranslationX(), 0);
        } else if (spinMenuLayout.isCyclic() && spinMenuLayout.getSelectedPosition() + 1 == spinMenuLayout.getMenuItemCount()) {
            ViewGroup rightItemLayout = (ViewGroup) spinMenuLayout.getChildAt(0);
            rightTranXAnima = ObjectAnimator.ofFloat(rightItemLayout, "translationX", rightItemLayout.getTranslationX(), 0);
        }

        // 设置当前页面的缩放和上移动画
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

                // 从 selectContainer 中移除之前用来占位的 FrameLayout
                selectContainer.removeAllViews();

                // 将 showingPager 添加到 selectContainer 中
                FrameLayout.LayoutParams pagerParams = new FrameLayout.LayoutParams(
                        showingPager.getWidth(),
                        showingPager.getHeight()
                );
                selectContainer.addView(showingPager, pagerParams);

                // 校正 showingPager 在 selectContainer 中的位置
                float tranX = (showingPager.getWidth() * (1.f -  scaleRatio)) * .5f;
                float tranY = (showingPager.getHeight() * (1.f -  scaleRatio)) * .5f;
                showingPager.setTranslationX(-tranX);
                showingPager.setTranslationY(-tranY);

                if (onSpinMenuStateChangeListener != null) {
                    onSpinMenuStateChangeListener.onMenuOpened();
                }

                // 菜单打开后，允许滑动控制 spinMenuLayout 转动。且更新菜单状态为 MENU_STATE_OPENED
                spinMenuLayout.postEnable(true);
                spinMenu.updateMenuState(SpinMenu.MENU_STATE_OPENED);
            }
        });
    }

    public void closeMenuAnimator(SMItemLayout chooseItemLayout) {
        // 关闭菜单之前更新菜单状态为 MENU_STATE_CLOSE，并且不允许滑动控制 spinMenuLayout 转动
        spinMenu.updateMenuState(SpinMenu.MENU_STATE_CLOSE);
        spinMenuLayout.postEnable(false);

         // 从 chooseItemLayout 中移除包含显示 Fragment 的 FrameLayout
        FrameLayout frameContainer = (FrameLayout) chooseItemLayout.findViewWithTag(SpinMenu.TAG_ITEM_CONTAINER);
        FrameLayout pagerLayout = (FrameLayout) frameContainer.findViewWithTag(SpinMenu.TAG_ITEM_PAGER);
        frameContainer.removeView(pagerLayout);

        // 创建一个用来占位的 FrameLayout
        FrameLayout.LayoutParams pagerFrameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout holderLayout = new FrameLayout(chooseItemLayout.getContext());
        holderLayout.setLayoutParams(pagerFrameParams);

        // 将占位的 FrameLayout 添加到 chooseItemLayout 布局中的 frameContainer 中
        frameContainer.addView(holderLayout);

        // 添加 pagerLayout 添加到 SpinMenu 中
        pagerLayout.setLayoutParams(pagerFrameParams);
        spinMenu.addView(pagerLayout);

        // 放置 pagerLayout 到同一个位置
        int currTranX = (int) (spinMenu.getWidth() * (1.f - spinMenu.getScaleRatio()) * .5f);
        int currTranY = (int) (spinMenu.getHeight() * (1.f - spinMenu.getScaleRatio()) * .5f - diffTranY);
        pagerLayout.setTranslationX(currTranX);
        pagerLayout.setTranslationY(currTranY);
        pagerLayout.setScaleX(spinMenu.getScaleRatio());
        pagerLayout.setScaleY(spinMenu.getScaleRatio());

        // 获取当前菜单中间位置左边的菜单项，并设置左移动画
        ObjectAnimator leftTranXAnima = null, rightTranXAnima = null;
        if (spinMenuLayout.getSelectedPosition() - 1 > -1) {
            ViewGroup leftItemLayout = (ViewGroup) spinMenuLayout.getChildAt(spinMenuLayout.getSelectedPosition() - 1);
            leftTranXAnima = ObjectAnimator.ofFloat(leftItemLayout, "translationX",
                    leftItemLayout.getTranslationX(), -SpinMenu.TRAN_SKNEW_VALUE);
        } else if (spinMenuLayout.isCyclic() && spinMenuLayout.getSelectedPosition() == 0) {
            ViewGroup leftItemLayout = (ViewGroup) spinMenuLayout.getChildAt(spinMenuLayout.getMenuItemCount() - 1);
            leftTranXAnima = ObjectAnimator.ofFloat(leftItemLayout, "translationX",
                    leftItemLayout.getTranslationX(), -SpinMenu.TRAN_SKNEW_VALUE);
        }

        // 获取当前菜单中间位置右边的菜单项，并设置右移动画
        if (spinMenuLayout.getSelectedPosition() + 1 < spinMenuLayout.getChildCount()) {
            ViewGroup rightItemLayout = (ViewGroup) spinMenuLayout.getChildAt(spinMenuLayout.getSelectedPosition() + 1);
            rightTranXAnima = ObjectAnimator.ofFloat(rightItemLayout, "translationX",
                    rightItemLayout.getTranslationX(), SpinMenu.TRAN_SKNEW_VALUE);
        } else if (spinMenuLayout.isCyclic() && spinMenuLayout.getSelectedPosition() + 1 == spinMenuLayout.getMenuItemCount()) {
            ViewGroup rightItemLayout = (ViewGroup) spinMenuLayout.getChildAt(0);
            rightTranXAnima = ObjectAnimator.ofFloat(rightItemLayout, "translationX",
                    rightItemLayout.getTranslationX(), SpinMenu.TRAN_SKNEW_VALUE);
        }

        // 设置当前选中菜单的缩放，左右和下移动画
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
                if (onSpinMenuStateChangeListener != null) {
                    onSpinMenuStateChangeListener.onMenuClosed();
                }

                // 菜单关闭后，设置 spinMenuLayout 隐藏，菜单状态更新为 MENU_STATE_CLOSED
                spinMenuLayout.setVisibility(View.GONE);
                spinMenu.updateMenuState(SpinMenu.MENU_STATE_CLOSED);
            }
        });
    }
}
