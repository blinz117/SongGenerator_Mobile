package com.blinz117.songgenerator;

import com.blinz117.songgenerator.SongWriter.*;

import android.os.Bundle;
import android.app.Activity;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {

	TextView songStructureView;
	Button songGenButton;
	SongWriter songWriter;
	Song currSong;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		songStructureView = (TextView)findViewById(R.id.songStructure);
		songGenButton = (Button)findViewById(R.id.songGen);
		songGenButton.setOnClickListener(onSongGenerate);
		songWriter = new SongWriter();
		currSong = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	View.OnClickListener onSongGenerate = new View.OnClickListener() {
		public void onClick(View view)
		{
			Song currSong = songWriter.writeSong();
			songStructureView.setText(currSong.vStructure.toString());
		}
	};

}
