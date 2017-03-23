package com.ys.cropimageview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

public class TouchActivity extends AppCompatActivity
{
    private static final String LOG_TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int index = event.getActionIndex();

        switch (event.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                log("first finger down");
                break;

            case MotionEvent.ACTION_UP:
                log("all finger up");
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                log("finger down, index:" + index);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                log("finger up, index:" + index);
                break;
        }

        return true;
    }

    private void log(String str)
    {
        Log.d(LOG_TAG, str);
    }


}
