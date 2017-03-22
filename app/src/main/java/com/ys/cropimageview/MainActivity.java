package com.ys.cropimageview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import static com.ys.cropimageview.BitmapUtil.zoomBitmap;

public class MainActivity extends AppCompatActivity implements CropImageView.OnStateChangeListener
{
    private CropImageView mCropImageView;

    private View btnOK;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //        setContentView(new CropImageView(this));

        mCropImageView = (CropImageView) findViewById(R.id.civ_crop);
        mCropImageView.setOnStateChangeListener(this);

        findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        btnOK = findViewById(R.id.tv_decide);
        btnOK.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mCropImageView.decideFocus();
            }
        });
        btnOK.setEnabled(false);
    }


    @Override
    public void onEnterNormalState(CropImageView cropImageView)
    {
        btnOK.setEnabled(false);

        float centerX = cropImageView.getWidth() * 0.5f;
        float centerY = cropImageView.getHeight() * 0.5f;
        Log.d("debug", "centerX:" + centerX + "/" + "centerY:" + centerY);

        float diff = centerX * 0.5f;

        float left = centerX - diff;
        float top = centerY - diff;
        float right = centerX + diff;
        float bottom = centerY + diff;

        cropImageView.focus(new RectF(left, top, right, bottom));
    }

    @Override
    public void onEnterFocusState(CropImageView cropImageView)
    {

    }

    @Override
    public void onEnterCroppedState(CropImageView cropImageView)
    {

    }

    @Override
    public void onCompletedFocusState(CropImageView cropImageView, RectF cropRect)
    {
        btnOK.setEnabled(true);
    }

    @Override
    public void onCroppedResult(Bitmap originalDrawable, RectF croppedRect)
    {
        Log.d("debug", originalDrawable.toString());
        Intent intent = new Intent(this, ShowActivity.class);
        intent.putExtra("bitmap", zoomBitmap(originalDrawable, originalDrawable.getWidth() / 10,
                originalDrawable.getHeight() / 10));
        //        startActivity(intent);
        //        finish();
        //        mIvShow.setVisibility(View.VISIBLE);
        //        mIvShow.setImageBitmap(originalDrawable);
    }

    @Override
    public void onCroppedResult(Bitmap bitmap)
    {
        Intent intent = new Intent(this, ShowActivity.class);
        intent.putExtra("bitmap", zoomBitmap(bitmap, .5f, .5f));
        startActivity(intent);
        finish();
    }

}
