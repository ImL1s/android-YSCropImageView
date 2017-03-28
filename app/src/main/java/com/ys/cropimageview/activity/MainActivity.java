package com.ys.cropimageview.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.ys.cropimageview.R;
import com.ys.cropimageview.view.CropImageView;

import static com.ys.cropimageview.utils.BitmapUtil.zoomBitmap;

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

        mCropImageView.setCroppedImage(R.drawable.emt);
//        mCropImageView.setCroppedImage(R.drawable.test_img);
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
        // 注意,如果不縮放圖片大小,會報出TransactionTooLargeException
        intent.putExtra("bitmap", zoomBitmap(bitmap, .1f, .1f));
        startActivity(intent);
        finish();
    }

}
