package com.ys.cropimageview.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;


/**
 * Created by ImL1s on 2017/3/28.
 * <p>
 * DESC:
 */

public class ScaleAnimationActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(new MyView(ScaleAnimationActivity.this));
    }


    class MyView extends View
    {

        Rect  rect;
        Paint paint;
        private Thread mDrawThread;

        public MyView(Context context)
        {
            // TODO Auto-generated constructor stub
            super(context);
            init();
        }

        public void init()
        {

            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLUE);
            rect = new Rect(100, 100, 100, 100);

            mDrawThread = new Thread(new Runnable()
            {

                @Override
                public void run()
                {
                    // TODO Auto-generated method stub
                    for (int i = 100; i < 1000; i++)
                    {
                        rect.union(100, 100, i, i);
                        postInvalidate();
                        try
                        {
                            Thread.sleep(100);
                        }
                        catch (InterruptedException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                }
            });

            mDrawThread.start();

        }

        @Override
        protected void onDraw(final Canvas canvas)
        {
            // TODO Auto-generated method stub

            canvas.drawRect(rect, paint);
        }
    }
}
