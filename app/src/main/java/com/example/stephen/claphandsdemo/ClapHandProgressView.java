package com.example.stephen.claphandsdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;

/**
 * Stephen
 * 2019/11/29
 */
public class ClapHandProgressView extends ProgressBar {
    private final Context mContext;
    private int mMax = 1000;
    private int mCurrent = 0;
    private final int DEFAULT_FINISH_COLOR = getResources().getColor(R.color.blue);
    private final int DEFAULT_COLOR = getResources().getColor(R.color.black);

    public static final int STYLE_TICK = 1;
    public static final int STYLE_ARC = 0;
    private final int DEFAULT_LINEHEIGHT = dp2px(5);
    private final int DEFAULT_mTickWidth = dp2px(2);
    private final int DEFAULT_mRadius = dp2px(72);
    private final int DEFAULT_mUnmProgressColor = getResources().getColor(R.color.black);
    private final int DEFAULT_mProgressColor = getResources().getColor(R.color.blue);
    private final int DEFAULT_OFFSETDEGREE = 60;
    private final int DEFAULT_DENSITY = 4;
    private final int MIN_DENSITY = 2;
    private final int MAX_DENSITY = 8;
    private int mStylePogress = STYLE_TICK;
    private boolean mBgShow;
    private float mRadius;
    private int mArcbgColor;
    private int mBoardWidth;
    private int mDegree = DEFAULT_OFFSETDEGREE;
    private RectF mArcRectf;
    private Paint mLinePaint;
    private Paint mArcPaint;
    private int mUnmProgressColor;
    private int mProgressColor;
    private int mTickWidth;
    private int mTickDensity;
    private Bitmap  mCenterBitmap;
    private Canvas mCenterCanvas;
//    private OnCenterDraw mOnCenter;
    /**
     * 画笔
     */
    private Paint mPaint;
    private Bitmap mTarget;

    public ClapHandProgressView(Context context) {
        this(context, null);
    }


    public ClapHandProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClapHandProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext=context;
        init(attrs);
        initPainters();
    }


    private void init(AttributeSet attrs) {
        final TypedArray attributes = getContext().obtainStyledAttributes(
                attrs, R.styleable.ClapHandProgress);
        mBoardWidth = attributes.getDimensionPixelOffset(R.styleable.ClapHandProgress_borderWidth, DEFAULT_LINEHEIGHT);
        mUnmProgressColor = attributes.getColor(R.styleable.ClapHandProgress_unprogresColor, DEFAULT_mUnmProgressColor);
        mProgressColor = attributes.getColor(R.styleable.ClapHandProgress_progressColor, DEFAULT_mProgressColor);
        mTickWidth = attributes.getDimensionPixelOffset(R.styleable.ClapHandProgress_tickWidth,DEFAULT_mTickWidth);
        mTickDensity = attributes.getInt(R.styleable.ClapHandProgress_tickDensity,DEFAULT_DENSITY);
        mRadius = attributes.getDimensionPixelOffset(R.styleable.ClapHandProgress_radius,DEFAULT_mRadius);
        mArcbgColor = attributes.getColor(R.styleable.ClapHandProgress_arcbgColor,DEFAULT_mUnmProgressColor);
        mTickDensity = Math.max(Math.min(mTickDensity,MAX_DENSITY),MIN_DENSITY);
        mBgShow = attributes.getBoolean(R.styleable.ClapHandProgress_bgShow,false);
        mDegree = attributes.getInt(R.styleable.ClapHandProgress_degree,DEFAULT_OFFSETDEGREE);
        mStylePogress = attributes.getInt(R.styleable.ClapHandProgress_progressStyle,STYLE_TICK);
        boolean capRount = attributes.getBoolean(R.styleable.ClapHandProgress_arcCapRound,false);
        mArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArcPaint.setColor(mArcbgColor);
        if(capRount)
            mArcPaint.setStrokeCap(Paint.Cap.ROUND);
        mArcPaint.setStrokeWidth(mBoardWidth);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStrokeWidth(mTickWidth);
    }

    private void initPainters() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
        mPaint.setAntiAlias(true);//防抖动
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if(widthMode!=MeasureSpec.EXACTLY){
            int widthSize = (int) (mRadius*2+mBoardWidth*2);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize,MeasureSpec.EXACTLY);
        }
        if(heightMode != MeasureSpec.EXACTLY){
            int heightSize = (int) (mRadius*2+mBoardWidth*2);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize,MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.save();
        float roate = getProgress() * 1.0f / getMax();
        float x = mArcRectf.right / 2 + mBoardWidth / 2;
        float y = mArcRectf.right / 2 + mBoardWidth / 2;
//        if (mOnCenter != null) {
            if(mCenterCanvas == null){
                mCenterBitmap = Bitmap.createBitmap((int)mRadius*2,(int)mRadius*2, Bitmap.Config.ARGB_8888);
                mCenterCanvas = new Canvas(mCenterBitmap);
            }
            mCenterCanvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
            draw(mCenterCanvas, mArcRectf, x, y,mBoardWidth,getProgress());
            canvas.drawBitmap(mCenterBitmap, 0, 0, null);
//        }
        int angle = mDegree/2;
        int count = (360 - mDegree )/mTickDensity;
        int target = (int) (roate * count);
        if(mStylePogress == STYLE_ARC){
            float targetmDegree = (360-mDegree)*roate;
            //绘制完成部分
            mArcPaint.setColor(mProgressColor);
            canvas.drawArc(mArcRectf,-90+angle,targetmDegree,false,mArcPaint);
            //绘制未完成部分
            mArcPaint.setColor(mUnmProgressColor);
            canvas.drawArc(mArcRectf,-90+angle+targetmDegree,360-mDegree-targetmDegree,false,mArcPaint);
        }else{
            if(mBgShow)
                canvas.drawArc(mArcRectf,90+angle,360-mDegree,false,mArcPaint);
            canvas.rotate(180+angle,x,y);
            for(int i = 0 ; i<count;i++){
                if(i<target){
                    mLinePaint.setColor(mProgressColor);
                }else{
                    mLinePaint.setColor(mUnmProgressColor);
                }
                canvas.drawLine(x,mBoardWidth+mBoardWidth/2,x,mBoardWidth-mBoardWidth/2,mLinePaint);
                canvas.rotate(mTickDensity,x,y);
            }
        }
        canvas.restore();
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mArcRectf = new RectF(mBoardWidth,
                mBoardWidth,
                mRadius*2 - mBoardWidth,
                mRadius*2 - mBoardWidth);
        Log.e("DEMO","right == "+mArcRectf.right+"   mRadius == "+mRadius*2);
    }
    /**
     * dp 2 px
     * @param dpVal
     */
    protected int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }

//    public class   OnCenterDraw {
        /**
         *
         * @param canvas
         * @param rectF  圆弧的Rect
         * @param x      圆弧的中心x
         * @param y      圆弧的中心y
         * @param storkeWidth   圆弧的边框宽度
         * @param progress      当前进度
         */
        public  void draw(Canvas canvas, RectF rectF, float x, float y,float storkeWidth,int progress){
            Bitmap mBmp = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.clapping_hands_72);
//            mPaint.setShader(new BitmapShader(mBmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

            if(mTarget == null){
                mTarget = Bitmap.createScaledBitmap(mBmp, (int) (rectF.right - storkeWidth * 10), (int) (rectF.right - storkeWidth * 10), false);
            }
            double v = mTarget.getWidth();
            Bitmap target = Bitmap.createBitmap(mTarget, 0, 0, mTarget.getWidth(), mTarget.getHeight());
            float sx = x - target.getWidth() / 2;
            float sy = y - target.getHeight() / 2;
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//            canvas.drawCircle(x, y, (rectF.right - storkeWidth * 2) / 2, paint);
            canvas.drawCircle(x, y, (rectF.right - storkeWidth * 2) / 2, mPaint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            canvas.drawBitmap(target, sx, sy, paint);

        };
//    }
}
