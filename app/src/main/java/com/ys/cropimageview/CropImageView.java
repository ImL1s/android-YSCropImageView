package com.ys.cropimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * Created by ImL1s on 2017/3/21.
 * <p>
 * DESC:
 */

public class CropImageView extends View
        implements GestureDetector.OnGestureListener, View.OnClickListener
{
    private Context mContext;
    private float mXDistance     = 0;
    private float mYDistance     = 0;
    private int   mCColor        = Color.argb(100, 100, 100, 100);
    private int   mCropBackColor = Color.BLACK;

    private Rect mBitmapRect = null;
    private RectF mViewRect;
    private RectF  mClipRectF      = new RectF();
    private Paint  mPaint          = new Paint();
    private PointF mSPoint         = new PointF(0f, 0f);
    private PointF mLastMovePointF = new PointF(0, 0);

    private Bitmap mBitmap;
    private Bitmap croppedBitmap;
    private Path   mClipPath;
    private Paint                 mWhitePaint   = null;
    private OnStateChangeListener mOnSCListener = null;
    private ViewState             mViewState    = ViewState.normal;
    private float  mFocusDiffX;
    private float  mFocusDiffY;
    private Bitmap mCacheBitmap;


    enum ViewState
    {
        normal, focus, cropped
    }

    // region method - constructor
    public CropImageView(Context context)
    {
        super(context);
        init(context);
    }

    public CropImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context);
    }
    // endregion

    private void init(Context context)
    {
        mContext = context;
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.emt);
        mBitmapRect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        mViewRect = new RectF(0, 0, getWidth(), getHeight());

        mWhitePaint = new Paint();
        mWhitePaint.setColor(Color.WHITE);
        mWhitePaint.setStrokeWidth(8);
        mClipPath = new Path();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event.getPointerCount() == 1)
        {
            Log.d("debug", "雙點觸控: " + event.getPointerCount());

            /* TODO 1.雙點觸控down時紀錄當前兩點距離
             *  2.Move時計算當前距離與原距離的比例(當前/原本)
             *  3.按照比例進行縮放處理
             */

            return true;
            //            return true;
        }
        //        else if (event.getAction() > 0x00ff)
        //        {
        //            Log.d("debug", "多點觸控");
        //            return true;
        //        }
        else
        {
            switch ((event.getAction() & MotionEvent.ACTION_MASK))
            {
                case MotionEvent.ACTION_DOWN:
                    onDown(event);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    onMove(event);
                    return true;

                case MotionEvent.ACTION_UP:
                    onUp(event);
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    onCancel(event);
                    return true;
            }
        }

        return false;
    }


    @Override
    protected void onDraw(Canvas canvas)
    {
        switch (mViewState)
        {
            case normal:
                onDrawNormalState(canvas);
                break;

            case focus:
                onFocusState(canvas);
                break;

            case cropped:
                onDrawCroppedState();
                break;
        }
    }

    /**
     * 一般狀態繪製時(onDraw)邏輯
     *
     * @param canvas
     */
    private void onDrawNormalState(Canvas canvas)
    {
        if (mOnSCListener != null)
            mOnSCListener.onEnterNormalState(this);

        // 渲染被裁切的圖片
        renderOriginalImage(canvas);

        // 畫出用戶選擇的區域
        drawArea(canvas);
    }

    /**
     * 專注狀態繪製時(onDraw)繪製邏輯
     *
     * @param canvas
     */
    private void onFocusState(Canvas canvas)
    {
        if (mOnSCListener != null)
            mOnSCListener.onEnterFocusState(this);

        canvas.drawColor(mCropBackColor);

        mPaint.setColor(mCColor);

        renderOriginalImageWithDiff(canvas);

        buildBitmapCache();

        drawBlackMask(canvas);

        canvas.drawCircle(mClipRectF.centerX(), mClipRectF.centerY(), mClipRectF.width() * 0.5f + 4,
                mWhitePaint);

        mClipPath.reset();

        mClipPath.addCircle(mClipRectF.centerX(), mClipRectF.centerY(), mClipRectF.width() * 0.5f,
                Path.Direction.CCW);

        canvas.clipPath(mClipPath);

        canvas.drawBitmap(mBitmap, mBitmapRect, mViewRect, null);

        if (mOnSCListener != null)
            mOnSCListener.onCompletedFocusState(this, mClipRectF);
    }

    /**
     * 裁切狀態繪製時(onDraw)邏輯
     */
    private void onDrawCroppedState()
    {
        if (mOnSCListener != null)
            mOnSCListener.onEnterCroppedState(this);

        //            mResultBitmap = getDrawingCache();

        if (mOnSCListener != null)
            mOnSCListener.onCroppedResult(mCacheBitmap, mClipRectF);

        croppedBitmap =
                Bitmap.createBitmap(getBitmapCache(), (int) mClipRectF.left, (int) mClipRectF.top,
                        (int) mClipRectF.width(), (int) mClipRectF.height());

        if (mOnSCListener != null)
            mOnSCListener.onCroppedResult(croppedBitmap);
    }

    /**
     * 將當前View所顯示的樣子存儲成bitmap
     *
     * @return 當前View樣貌的bitmap
     */
    public Bitmap buildBitmapCache()
    {
        mCacheBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(mCacheBitmap);

        canvas.drawBitmap(mBitmap, null, mViewRect, null);

        return mCacheBitmap;
    }

    public Bitmap getBitmapCache()
    {
        return mCacheBitmap;
    }

    /**
     * 在canvas上畫出黑色半透明遮罩
     *
     * @param canvas
     */
    private void drawBlackMask(Canvas canvas)
    {
        canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
    }

    /**
     * 畫出當前使用者選擇的區域
     *
     * @param canvas
     */
    private void drawArea(Canvas canvas)
    {
        mPaint.setStrokeWidth(1);
        mPaint.setColor(mCColor);
        canvas.drawCircle(mClipRectF.centerX(), mClipRectF.centerY(), mClipRectF.width() * 0.5f,
                mPaint);
    }

    /**
     * 以原本圖片縮放到本CropImageView的寬度的比例為基準,縮放原圖的高度到View上
     *
     * @param canvas
     */
    private void renderOriginalImage(Canvas canvas)
    {
        float mul = (float) getWidth() / (float) mBitmapRect.right;
        mViewRect.set(0, 0, getWidth(), mBitmapRect.bottom * mul);
        canvas.drawBitmap(mBitmap, mBitmapRect, mViewRect, null);
    }

    private void renderOriginalImageWithDiff(Canvas canvas)
    {
        float mul = (float) getWidth() / (float) mBitmapRect.right;

        mViewRect.set(mFocusDiffX, mFocusDiffY, mFocusDiffX + getWidth(),
                mFocusDiffY + mBitmapRect.bottom * mul);

        canvas.drawBitmap(mBitmap, mBitmapRect, mViewRect, null);
    }


    // region method - touch event
    private void onUp(MotionEvent e)
    {
        mViewState = ViewState.focus;
        invalidate();
    }


    private void onMove(MotionEvent e)
    {
        switch (mViewState)
        {
            case normal:
                onMoveNormal(e);
                break;

            case focus:
                onMoveFocus(e);
                break;

            case cropped:

                break;
        }
    }

    private void onMoveNormal(MotionEvent e)
    {
        float tempDistanceX = (e.getX() - mSPoint.x);
        //        float tempDistanceY = (e.getY() - mSPoint.y);

        float tempX = tempDistanceX + mSPoint.x;
        //        float tempY = tempDistanceY + mYDistance;

        if (tempX <= getWidth() && tempX >= 0)
            mXDistance = tempDistanceX;

        // Rect長寬會不同
        //        if (tempY <= getHeight() && tempY >= 0)
        //        mYDistance = tempDistanceY;

        // Rect長寬以寬度為準
        if (tempX <= getHeight() && tempX >= 0)
            mYDistance = tempDistanceX;

        mClipRectF.set(mSPoint.x, mSPoint.y, mSPoint.x + mXDistance, mSPoint.y + mXDistance);
        invalidate();
    }


    private void onMoveFocus(MotionEvent e)
    {
        //        float oX = mLastMovePointF.x;
        //        float oY = mLastMovePointF.y;

        mFocusDiffX += -(mLastMovePointF.x - e.getX());
        mFocusDiffY += -(mLastMovePointF.y - e.getY());

        mLastMovePointF.x = e.getX();
        mLastMovePointF.y = e.getY();

        invalidate();
    }

    @Override
    public boolean onDown(MotionEvent e)
    {
        switch (mViewState)
        {
            case normal:
                mSPoint.set(e.getX(), e.getY());
                return true;

            case focus:
                mLastMovePointF.set(e.getX(), e.getY());
                return true;
        }

        return false;


    }

    private void onCancel(MotionEvent e)
    {
        //        mSPoint.set(e.getX(), e.getY());
        log("onCancel");
    }

    @Override
    public void onShowPress(MotionEvent e)
    {
        log("onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e)
    {
        log("onSingleTapUp");
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        log("onScroll");
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e)
    {
        log("onLongPress");
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        log("onFLing");
        return true;
    }

    @Override
    public void onClick(View v)
    {
        log("onClick");
    }

    private void log(String str)
    {
        Log.d("debug", str);
        Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
    }
    // endregion


    /**
     * 將狀態轉換成cropped狀態,將當前畫面被匡選的部分輸出
     */
    public void decideFocus()
    {
        mViewState = ViewState.cropped;
        invalidate();
    }

    /**
     * 將狀態轉換成normal狀態,也就是變成能夠拉出圓形選擇框的
     */
    public void cancelFocus()
    {
        mViewState = ViewState.normal;
        mClipRectF.set(0, 0, 0, 0);
        invalidate();
    }

    public void setOnStateChangeListener(OnStateChangeListener listener)
    {
        this.mOnSCListener = listener;
    }

    /**
     * 進入focus狀態(移動背後圖片階段)
     *
     * @param rectF
     */
    public void focus(RectF rectF)
    {
        mClipRectF = rectF;
        this.mViewState = ViewState.focus;
        invalidate();
    }

    public interface OnStateChangeListener
    {
        void onEnterNormalState(CropImageView cropImageView);

        void onEnterFocusState(CropImageView cropImageView);

        void onEnterCroppedState(CropImageView cropImageView);

        void onCompletedFocusState(CropImageView cropImageView, RectF cropRect);

        void onCroppedResult(Bitmap originalDrawable, RectF croppedRect);

        void onCroppedResult(Bitmap bitmap);
    }
}
