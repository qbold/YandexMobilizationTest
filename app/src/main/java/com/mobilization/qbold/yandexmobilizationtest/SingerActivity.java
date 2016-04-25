package com.mobilization.qbold.yandexmobilizationtest;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

// Активность с расширенной информацией об исполнителе
public class SingerActivity extends AppCompatActivity {

    private Singer s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singer);
        s = getIntent().getParcelableExtra("singer");
        setTitle(s.getName());
        ((TextView) findViewById(R.id.textView4)).setText(s.getGenres());
        ((TextView) findViewById(R.id.textView5)).setText(s.getAlbums() + " · " + s.getTracks());
        ((TextView) findViewById(R.id.textView7)).setText(s.getDescription());
    }

    // Когда стали доступны размеры элементов управления, решаем какую иконку исполнителя ставить в активности с
    // расширенной информацией об исполнителе - большую или маленькую (в зависимости от размера ImageView)
    @Override
    public void onWindowFocusChanged(boolean focus) {
        final ImageView img = ((ImageView) findViewById(R.id.imageView2));
        final ProgressBar progress = ((ProgressBar) findViewById(R.id.pb2));
        IconsLoader.SetIconCallback callback = new IconsLoader.SetIconCallback() {
            @Override
            public void setIcon(final Bitmap big) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.setVisibility(View.GONE);
                        img.setImageBitmap(big);
                    }
                });
            }
        };
        if (img.getWidth() > 300) {
            IconsLoader.loadBigIcon(s, callback);
        } else {
            IconsLoader.loadSmallIcon(s, callback);
        }
    }
}
