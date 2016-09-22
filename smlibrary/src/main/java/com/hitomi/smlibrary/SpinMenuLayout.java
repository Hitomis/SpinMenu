package com.hitomi.smlibrary;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by hitomi on 2016/9/13. <br/>
 *
 * github : https://github.com/Hitomis <br/>
 *
 * email : 196425254@qq.com
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

    /**
     * 用于自动滚动时速度加快，无其他意义
     */
    private static final float ACCELERATE_ANGLE_RATIO = 1.8f;

    /**
     * 用于加长半径，无其他意义
     */
    private static final float RADIUS_HALF_WIDTH_RATIO = 1.2f;

    /**
     * 转动角度超出可转动范围时，转动角度的迟延比率
     */
    private static final float DELAY_ANGLE_RATIO = 5.6f;

    /**
     * 点击与拖动的切换阀值
     */
    private final int touchSlopAngle = 2;

    /**
     * 最小和最大惯性滚动角度值 [-(getChildCount() - 1) * ANGLE_SPACE, 0]
     */
    private int minFlingAngle, maxFlingAngle;

    /**
     * delayAngle: 当前转动的总角度值， perAngle：每次转动的角度值
     */
    private float delayAngle, perAngle;

    /**
     * 半径：从底边到 Child 高度的中点
     */
    private float radius;

    /**
     * 每次手指按下时坐标值
     */
    private float preX, preY;

    /**
     * 每次转动的速度
     */
    private float anglePerSecond;

    /**
     * 每次手指按下的时间值
     */
    private long preTimes;

    /**
     * 是否可以循环滚动
     */
    private boolean isCyclic;

    /**
     * 是否允许可以转动菜单
     */
    private boolean enable;

    private Scroller scroller;

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

        // 宽度、高度与父容器一致
        ViewGroup parent = ((ViewGroup )getParent());
        int measureWidth = parent.getMeasuredWidth();
        int measureHeight = parent.getMeasuredHeight();
        setMeasuredDimension(measureWidth, measureHeight);

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

        delayAngle %= 360.f;
        float startAngle = delayAngle;

        View child;
        int childWidth, childHeight;
        int centerX = getMeasuredWidth() / 2;
        int centerY = getMeasuredHeight();
        radius = centerX * RADIUS_HALF_WIDTH_RATIO + getChildAt(1).getMeasuredHeight() / 2;

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
        if (!enable) return super.dispatchTouchEvent(ev);
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

                float perDiffAngle;
                if (diffX > 0) {
                    perDiffAngle = Math.abs(start - end);
                } else {
                    perDiffAngle = -Math.abs(end - start);
                }
                if (!isCyclic && (delayAngle < minFlingAngle || delayAngle > maxFlingAngle)) {
                    // 当前不是循环滚动模式，且转动的角度超出了可转角度的范围
                    perDiffAngle /= DELAY_ANGLE_RATIO;
                }
                delayAngle += perDiffAngle;
                perAngle += perDiffAngle;

                preX = curX;
                preY = curY;
                requestLayout();
                break;
            case MotionEvent.ACTION_UP:
                anglePerSecond = perAngle * 1000 / (System.currentTimeMillis() - preTimes);
                int startAngle = (int) delayAngle;
                if (Math.abs(anglePerSecond) > MIN_PER_ANGLE && startAngle >= minFlingAngle && startAngle <= maxFlingAngle) {
                    scroller.fling(startAngle, 0, (int) (anglePerSecond * ACCELERATE_ANGLE_RATIO), 0, minFlingAngle, maxFlingAngle, 0, 0);
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

    /**
     * 计算最小和最大惯性滚动角度
     */
    private void computeFlingLimitAngle() {
        // 因为中心点在底边中点（坐标系相反），故这里计算的min和max与实际相反
        minFlingAngle = isCyclic ? Integer.MIN_VALUE : -ANGLE_SPACE * (getChildCount() - 1);
        maxFlingAngle = isCyclic ? Integer.MAX_VALUE : 0;
    }

    /**
     * 依据当前触摸点坐标计算转动的角度
     * @param xTouch
     * @param yTouch
     * @return
     */
    private float computeAngle(float xTouch, float yTouch) {
        // 圆心点在底边的中点上，根据圆心点转化为对应坐标x, y
        float x = Math.abs(xTouch - getMeasuredWidth() / 2);
        float y = Math.abs(getMeasuredHeight() - yTouch);
        return (float) (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
    }

    /**
     * 计算自动滚动结束时角度值
     * @param remainder
     * @return
     */
    private int computeDistanceToEndAngle(int remainder) {
        int endAngle;
        if (remainder > 0) {
            if (Math.abs(remainder) > ANGLE_SPACE / 2) {
                if (perAngle < 0) { // 逆时针
                    endAngle = ANGLE_SPACE - remainder;
                } else { // 顺时针
                    endAngle = ANGLE_SPACE - Math.abs(remainder);
                }
            } else {
                endAngle = -remainder;
            }
        } else {
            if (Math.abs(remainder) > ANGLE_SPACE / 2) {
                if (perAngle < 0) {
                    endAngle = -ANGLE_SPACE - remainder;
                } else {
                    endAngle = Math.abs(remainder) - ANGLE_SPACE;
                }
            } else {
                endAngle = -remainder;
            }
        }
        return endAngle;
    }

    private int computeClickToEndAngle(int clickIndex, int currSelPos) {
        int endAngle;
        if (isCyclic) {
            clickIndex = clickIndex == 0 && currSelPos == getMenuItemCount() - 1 ? getMenuItemCount() : clickIndex;
            currSelPos = currSelPos == 0 && clickIndex != 1 ? getMenuItemCount() : currSelPos;
        }
        endAngle = (currSelPos - clickIndex) * ANGLE_SPACE;
        return endAngle;
    }

    @Override
    public void run() {
        if (scroller.isFinished()) {
            int position = Math.abs(scroller.getCurrX() / ANGLE_SPACE);
            if (onSpinSelectedListener != null) {
                onSpinSelectedListener.onSpinSelected(position);
            }
        }
        if (scroller.computeScrollOffset()) {
            delayAngle = scroller.getCurrX();
            postDelayed(this, 16);
            requestLayout();
        }
    }

    @Override
    public void onClick(View view) {
        int index = indexOfChild(view);
        int selPos = getSelectedPosition();
        if (Math.abs(perAngle) <= touchSlopAngle) {
            if (index != selPos) {
                // 当前点击的是左右两边的一个 Item，则把点击的 Item 滚动到选中[正中间]位置
                scroller.startScroll(-getSelectedPosition() * ANGLE_SPACE, 0, computeClickToEndAngle(index, selPos), 0, 300);
                post(this);
            } else {
                if (view instanceof SMItemLayout
                        && onMenuSelectedListener != null
                        && enable) {
                    onMenuSelectedListener.onMenuSelected((SMItemLayout) view);
                }

            }
        }
    }

    /**
     * 获取当前选中的位置
     * @return
     */
    public int getSelectedPosition() {
        if (scroller.getFinalX() > 0) {
            return (360 - scroller.getFinalX()) / ANGLE_SPACE;
        } else {
            return (Math.abs(scroller.getFinalX())) / ANGLE_SPACE;
        }
    }

    /**
     * 获取圆形转动菜单的真正半径<br/>
     * 半径是依据 child 的高度加上 SpinMenuLayout 的宽度的一半<br/>
     * 所以当没有 child 的时候，半径取值为 -1
     * @return
     */
    public int getRealRadius() {
        if (getChildCount() > 0) {
            return getMeasuredWidth() / 2 + getChildAt(0).getHeight();
        } else {
            return -1;
        }
    }

    public int getMaxMenuItemCount() {
        return 360 / ANGLE_SPACE;
    }

    public int getMenuItemCount() {
        return getChildCount();
    }

    public boolean isCyclic() {
        return isCyclic;
    }

    public void postEnable(boolean isEnable) {
        enable = isEnable;
    }

    public void setOnSpinSelectedListener(OnSpinSelectedListener listener) {
        onSpinSelectedListener = listener;
    }

    public void setOnMenuSelectedListener(OnMenuSelectedListener listener) {
        onMenuSelectedListener = listener;
    }
}
