package com.ys.cropimageview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

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
        iv.setImageBitmap(bitmap);
    }
}
