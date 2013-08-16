package com.blinz117.songgenerator;

import com.blinz117.songgenerator.SongWriter.*;

import android.os.Bundle;
import android.app.Activity;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

public class MainActivity extends Activity implements OnItemSelectedListener{

	Spinner timeSigNumSpin;
	Spinner timeSigDenomSpin;
	Button songGenButton;
	TextView songStructureView;
	
	SongWriter songWriter;
	Song currSong;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Set up adapter and listener for spinners
		timeSigNumSpin = (Spinner) findViewById(R.id.timeSigNum);
		ArrayAdapter<CharSequence> numAdapter = ArrayAdapter.createFromResource(this,
		        R.array.timeSig_array, android.R.layout.simple_spinner_item);
		numAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		timeSigNumSpin.setAdapter(numAdapter);
		timeSigNumSpin.setOnItemSelectedListener(this);
		
		timeSigDenomSpin = (Spinner) findViewById(R.id.timeSigDenom);
		ArrayAdapter<CharSequence> denomAdapter = ArrayAdapter.createFromResource(this,
		        R.array.timeSig_array, android.R.layout.simple_spinner_item);
		denomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		timeSigDenomSpin.setAdapter(denomAdapter);
		timeSigDenomSpin.setOnItemSelectedListener(this);
		
		songGenButton = (Button)findViewById(R.id.songGen);
		songGenButton.setOnClickListener(onSongGenerate);
		songStructureView = (TextView)findViewById(R.id.songStructure);
		
		songWriter = new SongWriter();
		currSong = null;
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
	
	View.OnClickListener onSongGenerate = new View.OnClickListener() {
		public void onClick(View view)
		{
			Song currSong = songWriter.writeSong();
			String displayString = "Time Signature: ";
			displayString += songWriter.getTimeSigNumerator() + "/" + songWriter.getTimeSigDenominator();
			displayString = displayString + "\n" + currSong.vStructure.toString();
			displayString = displayString + "\nVerse: " + currSong.verseChords;
			displayString = displayString + "\nChorus: " + currSong.chorusChords;
			displayString = displayString + "\nBridge: " + currSong.bridgeChords;
			displayString += "\nRhythm1: " + currSong.rhythm1;
			displayString += "\nRhythm2: " + currSong.rhythm2;
			
			
			songStructureView.setText(displayString);
		}
	};

	@Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
	{
		Spinner spinner = (Spinner) parent;
		String value = parent.getItemAtPosition(pos).toString();
		if (spinner.getId() == R.id.timeSigNum)
			songWriter.setTimeSigNumerator(Integer.parseInt(value));
		else if (spinner.getId() == R.id.timeSigDenom)
			songWriter.setTimeSigDenominator(Integer.parseInt(value));
    }

	@Override
    public void onNothingSelected(AdapterView<?> parent) {
		//Nothing to do as far as I can tell
	}

}
