package com.ys.cropimageview.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.ys.cropimageview.R;

public class ShowActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        Intent data = getIntent();

        Bitmap bitmap = (Bitmap) data.getExtras().get("bitmap");
        ImageView iv = (ImageView) findViewById(R.id.iv_shows);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setImageBitmap(bitmap);
    }

}
