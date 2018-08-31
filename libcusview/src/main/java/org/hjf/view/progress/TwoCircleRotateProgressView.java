package org.hjf.view.progress;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import org.hjf.log.LogUtil;
import org.hjf.view.R;

/**
 * default draw at view center
 */
public class TwoCircleRotateProgressView extends View {

    private static final int DEFAULT_ANIMATOR_DURATION = 1000;
    private static final int DEFAULT_MAX_RADIUS = 15;
    private static final int DEFAULT_MIN_RADIUS = 5;
    private static final int DEFAULT_DIRECTION_CLOCKWISE = 1;
    private static final int DEFAULT_DIRECTION_ANTI_CLOCKWISE = 2;

    private int rotateDirection = DEFAULT_DIRECTION_CLOCKWISE;
    private PointF centerPointAtCircle = new PointF();
    private float twoCircleDistanceX;
    private float moveableDistanceX4CircleCenter;

    private Circle mCircle1;
    private Circle mCircle2;

    private Paint mPaint;
    private AnimatorSet animatorSet;

    public TwoCircleRotateProgressView(Context context) {
        this(context, null);
    }

    public TwoCircleRotateProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TwoCircleRotateProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFromAttributes(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int modeW = MeasureSpec.getMode(widthMeasureSpec);
        int modeH = MeasureSpec.getMode(heightMeasureSpec);
        int sizeW = MeasureSpec.getSize(widthMeasureSpec);
        int sizeH = MeasureSpec.getSize(heightMeasureSpec);

        if (modeW != MeasureSpec.EXACTLY) {
            sizeW = (int) ((mCircle1.middRadius + mCircle2.middRadius) * 2 + twoCircleDistanceX);
            // padding
            sizeW = sizeW + getPaddingStart() + getPaddingEnd();
        }

        if (modeH != MeasureSpec.EXACTLY) {
            sizeH = (int) (Math.max(mCircle1.maxRadius, mCircle2.maxRadius) * 2);
            // padding
            sizeH = sizeH + getPaddingTop() + getPaddingBottom();
        }


        //这里将宽度和高度与Google为我们设定的建议最低宽高对比，确保我们要求的尺寸不低于建议的最低宽高。
        sizeW = Math.max(sizeW, getSuggestedMinimumWidth());
        sizeH = Math.max(sizeH, getSuggestedMinimumHeight());

        LogUtil.d("size: TwoCircleRotateProgress View request Width{0} Height{1}", sizeW, sizeH);
        // 请求宽高
        setMeasuredDimension(resolveSizeAndState(sizeW, widthMeasureSpec, 0),
                resolveSizeAndState(sizeH, heightMeasureSpec, 0));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LogUtil.d("size: TwoCircleRotateProgress Padding[l,t,r,b]=[{0}, {1}, {2}, {3}]",
                getPaddingStart(), getPaddingTop(), getPaddingEnd(), getPaddingBottom());
        LogUtil.d("size: TwoCircleRotateProgress View Circle minRadius={0}, middleRadius={1}, maxRadius={2}",
                mCircle1.minRadius, mCircle1.middRadius, mCircle1.maxRadius);

        centerPointAtCircle.x = getPaddingStart() + (w - getPaddingStart()) * 0.5f;
        centerPointAtCircle.y = getPaddingTop() + (h - getPaddingTop()) * 0.5f;
        LogUtil.d("size: TwoCircleRotateProgress View CenterPointAtCircle[{0}, {1}]",
                centerPointAtCircle.x, centerPointAtCircle.y);

        moveableDistanceX4CircleCenter = twoCircleDistanceX * 0.5f + mCircle1.middRadius;
        LogUtil.d("size: TwoCircleRotateProgress View centerPointAtCircle = {0}",
                moveableDistanceX4CircleCenter);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // the big circle cover the small circle, so draw big circle after draw the small circle.
        if (mCircle1.radius > mCircle2.radius) {
            mPaint.setColor(mCircle2.color);
            canvas.drawCircle(mCircle2.centerX, centerPointAtCircle.y, mCircle2.radius, mPaint);

            mPaint.setColor(mCircle1.color);
            canvas.drawCircle(mCircle1.centerX, centerPointAtCircle.y, mCircle1.radius, mPaint);
        } else {
            mPaint.setColor(mCircle1.color);
            canvas.drawCircle(mCircle1.centerX, centerPointAtCircle.y, mCircle1.radius, mPaint);

            mPaint.setColor(mCircle2.color);
            canvas.drawCircle(mCircle2.centerX, centerPointAtCircle.y, mCircle2.radius, mPaint);
        }
    }

    @Override
    public void setVisibility(int v) {
        if (getVisibility() != v) {
            super.setVisibility(v);
            if (v == GONE || v == INVISIBLE) {
                stopAnimator();
            } else {
                startAnimator();
            }
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int v) {
        super.onVisibilityChanged(changedView, v);
        if (v == GONE || v == INVISIBLE) {
            stopAnimator();
        } else {
            startAnimator();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimator();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimator();
    }

    public void startAnimator() {
        if (getVisibility() != VISIBLE) return;

        if (animatorSet.isRunning()) return;

        if (animatorSet != null) {
            animatorSet.start();
        }
    }

    public void stopAnimator() {
        if (animatorSet != null) {
            animatorSet.end();
        }
    }


    private void initFromAttributes(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        @ColorInt int color1 = getResources().getColor(R.color.grass_green);
        @ColorInt int color2 = getResources().getColor(R.color.pink);
        float maxRadius = DEFAULT_MAX_RADIUS;
        float minRadius = DEFAULT_MIN_RADIUS;
        int rotateDuration = DEFAULT_ANIMATOR_DURATION;
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TwoCircleRotateProgressView, defStyleAttr, 0);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            // one circle color
            if (attr == R.styleable.TwoCircleRotateProgressView_one_circle_color) {
                color1 = a.getColor(attr, color1);
            }
            // two circle color
            else if (attr == R.styleable.TwoCircleRotateProgressView_two_circle_Color) {
                color2 = a.getColor(attr, color2);
            }
            // max radius
            else if (attr == R.styleable.TwoCircleRotateProgressView_max_radius) {
                maxRadius = a.getDimension(attr, DEFAULT_MAX_RADIUS);
            }
            // min radius
            else if (attr == R.styleable.TwoCircleRotateProgressView_min_radius) {
                minRadius = a.getDimension(attr, DEFAULT_MIN_RADIUS);
            }
            // distance
            else if (attr == R.styleable.TwoCircleRotateProgressView_circle_distanceX) {
                twoCircleDistanceX = a.getDimension(attr, 0);
            }
            // rotate rotateDuration time
            else if (attr == R.styleable.TwoCircleRotateProgressView_duration) {
                rotateDuration = a.getInteger(attr, DEFAULT_ANIMATOR_DURATION);
            }
            // rotate rotateDirection
            else if (attr == R.styleable.TwoCircleRotateProgressView_direction) {
                rotateDirection = a.getInteger(attr, DEFAULT_DIRECTION_CLOCKWISE);
            }
        }
        a.recycle();
        init(color1, color2, minRadius, maxRadius, rotateDuration);
        initAnimator();
    }

    public void init(@ColorInt int oneCircleColor, @ColorInt int twoCircleColor, float minRadius, float maxRadius, int rotateDuration) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        animatorSet = new AnimatorSet();
        animatorSet.setDuration(rotateDuration);

        mCircle1 = new Circle(minRadius, maxRadius);
        mCircle1.setColor(oneCircleColor);
        mCircle2 = new Circle(minRadius, maxRadius);
        mCircle2.setColor(twoCircleColor);
    }

    private void initAnimator() {
        ObjectAnimator oneScaleAnimator;
        ObjectAnimator twoScaleAnimator;
        switch (rotateDirection) {
            // anti-clockwise rotate
            case DEFAULT_DIRECTION_ANTI_CLOCKWISE:
                // circle have max radius in middle of x.
                // circle radius change process： middle -> max -> middle -> min -> ...
                oneScaleAnimator = ObjectAnimator.ofFloat(mCircle1, "radius",
                        mCircle1.middRadius, mCircle1.maxRadius, mCircle1.middRadius, mCircle1.minRadius, mCircle1.middRadius);
                twoScaleAnimator = ObjectAnimator.ofFloat(mCircle2, "radius",
                        mCircle2.middRadius, mCircle2.minRadius, mCircle2.middRadius, mCircle2.maxRadius, mCircle2.middRadius);
                break;
            // clockwise rotate
            default:
            case DEFAULT_DIRECTION_CLOCKWISE:
                // circle radius change process： middle -> min -> middle -> max -> ...
                oneScaleAnimator = ObjectAnimator.ofFloat(mCircle1, "radius",
                        mCircle1.middRadius, mCircle1.minRadius, mCircle1.middRadius, mCircle1.maxRadius, mCircle1.middRadius);
                twoScaleAnimator = ObjectAnimator.ofFloat(mCircle2, "radius",
                        mCircle2.middRadius, mCircle2.maxRadius, mCircle2.middRadius, mCircle2.minRadius, mCircle2.middRadius);
                break;
        }
        // circle1 horizontal move process： left -> middle -> right
        ValueAnimator oneCenterXAnimator = ValueAnimator.ofFloat(-1f, 0f, 1f, 0f, -1f);
        oneCenterXAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (Float) valueAnimator.getAnimatedValue();
                float x = centerPointAtCircle.x + value * moveableDistanceX4CircleCenter;
                mCircle1.setCenterX(x);
                //只在这边重画
                invalidate();
            }
        });
        // circle2 horizontal move process： right -> middle -> left
        ValueAnimator twoCenterXAnimator = ValueAnimator.ofFloat(1f, 0f, -1f, 0f, 1f);
        twoCenterXAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (Float) valueAnimator.getAnimatedValue();
                float x = centerPointAtCircle.x + value * moveableDistanceX4CircleCenter;
                mCircle2.setCenterX(x);
            }
        });
        oneScaleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        twoScaleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        oneCenterXAnimator.setRepeatCount(ValueAnimator.INFINITE);
        twoCenterXAnimator.setRepeatCount(ValueAnimator.INFINITE);
        animatorSet.playTogether(oneScaleAnimator, twoScaleAnimator, oneCenterXAnimator, twoCenterXAnimator);
        animatorSet.setInterpolator(new DecelerateInterpolator());
    }


    public static class Circle {

        private float minRadius, middRadius, maxRadius;
        private float radius;
        private float centerX;
        @ColorInt
        private int color;

        Circle(float minRadius, float maxRadius) {
            this.setRadius(minRadius, maxRadius);
        }

        @Deprecated
        public final void setRadius(float radius) {
            this.radius = radius;
        }

        public void setRadius(float minRadius, float maxRadius) {
            this.minRadius = minRadius;
            this.middRadius = (minRadius + maxRadius) * 0.5f;
            this.maxRadius = maxRadius;
        }

        void setCenterX(float centerX) {
            this.centerX = centerX;
        }

        public void setColor(@ColorInt int color) {
            this.color = color;
        }
    }
}
