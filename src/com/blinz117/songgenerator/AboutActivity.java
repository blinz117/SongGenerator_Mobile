package com.blinz117.songgenerator;

import android.app.Activity;
import android.os.Bundle;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
}
