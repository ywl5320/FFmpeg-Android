package com.ywl5320.ffmpegas;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    FFmpegPlayer fFmpegPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fFmpegPlayer = new FFmpegPlayer();
        fFmpegPlayer.playMyMedia("/mnt/shared/Other/mydream.m4a");
    }
}
