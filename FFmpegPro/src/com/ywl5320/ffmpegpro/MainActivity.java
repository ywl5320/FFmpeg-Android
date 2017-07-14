package com.ywl5320.ffmpegpro;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.ywl5320.ffmpegpro.R;

public class MainActivity extends Activity {

	FFmplayer ffmplayer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ffmplayer = new FFmplayer();
		ffmplayer.showFFmpegInfo();
	}
}
