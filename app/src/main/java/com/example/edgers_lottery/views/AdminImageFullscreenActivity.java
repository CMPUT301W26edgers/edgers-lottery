package com.example.edgers_lottery.views;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.edgers_lottery.R;

/**
 * Fullscreen activity for viewing a single image in the admin images view.
 * Should only be accessed for users with admin privileges.
 */
public class AdminImageFullscreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_image_fullscreen);

        ImageView imageView = findViewById(R.id.fullscreenImage);
        String imageUrl = getIntent().getStringExtra("image_url");

        Glide.with(this)
                .load(imageUrl)
                .into(imageView);

        imageView.setOnClickListener(v -> finish());
    }
}