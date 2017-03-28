package com.ys.cropimageview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ImL1s on 2017/3/28.
 * <p>
 * DESC:
 */

public class ScaleImage extends android.support.v7.widget.AppCompatImageView
{
    // 初始狀態的Matrix
    private Matrix mMatrix       = new Matrix();
    // 進行變動狀況下的Matrix
    private Matrix mChangeMatrix = new Matrix();
    // 動畫用Matrix
    private Matrix mScaleMatrix  = new Matrix();
    // 圖片的Bitmap
    private Bitmap mBitmap       = null;
    // 手機畫面尺寸資訊
    private DisplayMetrics mDisplayMetrics;
    // 設定縮放最小比例
    private              float mMinScale              = .5f;
    // 設定縮放最大比例
    private              float mMaxScale              = 1.3f;
    // 圖片狀態 - 初始狀態
    private static final int   STATE_NONE             = 0;
    // 圖片狀態 - 拖動狀態
    private static final int   STATE_DRAG             = 1;
    // 圖片狀態 - 縮放狀態
    private static final int   STATE_ZOOM             = 2;
    // 圖片狀態 - 動畫狀態
    private static final int   STATE_ANIMATION_TO_MIN = 4;

    private static final int    STATE_ANIMATION_TO_MAX = 8;
    // 當下的狀態
    private              int    mState                 = STATE_NONE;
    // 第一點按下的座標
    private              PointF mFirstPointF           = new PointF();
    // 第二點按下的座標
    private              PointF mSecondPointF          = new PointF();
    // 抬起時當前圖片中間
    private              PointF mLastUpPointF          = new PointF();
    // 兩點距離
    private              float  mDistance              = 1f;
    // 圖片中心座標
    private float mCenterX, mCenterY;
    // 圖片縮放用
    private float mScaleX, mScaleY;

    // 縮放速度
    private float mScaleMinSpeed = 1.8f;

    private float mScaleMaxSpeed = 3.8f;

    // 紀錄touch up之前圖片的scale值,讓動畫從該scale值開始縮放
    private float mCurrentToMinScale;

    private float mCurrentToMaxScale;

    @Override
    protected void onDraw(Canvas canvas)
    {
        if (mState == STATE_ANIMATION_TO_MIN)
        {
            //            mScaleMatrix.set(mMatrix);
            Log.d("debug", "State_animation");
            mMatrix.setScale(mCurrentToMinScale + mScaleX, mCurrentToMinScale + mScaleY,
                    mLastUpPointF.x, mLastUpPointF.y);

            mScaleX += 0.01f * mScaleMinSpeed;
            mScaleY += 0.01f * mScaleMinSpeed;
            if (mScaleX + mCurrentToMinScale > mMinScale &&
                mScaleY + mCurrentToMinScale > mMinScale)
            {
                mScaleX = 0;
                mScaleY = 0;
                mMatrix.setScale(mMinScale, mMinScale, mLastUpPointF.x, mLastUpPointF.y);
                mState = STATE_NONE;
            }
            setImageMatrix(mMatrix);
        }
        else if (mState == STATE_ANIMATION_TO_MAX)
        {
            //            mScaleMatrix.set(mMatrix);
            Log.d("debug", "State_animation");
            mMatrix.setScale(mCurrentToMaxScale - mScaleX, mCurrentToMaxScale - mScaleY,
                    mLastUpPointF.x, mLastUpPointF.y);

            mScaleX += 0.01f * mScaleMaxSpeed;
            mScaleY += 0.01f * mScaleMaxSpeed;
            if (mCurrentToMaxScale - mScaleX < mMaxScale &&
                mCurrentToMaxScale - mScaleY < mMaxScale)
            {
                mScaleX = 0;
                mScaleY = 0;
                mMatrix.setScale(mMaxScale, mMaxScale, mLastUpPointF.x, mLastUpPointF.y);
                mState = STATE_NONE;
            }
            setImageMatrix(mMatrix);
        }
        super.onDraw(canvas);
    }

    //ScaleImage類別，xml呼叫運用
    public ScaleImage(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        //取得圖片Bitmap
        BitmapDrawable mBitmapDrawable = (BitmapDrawable) this.getDrawable();
        if (mBitmapDrawable != null)
        {
            mBitmap = mBitmapDrawable.getBitmap();
            build_image();
        }
    }


    //兩點距離
    private float Spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    //兩點中心
    private void MidPoint(PointF point, MotionEvent event)
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    //圖片縮放設定
    public void build_image()
    {
        //取得Context
        Context mContext = getContext();
        //取得手機畫面尺寸資訊
        mDisplayMetrics = mContext.getResources().getDisplayMetrics();

        //設置縮放的型態
        this.setScaleType(ScaleType.MATRIX);
        //將Bitmap帶入
        this.setImageBitmap(mBitmap);

        //將圖片放置畫面中央
        mCenterX = (float) ((mDisplayMetrics.widthPixels / 2) - (mBitmap.getWidth() / 2));
        mCenterY = (float) ((mDisplayMetrics.heightPixels / 3) - (mBitmap.getHeight() / 2));
        mMatrix.postTranslate(mCenterX, mCenterY);

        //將mMatrix帶入
        this.setImageMatrix(mMatrix);

        //設置Touch觸發的Listener動作
        this.setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                //                Log.d("debug", "onTouch state:" + mState);
                if (mState == STATE_ANIMATION_TO_MIN || mState == STATE_ANIMATION_TO_MAX)
                    return false;

                //多點觸碰偵測
                switch (event.getAction() & MotionEvent.ACTION_MASK)
                {
                    case MotionEvent.ACTION_CANCEL:
                        checkScaleLimit();
                        break;


                    //第一點按下進入
                    case MotionEvent.ACTION_DOWN:
                        mChangeMatrix.set(mMatrix);
                        mFirstPointF.set(event.getX(), event.getY());
                        mState = STATE_DRAG;
                        break;

                    //第二點按下進入
                    case MotionEvent.ACTION_POINTER_DOWN:
                        mDistance = Spacing(event);
                        //只要兩點距離大於10就判定為多點觸碰
                        if (Spacing(event) > 10f)
                        {
                            mChangeMatrix.set(mMatrix);
                            MidPoint(mSecondPointF, event);
                            mState = STATE_ZOOM;
                        }
                        break;

                    //離開觸碰
                    case MotionEvent.ACTION_UP:
                        checkScaleLimit();
                        break;

                    //離開觸碰，狀態恢復
                    case MotionEvent.ACTION_POINTER_UP:

                        checkScaleLimit();
                        break;

                    //滑動過程進入
                    case MotionEvent.ACTION_MOVE:
                        if (mState == STATE_DRAG)
                        {
                            mMatrix.set(mChangeMatrix);
                            mMatrix.postTranslate(event.getX() - mFirstPointF.x,
                                    event.getY() - mFirstPointF.y);
                        }
                        else if (mState == STATE_ZOOM)
                        {
                            float NewDistance = Spacing(event);
                            if (NewDistance > 10f)
                            {
                                mMatrix.set(mChangeMatrix);
                                float NewScale = NewDistance / mDistance;
                                mMatrix.postScale(NewScale, NewScale, mSecondPointF.x,
                                        mSecondPointF.y);
                            }
                        }
                        break;
                }

                //將mMatrix滑動縮放控制帶入
                ScaleImage.this.setImageMatrix(mMatrix);


                return true;
            }
        });
    }

    private void checkScaleLimit()
    {
        float level[] = new float[9];
        mMatrix.getValues(level);

        RectF rectF = new RectF();
        mMatrix.mapRect(rectF);
        mLastUpPointF = new PointF(rectF.centerX() + super.getDrawable().getIntrinsicWidth() * 0.5f * level[0],
                rectF.centerY() + super.getDrawable().getIntrinsicHeight() * 0.5f * level[0]);

        //狀態為縮放時進入
        if (mState == STATE_ZOOM)
        {
            Log.d("debug", level[0] + "");
            //若層級小於1則縮放至原始大小(第一位沒有旋轉時可以直接代表縮放層級)
            if (level[0] < mMinScale)
            {
                mCurrentToMinScale = level[0];
                //                mMatrix.setScale(mMinScale, mMinScale, mSecondPointF.x, mSecondPointF.y);
                //                this.setImageMatrix(mMatrix);
                //                invalidate();
                mState = STATE_ANIMATION_TO_MIN;
                invalidate();
                //                mMatrix.postTranslate(mCenterX,mCenterY);
            }
            //若縮放層級大於最大層級則顯示最大層級
            else if (level[0] > mMaxScale)
            {
                //                mMatrix.set(mChangeMatrix);
                mCurrentToMaxScale = level[0];
                mState = STATE_ANIMATION_TO_MAX;
                invalidate();
            }
            else
            {
                mState = STATE_NONE;
            }

        }
        else
        {
            mState = STATE_NONE;
        }
    }
}
