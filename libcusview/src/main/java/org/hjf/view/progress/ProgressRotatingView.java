package org.hjf.view.progress;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

public class ProgressRotatingView extends View {
	
	private static final int LINE_NUMS = 12;
	private Paint mPaint = new Paint();
	private PointF mCenterPoint = new PointF();
	private PointF mStartPoint;
	private PointF mEndPoint;
	private float mInnerRadius;
	private float mOuterRadius;
	private double mArc;
	private float mStrokeWidth;
	private int mCurrOffset;
	private int MSG_UPDATE_UI = 0;
	private Handler mHandler = new Handler(Looper.getMainLooper()){
		@Override
		public void handleMessage(android.os.Message msg) {
			invalidate();
		};
	};

	public ProgressRotatingView(Context context) {
		this(context, null);
	}
	
	public ProgressRotatingView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public ProgressRotatingView(Context context, AttributeSet attrs,
                                int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setColor(Color.WHITE);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int min = Math.min(getMeasuredWidth(), getMeasuredHeight());
		mStrokeWidth = min / 3.7f / 3.2f;
		mPaint.setStrokeWidth(mStrokeWidth);
		mCenterPoint.x = getMeasuredWidth() / 2f;
		mCenterPoint.y = getMeasuredHeight() / 2f;
		mOuterRadius = (min - mStrokeWidth)/ 2f;
		mInnerRadius = mOuterRadius / 1.7f;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		for (int i = 0; i < LINE_NUMS; i++){
			mArc = i * 30;
			mPaint.setAlpha((int)( (1f-((i+1+mCurrOffset)%(LINE_NUMS+1))/(float)LINE_NUMS) *255));
			mStartPoint = getPointByArc(mCenterPoint, mInnerRadius, mArc);
			mEndPoint = getPointByArc(mCenterPoint, mOuterRadius, mArc);
			canvas.drawLine(mStartPoint.x, mStartPoint.y, mEndPoint.x, mEndPoint.y, mPaint);
		}
		mCurrOffset++;
		mCurrOffset = mCurrOffset > LINE_NUMS ? 0 : mCurrOffset;
		mHandler.removeMessages(MSG_UPDATE_UI);
//		if (isAttachedToWindow()){
			mHandler.sendEmptyMessageDelayed(MSG_UPDATE_UI, 50L);
//		}
	}
	
	/**
	 * 获取圆上的点的坐标
	 * @param center 圆心坐标
	 * @param radius 圆半径
	 * @param arc 角度
	 */
	private PointF getPointByArc(PointF center, float radius, double arc){
		double r = 2*Math.PI/360*arc;
		float dx = (float) (radius * Math.cos(r));
		float dy = (float) (radius * Math.sin(r));
		return new PointF(center.x + dx, center.y - dy);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mHandler.removeMessages(MSG_UPDATE_UI);
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mHandler.removeMessages(MSG_UPDATE_UI);
		mHandler.sendEmptyMessageDelayed(MSG_UPDATE_UI, 50L);
	}

}
