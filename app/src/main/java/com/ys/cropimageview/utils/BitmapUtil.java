package com.ys.cropimageview.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by ImL1s on 2017/3/22.
 * <p>
 * DESC:
 */

public class BitmapUtil
{
    /**
     * 縮放bitmap到指定的大小
     *
     * @param bitmap
     * @param width
     * @param height
     * @return
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height)
    {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }

    /**
     * 縮放bitmap到指定的比例
     *
     * @param bitmap
     * @param scaleX
     * @param scaleY
     * @return
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, float scaleX, float scaleY)
    {
        scaleX = 1 / scaleX;
        scaleY = 1 / scaleY;
        return zoomBitmap(bitmap, (int) (bitmap.getWidth() / scaleX),
                (int) (bitmap.getHeight() / scaleY));
    }

}
