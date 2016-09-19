package com.hitomi.smlibrary;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by hitomi on 2016/9/13.
 */
public class SpinMenuLayout extends ViewGroup implements Runnable, View.OnClickListener{

    /**
     * View 之间间隔的角度
     */
    private static final int ANGLE_SPACE = 45;

    /**
     * View 旋转时最小转动角度的速度
     */
    private static final int MIN_PER_ANGLE = ANGLE_SPACE;

    private float delayAngle, perAngle;

    private float radius;

    private float preX, preY;

    private long preTimes;

    private float anglePerSecond;

    private boolean isCyclic;

    private Scroller scroller;

    private int minFlingAngle, maxFlingAngle;

    private OnSpinSelectedListener onSpinSelectedListener;

    private OnMenuSelectedListener onMenuSelectedListener;

    public SpinMenuLayout(Context context) {
        this(context, null);
    }

    public SpinMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpinMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        scroller = new Scroller(context);
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

        isCyclic = getChildCount() == 360 / MIN_PER_ANGLE;
        computeFlingLimitAngle();

        delayAngle %= 360;
        float startAngle = delayAngle;

        View child;
        int childWidth, childHeight;
        int centerX = getMeasuredWidth() / 2;
        int centerY = getMeasuredHeight();
        radius = centerX + getChildAt(0).getMeasuredHeight() / 2;

        for (int i = 0; i < childCount; i++) {
            child = getChildAt(i);
            childWidth = child.getMeasuredWidth();
            childHeight = child.getMeasuredHeight();

            left = (int) (centerX + Math.sin(Math.toRadians(startAngle)) * radius);
            top = (int) (centerY - Math.cos(Math.toRadians(startAngle)) * radius);

            child.layout(left - childWidth / 2, top - childHeight / 2,
                        left + childWidth / 2, top + childHeight / 2);

            child.setOnClickListener(this);
            child.setRotation(startAngle);
            startAngle += ANGLE_SPACE;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
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

                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float diffX = curX - preX;
                float start = computeAngle(preX, preY);
                float end = computeAngle(curX, curY);

                if (diffX > 0) {
                    delayAngle += Math.abs(start - end);
                    perAngle += Math.abs(start - end);
                } else {
                    delayAngle -= Math.abs(end - start);
                    perAngle -= Math.abs(end - start);
                }

                requestLayout();

                preX = curX;
                preY = curY;
                break;
            case MotionEvent.ACTION_UP:
                anglePerSecond = perAngle * 1000 / (System.currentTimeMillis() - preTimes) * 2;
                int startAngle = (int) delayAngle;
                if (Math.abs(anglePerSecond) > MIN_PER_ANGLE && startAngle >= minFlingAngle && startAngle <= maxFlingAngle) {
                    scroller.fling(startAngle, 0, (int) anglePerSecond, 0, minFlingAngle, maxFlingAngle, 0, 0);
                    scroller.setFinalX(scroller.getFinalX() + computeDistanceToEndAngle(scroller.getFinalX() % ANGLE_SPACE));
                } else {
                    scroller.startScroll(startAngle, 0, computeDistanceToEndAngle(startAngle % ANGLE_SPACE), 0, 300);
                }

                if (!isCyclic) { // 当不是循环转动时，需要校正角度
                    if (scroller.getFinalX() >= maxFlingAngle) {
                        scroller.setFinalX(maxFlingAngle);
                    } else if (scroller.getFinalX() <= minFlingAngle) {
                        scroller.setFinalX(minFlingAngle);
                    }
                }
                // post一个任务，自动滚动
                post(this);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void computeFlingLimitAngle() {
        // 因为中心点在底边中点（坐标系相反），故这里计算的min和max与实际相反
        minFlingAngle = isCyclic ? Integer.MIN_VALUE : -ANGLE_SPACE * (getChildCount() - 1);
        maxFlingAngle = isCyclic ? Integer.MAX_VALUE : 0;
    }

    private float computeAngle(float xTouch, float yTouch) {
        // 圆心点在底边的中点上，根据圆心点转化为对应坐标x, y
        float x = Math.abs(xTouch - getMeasuredWidth() / 2);
        float y = Math.abs(getMeasuredHeight() - yTouch);
        return (float) (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
    }

    private int computeDistanceToEndAngle(int remainder) {
        if (Math.abs(remainder) > ANGLE_SPACE / 2) {
            if (perAngle < 0)
                return -ANGLE_SPACE - remainder;
            else
                return Math.abs(remainder) - ANGLE_SPACE;
        } else {
            return -remainder;
        }
    }

    @Override
    public void run() {
        if (scroller.computeScrollOffset()) {
            delayAngle = scroller.getCurrX();
            postDelayed(this, 16);
            requestLayout();
        }
        if (scroller.isFinished()) {
            int position = Math.abs(scroller.getCurrX() / ANGLE_SPACE);
            if (onSpinSelectedListener != null) {
                onSpinSelectedListener.onSpinSelectedListener(position);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view instanceof SMItemLayout && onMenuSelectedListener != null)
        onMenuSelectedListener.onMenuSelectedListener((SMItemLayout) view);
    }

    public int getSelectedPosition() {
        return Math.abs(scroller.getFinalX() / ANGLE_SPACE);
    }

    public void setOnSpinSelectedListener(OnSpinSelectedListener listener) {
        onSpinSelectedListener = listener;
    }

    public void setOnMenuSelectedListener(OnMenuSelectedListener listener) {
        onMenuSelectedListener = listener;
    }
}
