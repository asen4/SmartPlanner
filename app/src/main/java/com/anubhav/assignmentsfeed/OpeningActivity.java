package com.anubhav.assignmentsfeed;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class OpeningActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Intent loginIntent = new Intent(OpeningActivity.this, LRContainerActivity.class);
                startActivity(loginIntent);
            }
        }, 2000);
    }
}