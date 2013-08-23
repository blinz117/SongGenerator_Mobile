package com.blinz117.songgenerator;

import java.io.*;

import com.blinz117.songgenerator.MidiManager;
import com.leff.midi.*;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

public class MainActivity extends Activity implements OnItemSelectedListener, OnCompletionListener{

	Spinner timeSigNumSpin;
	Spinner timeSigDenomSpin;
	Button songGenButton;
	TextView songStructureView;
	Button playButton;
	
	SongWriter songWriter;
	Song currSong;
	
	MediaPlayer mediaPlayer;
	
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
		
		playButton = (Button)findViewById(R.id.songPlay);
		playButton.setOnClickListener(onPlaySong);
		songWriter = new SongWriter();
		currSong = null;
		
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(this);
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
			Song currSong = songWriter.writeNewSong();
			String displayString = "Time Signature: ";
			displayString += songWriter.getTimeSigNumerator() + "/" + songWriter.getTimeSigDenominator();
			displayString = displayString + "\n" + currSong.vStructure.toString();
			displayString = displayString + "\nVerse: " + currSong.verseChords;
			displayString = displayString + "\nChorus: " + currSong.chorusChords;
			displayString = displayString + "\nBridge: " + currSong.bridgeChords;
			displayString += "\nRhythm1: " + currSong.rhythm1;
			displayString += "\nRhythm2: " + currSong.rhythm2;
			
			
			songStructureView.setText(displayString);
			
			MidiManager midiManager = new MidiManager();
			MidiFile midiSong = midiManager.generateChordMidi(currSong, songWriter.mTimeSigNumer, songWriter.mTimeSigDenom);
			
			File midiOut = new File(getApplicationContext().getFilesDir(), "tempOut.mid");
			try {
				midiSong.writeToFile(midiOut);
			} 
			catch(Exception e) {
				Context context = getApplicationContext();
				CharSequence text = "Oops! Something bad happened trying to create a new MIDI!";
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			}
			
			playButton.setEnabled(true);
		}
	};
	
	View.OnClickListener onPlaySong = new View.OnClickListener() {
		public void onClick(View view)
		{			
			if (mediaPlayer.isPlaying())
			{
				mediaPlayer.stop();
				mediaPlayer.reset();
			}
			
			FileInputStream midiStream;
			try 
			{
				midiStream = openFileInput(getResources().getString(R.string.temp_midi));
				FileDescriptor fd = midiStream.getFD();
				mediaPlayer.setDataSource( fd );
				mediaPlayer.prepare();
				mediaPlayer.start();
				
				playButton.setEnabled(false);
				songGenButton.setEnabled(false);
			}
			catch  (Exception e) 
			{ 
				Context context = getApplicationContext();
				CharSequence text = "Oops! Something bad happened trying to find your MIDI! Here's the message: " + e.getMessage();
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			}
			
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

	@Override
	public void onCompletion(MediaPlayer mp) {
		songGenButton.setEnabled(true);
		playButton.setEnabled(true);
		
		mp.reset();
	}

}
