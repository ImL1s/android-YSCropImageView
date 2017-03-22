package com.ys.cropimageview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;


/**
 * Created by ImL1s on 2017/3/7.
 *
 * DESC:
 */

public class CircleImageView extends android.support.v7.widget.AppCompatImageView
{

    //基本的三個構造函數
    public CircleImageView(Context context)
    {
        super(context);
    }

    public CircleImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    //自定義View實現過程中很重要的onDraw繪制圖形的方法
    @Override
    protected void onDraw(Canvas canvas)
    {

        Drawable drawable = getDrawable();

        //空值判斷，必要步驟，避免由於沒有設置src導致的異常錯誤
        if (drawable == null)
        {
            return;
        }

        //必要步驟，避免由於初始化之前導致的異常錯誤
        if (getWidth() == 0 || getHeight() == 0)
        {
            return;
        }

        if (!(drawable instanceof BitmapDrawable))
        {
            return;
        }
        Bitmap b = ((BitmapDrawable) drawable).getBitmap();

        if (null == b)
        {
            return;
        }

        Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);

        int w = getWidth();

        Bitmap roundBitmap = getCroppedBitmap(bitmap, w);
        canvas.drawBitmap(roundBitmap, 0, 0, null);

    }

    /**
     * 初始Bitmap對像的縮放裁剪過程
     *
     * @param bmp    初始Bitmap對像
     * @param radius 圓形圖片直徑大小
     * @return 返回一個圓形的縮放裁剪過後的Bitmap對像
     */
    public static Bitmap getCroppedBitmap(Bitmap bmp, int radius)
    {
        Bitmap sbmp;
        //比較初始Bitmap寬高和給定的圓形直徑，判斷是否需要縮放裁剪Bitmap對像
        if (bmp.getWidth() != radius || bmp.getHeight() != radius)
            sbmp = Bitmap.createScaledBitmap(bmp, radius, radius, false);
        else
            sbmp = bmp;
        Bitmap output =
                Bitmap.createBitmap(sbmp.getWidth(), sbmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(sbmp.getWidth() / 2 + 0.7f, sbmp.getHeight() / 2 + 0.7f,
                          sbmp.getWidth() / 2 + 0.1f, paint);
        //核心部分，設置兩張圖片的相交模式，在這裡就是上面繪制的Circle和下面繪制的Bitmap
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);

        return output;
    }

}
