package com.anubhav.assignmentsfeed;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity {

    private ImageView imageView;
    private String imageURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imageView = findViewById(R.id.imageViewer);
        imageURL = getIntent().getStringExtra("URL");
        Picasso.get().load(imageURL).placeholder(R.drawable.ic_baseline_image_24).into(imageView);
    }
}