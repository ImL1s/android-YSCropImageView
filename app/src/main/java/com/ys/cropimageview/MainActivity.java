package com.ys.cropimageview;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements CropImageView.OnStateChangeListener
{

    private CropImageView mCropImageView;

    private ImageView mIvShow;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //        setContentView(new CropImageView(this));

        mIvShow = (ImageView) findViewById(R.id.iv_show);
        mCropImageView = (CropImageView) findViewById(R.id.civ_crop);
        mCropImageView.setOnStateChangeListener(this);

        findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mCropImageView.cancelFocus();
            }
        });

        findViewById(R.id.tv_decide).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mCropImageView.decideFocus();
            }
        });


    }


    @Override
    public void OnEnterNormalState(CropImageView cropImageView)
    {

    }

    @Override
    public void OnEnterFocusState(CropImageView cropImageView)
    {

    }

    @Override
    public void OnCroppedState(CropImageView cropImageView, Drawable drawable)
    {

    }

    @Override
    public void OnCroppedResult(Bitmap originalDrawable, RectF croppedRect)
    {
        Log.d("debug", originalDrawable.toString());
        mIvShow.setVisibility(View.VISIBLE);
        mIvShow.setImageBitmap(originalDrawable);
    }
}
