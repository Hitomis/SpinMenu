package com.hitomi.smlibrary;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by hitomi on 2016/9/13.
 */
public class SpinMenuLayout extends ViewGroup {

    /**
     * 计算半径的比例系数
     */
    private static final float RADIUS_SAPCE_RATIO = 1.2f;

    /**
     * View 之间间隔的角度
     */
    private static final int ANGEL_SPACE = 45;

    private VelocityTracker vTracker;

    private int startAngle, delayAngle;

    private float radius;

    private float preX, preY;

    public SpinMenuLayout(Context context) {
        this(context, null);
    }

    public SpinMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpinMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(Math.min(width, height), Math.max(width, height));

        if (getChildCount() > 0) {
            // 对子元素进行测量
            measureChildren(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean b, int left, int top, int right, int bottom) {
        final int childCount = getChildCount();
        if (childCount <= 0) return;

        delayAngle %= 360;
        startAngle = delayAngle;

        View child;
        int childWidth, childHeight;
        int centerX = getMeasuredWidth() / 2;
        int centerY = getMeasuredHeight();
        radius = centerX * RADIUS_SAPCE_RATIO + getChildAt(0).getMeasuredHeight() / 2;

        for (int i = 0; i < childCount; i++) {
            child = getChildAt(i);
            childWidth = child.getMeasuredWidth();
            childHeight = child.getMeasuredHeight();

            left = (int) (centerX + Math.sin(Math.toRadians(startAngle)) * radius);
            top = (int) (centerY - Math.cos(Math.toRadians(startAngle)) * radius);

            child.layout(left - childWidth / 2, top - childHeight / 2,
                    left + childWidth / 2, top + childHeight / 2);

            child.setRotation(startAngle);
            startAngle += ANGEL_SPACE;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        float curX = ev.getX();
        float curY = ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preX = curX;
                preY = curY;

                if (vTracker == null) {
                    vTracker = VelocityTracker.obtain();
                } else {
                    vTracker.clear();
                }
                vTracker.addMovement(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                vTracker.addMovement(ev);
                vTracker.computeCurrentVelocity(1000);

                float start = getAngle(preX, preY);
                float end = getAngle(curX, curY);
                delayAngle += end - start;
                requestLayout();

                preX = curX;
                preY = curY;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                vTracker.recycle();
                break;
        }
        return true;
    }

    private float getAngle(float xTouch, float yTouch) {
        double x = Math.abs(xTouch - radius);
        double y = Math.abs(getMeasuredHeight() - yTouch - radius);
        return (float) (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
    }

}
