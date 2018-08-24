package org.hjf.view.refresh;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Scroller;

import org.hjf.log.LogUtil;
import org.hjf.view.R;


/**
 * arrow bitmap 变小 下移 转动
 */
public class DripView extends View {

    private static final int BOTTOM_CIRCLE_SCROLL_BACK_DURATION = 1000;
    private static final int TOP_CIRCLE_MAX_RADIUS = 60;
    private static final int BOTTOM_CIRCLE_MAX_RADIUS = 8;
    private final static int STROKE_WIDTH = 1;


    private int twoCircleCenterYDistanceMax = 180;
    private Circle topCircle;
    private Circle bottomCircle;

    private Scroller mScrollerY;
    private Paint mPaint;
    private Path mPath;

    // arrow bitmap
    private Bitmap arrowBitmap;
    private Matrix arrowBpMatrix = new Matrix();
    private int arrowRotateNum = 0;
    private ValueAnimator arrowRotateAnimator;
    private ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            arrowRotateNum = (int) animation.getAnimatedValue();
            postInvalidate();
        }
    };

    public DripView(Context context) {
        this(context, null);
    }

    public DripView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DripView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        initFromAttributes(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int modeW = MeasureSpec.getMode(widthMeasureSpec);
        int modeH = MeasureSpec.getMode(heightMeasureSpec);
        int viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        int viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        // width = (max circle radius + stroke_width) * 2
        if (modeW != MeasureSpec.EXACTLY) {
            viewWidth = (Math.max(topCircle.getMaxRadius(), bottomCircle.getMaxRadius()) + STROKE_WIDTH) * 2;
        }

        // height = top circle radius + bottom circle radius + tow circle center distance
        if (modeH != MeasureSpec.EXACTLY) {
            viewHeight = topCircle.getMaxRadius() + bottomCircle.getMaxRadius() + twoCircleCenterYDistanceMax + STROKE_WIDTH * 2;
        }

        // padding
        viewWidth = viewWidth + getPaddingStart() + getPaddingEnd();
        viewHeight = viewHeight + getPaddingTop() + getPaddingBottom();

        //这里将宽度和高度与Google为我们设定的建议最低宽高对比，确保我们要求的尺寸不低于建议的最低宽高。
        viewWidth = Math.max(viewWidth, getSuggestedMinimumWidth());
        viewHeight = Math.max(viewHeight, getSuggestedMinimumHeight());

        LogUtil.d("size: Drip View request Width{0} Height{1}", viewWidth, viewHeight);
        // 请求宽高
        setMeasuredDimension(resolveSizeAndState(viewWidth, widthMeasureSpec, 0),
                resolveSizeAndState(viewHeight, heightMeasureSpec, 0));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        LogUtil.d("size: Drip View result W{0} H{1} twoCircleCenterYDistanceMax={2}", w, h, twoCircleCenterYDistanceMax);

        int maxRadius = Math.max(topCircle.getRadius(), bottomCircle.getRadius());

        // display form bottom
        // so. bottom circle always close to DripView's bottom, centerY changed follow radius change.
        bottomCircle.setCenterX(getPaddingStart() + STROKE_WIDTH + maxRadius);
        bottomCircle.setCenterY(h - getPaddingBottom() - STROKE_WIDTH - bottomCircle.getRadius());
        LogUtil.d("size: bottom circle CenterXY=[{0}, {1}] radius={2}", bottomCircle.getCenterX(), bottomCircle.getCenterY(), bottomCircle.getRadius());

        topCircle.setCenterX(bottomCircle.getCenterX());
        topCircle.setCenterY(bottomCircle.getCenterY());
        LogUtil.d("size: top circle CenterXY=[{0}, {1}] radius={2}", topCircle.getCenterX(), topCircle.getCenterY(), topCircle.getRadius());
    }

    @Override
    protected void onDraw(Canvas canvas) {

        makeBezierPath();
        canvas.drawPath(mPath, mPaint);

        // draw top circle
        LogUtil.d("draw top circle CenterXY=[{0}, {1}] radius={2}", topCircle.getCenterX(), topCircle.getCenterY(), topCircle.getRadius());
        canvas.drawCircle(topCircle.getCenterX(), topCircle.getCenterY(), topCircle.getRadius(), mPaint);

        // draw bottom circle
        LogUtil.d("draw bottom circle CenterXY=[{0}, {1}] radius={2}", bottomCircle.getCenterX(), bottomCircle.getCenterY(), bottomCircle.getRadius());
        canvas.drawCircle(bottomCircle.getCenterX(), bottomCircle.getCenterY(), bottomCircle.getRadius(), mPaint);

        // draw arrow bitmap
        int dx = topCircle.getCenterX() - arrowBitmap.getWidth() / 2;
        int dy = topCircle.getCenterY() - arrowBitmap.getHeight() / 2;
        LogUtil.d("draw arrow bitmap translate [dx,dy] = [{0}, {1}]", dx, dy);
        arrowBpMatrix.reset();
        arrowBpMatrix.postTranslate(dx, dy);
        // rotating
        if (arrowRotateAnimator != null && arrowRotateAnimator.isRunning()) {
            arrowBpMatrix.postRotate(arrowRotateNum, topCircle.getCenterX(), topCircle.getCenterY());
            canvas.drawBitmap(arrowBitmap, arrowBpMatrix, mPaint);
        }
        // no rotating
        else {
            canvas.drawBitmap(arrowBitmap, arrowBpMatrix, mPaint);
        }
    }


    /**
     * update view with animal
     * for {@link #updateView4ReadyLoad()}
     */
    @Override
    public void computeScroll() {
        if (mScrollerY.computeScrollOffset()) {
            LogUtil.d("scroller currY={0}", mScrollerY.getCurrY());
            bottomCircle.setCenterY(mScrollerY.getCurrY());
            postInvalidate();
        }
    }

    /**
     * update drip view for ready load.
     * top circle keep location.
     * bottom circle close to top circle.
     */
    void updateView4ReadyLoad() {
        int dy = -(bottomCircle.getCenterY() - topCircle.getCenterY());
        LogUtil.d("scroller from[{0},{1}  dx,dy[{2},{3}]]", 0, bottomCircle.getCenterY(), 0, dy);
        mScrollerY.startScroll(0, bottomCircle.getCenterY(), 0, dy, BOTTOM_CIRCLE_SCROLL_BACK_DURATION);
        postInvalidate();
    }

    /**
     * update drip view by scroll percent
     *
     * @param percent between[0,1]
     */
    void updateViewByScrollPercent(float percent) {

        percent = Math.min(1, Math.max(0, percent));
        LogUtil.d("water percent is {0}", percent);

        //set top circle radius
        int topRadius = (int) (topCircle.getMaxRadius() - 0.2f * percent * topCircle.getMaxRadius());
        LogUtil.d("water percent topRadius {0}", topRadius);
        topCircle.setRadius(topRadius);

        //set bottom circle radius and centerY
        int bottomRadius = (int) ((bottomCircle.getMaxRadius() - topCircle.getMaxRadius()) * percent + topCircle.getMaxRadius());
        bottomCircle.setRadius(bottomRadius);
        bottomCircle.setCenterY(getMeasuredHeight() - getPaddingBottom() - STROKE_WIDTH - bottomCircle.getRadius());

        // bottom circle is reference point
        // set top circle distanceY
        int bottomCircleOffset = (int) (percent * twoCircleCenterYDistanceMax);
        topCircle.setCenterY(bottomCircle.getCenterY() - bottomCircleOffset);

        postInvalidate();
    }

    /**
     * start arrow bitmap rotate
     */
    void openArrowRotate() {
        arrowRotateAnimator.start();
        arrowRotateAnimator.addUpdateListener(animatorUpdateListener);
    }

    /**
     * start arrow bitmap rotate
     */
    void closeArrowRotate() {
        arrowRotateAnimator.end();
        arrowRotateAnimator.removeUpdateListener(animatorUpdateListener);
    }


    private void makeBezierPath() {
        mPath.reset();
        // 获得两个圆切线与圆心连线的夹角
        double angle = Math.asin((topCircle.getRadius() - bottomCircle.getRadius()) / Math.max(1, bottomCircle.getCenterY() - topCircle.getCenterY()));
        float top_x1 = (float) (topCircle.getCenterX() - topCircle.getRadius() * Math.cos(angle));
        float top_y1 = (float) (topCircle.getCenterY() + topCircle.getRadius() * Math.sin(angle));

        float top_x2 = (float) (topCircle.getCenterX() + topCircle.getRadius() * Math.cos(angle));
        float top_y2 = top_y1;

        float bottom_x1 = (float) (bottomCircle.getCenterX() - bottomCircle.getRadius() * Math.cos(angle));
        float bottom_y1 = (float) (bottomCircle.getCenterY() + bottomCircle.getRadius() * Math.sin(angle));

        float bottom_x2 = (float) (bottomCircle.getCenterX() + bottomCircle.getRadius() * Math.cos(angle));
        float bottom_y2 = bottom_y1;

        mPath.moveTo(topCircle.getCenterX(), topCircle.getCenterY());

        mPath.lineTo(top_x1, top_y1);

        mPath.quadTo((bottomCircle.getCenterX() - bottomCircle.getRadius()),
                (bottomCircle.getCenterY() + topCircle.getCenterY()) / 2,
                bottom_x1,
                bottom_y1);
        mPath.lineTo(bottom_x2, bottom_y2);

        mPath.quadTo((bottomCircle.getCenterX() + bottomCircle.getRadius()),
                (bottomCircle.getCenterY() + top_y2) / 2,
                top_x2,
                top_y2);
        mPath.close();
    }

    private void init(Context context) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(STROKE_WIDTH);

        mPath = new Path();
        mScrollerY = new Scroller(context);

        // arrow bitmap animator
        Drawable drawable = getResources().getDrawable(R.mipmap.refresh_arrow);
        arrowBitmap = drawableToBitmap(drawable);
        arrowRotateAnimator = ValueAnimator.ofInt(0, 360);
        arrowRotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        arrowRotateAnimator.setDuration(500);

        topCircle = new Circle();
        topCircle.setMaxRadius(TOP_CIRCLE_MAX_RADIUS);
        topCircle.setRadius(TOP_CIRCLE_MAX_RADIUS);
        bottomCircle = new Circle();
        bottomCircle.setMaxRadius(BOTTOM_CIRCLE_MAX_RADIUS);
        bottomCircle.setRadius(BOTTOM_CIRCLE_MAX_RADIUS);
    }


    private void initFromAttributes(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DripView, defStyleAttr, 0);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            // color
            if (attr == R.styleable.DripView_drip_color) {
                int dripColor = a.getColor(attr, Color.RED);
                mPaint.setColor(dripColor);
            }
            // top circle
            else if (attr == R.styleable.DripView_top_circle_radius) {
                topCircle.setMaxRadius(a.getDimensionPixelSize(attr, TOP_CIRCLE_MAX_RADIUS));
                topCircle.setRadius(topCircle.getMaxRadius());
            }
            // bottom circle
            else if (attr == R.styleable.DripView_bottom_circle_radius) {
                bottomCircle.setMaxRadius(a.getDimensionPixelSize(attr, BOTTOM_CIRCLE_MAX_RADIUS));
                bottomCircle.setRadius(bottomCircle.getMaxRadius());
            }
            // circle center distanceY
            else if (attr == R.styleable.DripView_circle_center_max_distance_y) {
                twoCircleCenterYDistanceMax = a.getDimensionPixelSize(attr, twoCircleCenterYDistanceMax);
            }
        }
        a.recycle();
    }


    private static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }


    public static final class Circle {

        private int maxRadius;
        private int centerX;
        private int centerY;
        private int radius;

        int getCenterX() {
            return centerX;
        }

        void setCenterX(int centerX) {
            this.centerX = centerX;
        }

        int getCenterY() {
            return centerY;
        }

        void setCenterY(int centerY) {
            this.centerY = centerY;
        }

        int getRadius() {
            return radius;
        }

        void setRadius(int radius) {
            this.radius = radius;
        }

        int getMaxRadius() {
            return maxRadius;
        }

        void setMaxRadius(int maxRadius) {
            this.maxRadius = maxRadius;
        }
    }
}
