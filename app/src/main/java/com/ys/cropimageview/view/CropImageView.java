package com.ys.cropimageview.view;

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

import com.ys.cropimageview.R;

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
    private RectF mViewRectF;
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
    private float mFocusDiffX;
    private float mFocusDiffY;
    private float mBaseDistance = 0;
    private Bitmap  mCacheBitmap;
    private float   mScale;
    private boolean mCanSingleMove;


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
        mBitmap = null;
//        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.emt);
//        mBitmapRect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
//        mViewRectF = new RectF(0, 0, getWidth(), getHeight());

        mWhitePaint = new Paint();
        mWhitePaint.setColor(Color.WHITE);
        mWhitePaint.setStrokeWidth(8);
        mClipPath = new Path();
    }

    public void setCroppedImage(int drawableId)
    {
        mBitmap = BitmapFactory.decodeResource(getResources(), drawableId);
        mBitmapRect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        mViewRectF = new RectF(0, 0, getWidth(), getHeight());

        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch ((event.getAction() & MotionEvent.ACTION_MASK))
        {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerId(event.getActionIndex()) == 0 &&
                    mViewRectF.contains(event.getX(), event.getY()))
                {
                    onDown(event);
                }
                if (mBaseDistance != 0)
                    mBaseDistance = 0;
                return true;

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 2)
                {
                    onDoubleFingerMove(event);
                    recordFirstFingerLastMovePoint(event);
                }
                if (event.getPointerId(event.getActionIndex()) == 0 &&
                    mViewRectF.contains(event.getX(), event.getY()))
                {
                    onSingleFingerMove(event);
                }
                return true;

            case MotionEvent.ACTION_UP:
                onUp(event);
                return true;

            case MotionEvent.ACTION_POINTER_UP:
                onPointerUp(event);
                return true;

            case MotionEvent.ACTION_CANCEL:
                onCancel(event);
                return true;
        }


        return false;
    }

    private void onPointerUp(MotionEvent e)
    {
        switch (mViewState)
        {
            case focus:
                if (e.getPointerId(e.getActionIndex()) == 0 &&
                    mViewRectF.contains(e.getX(), e.getY()))
                {
                    mCanSingleMove = false;
                }
                break;
        }
    }

    /**
     * 雙指滑動做縮放
     *
     * @param event
     */
    private void onDoubleFingerMove(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        float distance = (float) Math.sqrt(x * x + y * y);

        if (mBaseDistance == 0)
        {
            mBaseDistance = distance;
        }
        else if (distance - mBaseDistance > 10 || distance - mBaseDistance < -10)
        {
            mScale = (distance / mBaseDistance);

            float width = mViewRectF.width() * mScale;
            float height = mViewRectF.height() * mScale;

            if (width < getWidth() * 0.5f || height < getHeight() * 0.5f ||
                width > getWidth() * 2f || height > getHeight() * 2f)
                return;

            mBaseDistance = distance;

            mViewRectF.set(mViewRectF.centerX() - width * 0.5f,
                    mViewRectF.centerY() - height * 0.5f, mViewRectF.centerX() + width * 0.5f,
                    mViewRectF.centerY() + height * 0.5f);

            invalidate();
        }
    }


    @Override
    protected void onDraw(Canvas canvas)
    {
        if (mBitmap == null)
            return;

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

        renderOriginalImageWithViewRect(canvas);

        buildBitmapCache();

        drawBlackMask(canvas);

        canvas.drawCircle(mClipRectF.centerX(), mClipRectF.centerY(), mClipRectF.width() * 0.5f + 4,
                mWhitePaint);

        mClipPath.reset();

        mClipPath.addCircle(mClipRectF.centerX(), mClipRectF.centerY(), mClipRectF.width() * 0.5f,
                Path.Direction.CCW);

        canvas.clipPath(mClipPath);

        canvas.drawBitmap(mBitmap, mBitmapRect, mViewRectF, null);

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

        canvas.drawBitmap(mBitmap, null, mViewRectF, null);

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
        mViewRectF.set(0, 0, getWidth(), mBitmapRect.bottom * mul);
        canvas.drawBitmap(mBitmap, mBitmapRect, mViewRectF, null);
    }

    private void renderOriginalImageWithViewRect(Canvas canvas)
    {
        float mul = (float) getWidth() / (float) mBitmapRect.right;

        //        mViewRectF.set(mFocusDiffX, mFocusDiffY, mFocusDiffX + getWidth(),
        //                mFocusDiffY + mBitmapRect.bottom * mul);

        canvas.drawBitmap(mBitmap, mBitmapRect, mViewRectF, null);
    }


    // region method - touch event
    private void onUp(MotionEvent e)
    {
        switch (mViewState)
        {
            case normal:
                mViewState = ViewState.focus;
                break;

            case focus:
                if (e.getPointerId(e.getActionIndex()) == 0 &&
                    mViewRectF.contains(e.getX(), e.getY()))
                {
                    mCanSingleMove = false;
                }

                if (mViewRectF.left > mClipRectF.left)
                {
                    float x = mClipRectF.left - mViewRectF.left;
                    mViewRectF.left += x;
                    mViewRectF.right += x;
                }
                else if (mViewRectF.right < mClipRectF.right)
                {
                    float x = mClipRectF.right - mViewRectF.right;
                    mViewRectF.left += x;
                    mViewRectF.right += x;
                }
                if (mViewRectF.top > mClipRectF.top)
                {
                    float y = mClipRectF.top - mViewRectF.top;
                    mViewRectF.top += y;
                    mViewRectF.bottom += y;
                }
                else if (mViewRectF.bottom < mClipRectF.bottom)
                {
                    float y = mClipRectF.bottom - mViewRectF.bottom;
                    mViewRectF.top += y;
                    mViewRectF.bottom += y;
                }

                invalidate();

                break;
        }


        invalidate();
    }


    private void onSingleFingerMove(MotionEvent e)
    {
        switch (mViewState)
        {
            case normal:
                onSingleMoveNormal(e);
                break;

            case focus:
                onSingleMoveFocus(e);
                break;

            case cropped:

                break;
        }
    }

    private void onSingleMoveNormal(MotionEvent e)
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


    private void onSingleMoveFocus(MotionEvent e)
    {
        mFocusDiffX = -(mLastMovePointF.x - e.getX());
        mFocusDiffY = -(mLastMovePointF.y - e.getY());

        recordFirstFingerLastMovePoint(e);

        mViewRectF.set(mFocusDiffX + mViewRectF.left, mFocusDiffY + mViewRectF.top,
                mFocusDiffX + mViewRectF.right, mFocusDiffY + mViewRectF.bottom);

        //        mViewRectF.set(mFocusDiffX + mViewRectF.left, mFocusDiffY + mViewRectF.top,
        //                mFocusDiffX + mViewRectF.right, mFocusDiffY + mViewRectF.bottom);

        invalidate();
    }

    /**
     * 紀錄第一根手指(負責移動圖片的手指)最後的位置
     *
     * @param e
     */
    private void recordFirstFingerLastMovePoint(MotionEvent e)
    {
        int id = e.getPointerId(e.getActionIndex());
        if (id != 0)
            return;

        mLastMovePointF.x = e.getX();
        mLastMovePointF.y = e.getY();
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
                if (e.getPointerId(e.getActionIndex()) == 0 &&
                    mViewRectF.contains(e.getX(), e.getY()))
                {
                    mCanSingleMove = true;
                    mLastMovePointF.set(e.getX(), e.getY());
                }
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
