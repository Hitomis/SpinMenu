package com.hitomi.smlibrary;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

/**
 * Created by hitomi on 2016/9/13.
 */
public class SpinMenuLayout extends ViewGroup implements Runnable{

    /**
     * 计算半径的比例系数
     */
    private static final float RADIUS_SAPCE_RATIO = 1.2f;

    /**
     * View 之间间隔的角度
     */
    private static final int ANGEL_SPACE = 45;

    /**
     * 点击与触摸的切换阀值
     */
    private int touchSlop = 8;

    private float delayAngle, perAngle;

    private float radius;

    private float preX, preY;

    private long preTimes;

    /**
     * 是否顺时针
     */
    private boolean wise;

    /**
     * 是否正在自动滚动
     */
    private boolean isFling;

    private float anglePerSecond;

    public SpinMenuLayout(Context context) {
        this(context, null);
    }

    public SpinMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpinMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
            ViewConfiguration conf = ViewConfiguration.get(getContext());
            touchSlop = conf.getScaledTouchSlop();
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

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
        float startAngle = delayAngle;

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
        float curX = ev.getX();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preX = curX;
                break;
            case MotionEvent.ACTION_MOVE:
                // 当手指拖动值大于 TouchSlop 值时，认为应该进行滚动，拦截子控件的事件
                float diffX = Math.abs(curX - preX);
                preX = curX;
                if (diffX > touchSlop) {
                    return true;
                }
                break;
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
                preTimes = System.currentTimeMillis();
                perAngle = 0;

                if (isFling) {
                    // 移除快速滚动的回调
                    removeCallbacks(this);
                    isFling = false;
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float diffX = curX - preX;
                if (Math.abs(diffX) < touchSlop) break;
                float start = getAngle(preX, preY);
                float end = getAngle(curX, curY);

                if (diffX > 0) {
                    wise = true;
                    delayAngle += Math.abs(start - end);
                    perAngle += Math.abs(start - end);
                } else {
                    wise = false;
                    delayAngle -= Math.abs(end - start);
                    perAngle -= Math.abs(end - start);
                }

                requestLayout();

                preX = curX;
                preY = curY;
                break;
            case MotionEvent.ACTION_UP:
                anglePerSecond = perAngle * 1000 / (System.currentTimeMillis() - preTimes);
                if (Math.abs(anglePerSecond) > 100 && !isFling) {
                    // post一个任务，去自动滚动
                    post(this);
                    return true;
                }
                if (Math.abs(perAngle) > 3) {
                    return true;
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    private float getAngle(float xTouch, float yTouch) {
        // 圆心点在底边的中点上，根据圆心点转化为对应坐标x, y
        float x = Math.abs(xTouch - getMeasuredWidth() / 2);
        float y = Math.abs(getMeasuredHeight() - yTouch);
        return (float) (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
    }


    @Override
    public void run() {
        if (Math.abs(anglePerSecond) < 12) {
            isFling = false;
            return;
        }
        isFling = true;
        if (wise) {
            delayAngle += (Math.abs(anglePerSecond) / 12);
        } else {
            delayAngle -= (Math.abs(anglePerSecond) / 12);
        }

        anglePerSecond /= 1.2f;
        postDelayed(this, 15);
        requestLayout();
    }
}
