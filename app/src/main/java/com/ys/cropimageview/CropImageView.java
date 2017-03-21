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
import android.graphics.drawable.Drawable;
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
    private Paint mPaint = new Paint();
    private Context mContext;
    private PointF    mSPoint    = new PointF(0f, 0f);
    private float     mXDistance = 0;
    private float     mYDistance = 0;
    private RectF     mClipRectF = new RectF();
    private int       mCColor    = Color.argb(100, 100, 100, 100);
    private ViewState mViewState = ViewState.normal;
    private Drawable mDrawable;
    private Bitmap   mBitmap;
    private Rect mBitmapRect = null;
    private RectF mViewRect;
    private Paint                 mWhitePaint            = null;
    private OnStateChangeListener mOnStateChangeListener = null;
    private Path mClipPath;

    enum ViewState
    {
        normal, focus, cropped
    }

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

    private void init(Context context)
    {
        mContext = context;
        mDrawable = getResources().getDrawable(R.drawable.emt);
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
        if (event.getAction() == MotionEvent.ACTION_CANCEL)
        {
            onCancel(event);
            return true;
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE)
        {
            onMove(event);
            return true;
        }
        else if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            onDown(event);
            return true;
        }
        else if (event.getAction() == MotionEvent.ACTION_UP)
        {
            onUp(event);
            return true;
        }
        else
        {
            return false;
        }

    }

    private void onUp(MotionEvent e)
    {
        log("onUp");
        mViewState = ViewState.focus;

        invalidate();
    }


    private void onMove(MotionEvent e)
    {
        if (mViewState != ViewState.normal)
            return;

        float tempDistanceX = (e.getX() - mSPoint.x);
        float tempDistanceY = (e.getY() - mSPoint.y);

        float tempX = tempDistanceX + mXDistance;
        float tempY = tempDistanceY + mYDistance;

        if (tempX <= getWidth() && tempX >= 0)
            mXDistance = tempDistanceX;

        if (tempY <= getHeight() && tempY >= 0)
            mYDistance = e.getY() - mSPoint.y;

        mClipRectF.set(mSPoint.x, mSPoint.y, mSPoint.x + mXDistance, mSPoint.y + mYDistance);
        invalidate();

    }

    private void onCancel(MotionEvent e)
    {
        log("onCancel");
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if (mViewState == ViewState.normal)
        {
            // 以原本圖片縮放到本CropImageView的寬度的比例為基準,縮放原圖的高度到屏幕上
            float mul = (float) getWidth() / (float) mBitmapRect.right;
            mViewRect.set(0, 0, getWidth(), mBitmapRect.bottom * mul);
            canvas.drawBitmap(mBitmap, mBitmapRect, mViewRect, null);

            // 畫出當前使用者選擇的區域
            mPaint.setStrokeWidth(1);
            mPaint.setColor(mCColor);
            canvas.drawCircle(mClipRectF.centerX(), mClipRectF.centerY(), mClipRectF.width(),
                    mPaint);

        }
        else if (mViewState == ViewState.focus)
        {
            mPaint.setColor(mCColor);

            canvas.drawBitmap(mBitmap, mBitmapRect, mViewRect, null);

            buildDrawingCache();

            canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);

            canvas.drawCircle(mClipRectF.centerX(), mClipRectF.centerY(), mClipRectF.width() + 4,
                    mWhitePaint);

            mClipPath.reset();
            mClipPath.addCircle(mClipRectF.centerX(), mClipRectF.centerY(), mClipRectF.width(),
                    Path.Direction.CCW);

            canvas.clipPath(mClipPath);

            canvas.drawColor(Color.TRANSPARENT);

            canvas.drawBitmap(mBitmap, mBitmapRect, mViewRect, null);

            focus();
        }
        else if (mViewState == ViewState.cropped)
        {
            //TODO
            Bitmap result = getDrawingCache();
            mOnStateChangeListener.OnCroppedResult(result, mClipRectF);
        }
    }

    @Override
    public boolean onDown(MotionEvent e)
    {
        if (mViewState != ViewState.normal)
            return false;

        mSPoint.set(e.getX(), e.getY());
        return true;
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

    public void decideFocus()
    {
        //TODO 將選取範圍的圖片輸出成Drawable or bitmap
        mViewState = ViewState.cropped;
        invalidate();
    }

    public void cancelFocus()
    {
        mViewState = ViewState.normal;
        mClipRectF.set(0, 0, 0, 0);
        invalidate();
    }

    public void setOnStateChangeListener(OnStateChangeListener listener)
    {
        this.mOnStateChangeListener = listener;
    }

    public void focus()
    {
        if (mOnStateChangeListener != null)
            mOnStateChangeListener.OnEnterFocusState(this);
    }

    public interface OnStateChangeListener
    {
        void OnEnterNormalState(CropImageView cropImageView);

        void OnEnterFocusState(CropImageView cropImageView);

        void OnCroppedState(CropImageView cropImageView, Drawable drawable);

        void OnCroppedResult(Bitmap originalDrawable, RectF croppedRect);


    }
}
