package com.blinz117.songgenerator;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);
		
		TextView licenses = (TextView)findViewById(R.id.licensesText);
		licenses.setMovementMethod(LinkMovementMethod.getInstance());
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
}
