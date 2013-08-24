package com.blinz117.songgenerator;

import java.io.*;
import java.util.ArrayList;

import com.blinz117.songgenerator.MidiManager;
import com.blinz117.songgenerator.SongStructure.*;

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
	
	/*
	 * State handlers
	 */
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
	
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	//Save state on certain changes, such as screen rotation
    	boolean hasSong = (currSong != null);
    	savedInstanceState.putBoolean("HASSONG", hasSong);
    	
    	if (hasSong)
    	{
	    	savedInstanceState.putSerializable("STRUCTURE", currSong.vStructure);
	    	savedInstanceState.putSerializable("VERSE", currSong.verseChords);
	    	savedInstanceState.putSerializable("CHORUS", currSong.chorusChords);
	    	savedInstanceState.putSerializable("BRIDGE", currSong.bridgeChords);
	    	
	    	savedInstanceState.putSerializable("MELODY", currSong.melody);
	    	
	    	savedInstanceState.putSerializable("TSNUM", currSong.timeSigNum);
	    	savedInstanceState.putSerializable("TSDENOM", currSong.timeSigDenom);
    	}
        super.onSaveInstanceState(savedInstanceState);
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	//Restore state on certain changes, such as screen rotation
          super.onRestoreInstanceState(savedInstanceState);
          boolean hasSong = savedInstanceState.getBoolean("HASSONG");
          if (hasSong)
          {
	          currSong = new Song();
	          currSong.vStructure = (ArrayList<SongPart>) savedInstanceState.getSerializable("STRUCTURE");
	          currSong.verseChords = (ArrayList<Integer>) savedInstanceState.getSerializable("VERSE");
	          currSong.chorusChords = (ArrayList<Integer>) savedInstanceState.getSerializable("CHORUS");
	          currSong.bridgeChords = (ArrayList<Integer>) savedInstanceState.getSerializable("BRIDGE");
	          
	          currSong.melody = (ArrayList<ArrayList<Integer>>) savedInstanceState.getSerializable("MELODY");
	
	          currSong.timeSigNum = (Integer) savedInstanceState.getSerializable("TSNUM");
	          currSong.timeSigDenom = (Integer) savedInstanceState.getSerializable("TSDENOM");
	          updateDisplay();
	          // Probably need to do a bit of checking if we are currently playing a song. As it is,
	          // I think we lost the media player. Maybe need to restore this somehow.
	          //createTempMidi();
	          playButton.setEnabled(true);
          }
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
			currSong = songWriter.writeNewSong();
			updateDisplay();
			createTempMidi();
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
				
				songGenButton.setEnabled(true);
				playButton.setText(getResources().getString(R.string.play_song));
				return;
			}
			
			FileInputStream midiStream;
			try 
			{
				midiStream = openFileInput(getResources().getString(R.string.temp_midi));
				FileDescriptor fd = midiStream.getFD();
				mediaPlayer.setDataSource( fd );
				mediaPlayer.prepare();
				mediaPlayer.start();
				
				//playButton.setEnabled(false);
				playButton.setText(getResources().getString(R.string.stop_play));
				songGenButton.setEnabled(false);
			}
			catch  (Exception e) 
			{ 
				Context context = getApplicationContext();
				CharSequence text = getResources().getString(R.string.error_read_MIDI);//"Oops! Something bad happened trying to find your MIDI file! Here's the message: " + e.getMessage();
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
		//playButton.setEnabled(true);
		playButton.setText(getResources().getString(R.string.play_song));
		
		mp.reset();
	}
	
	public void updateDisplay()
	{
		if (currSong == null)
			return;
		
		String displayString = "Time Signature: ";
		displayString += currSong.timeSigNum + "/" + currSong.timeSigDenom;
		displayString = displayString + "\n" + currSong.vStructure.toString();
		displayString = displayString + "\nVerse: " + currSong.verseChords;
		displayString = displayString + "\nChorus: " + currSong.chorusChords;
		displayString = displayString + "\nBridge: " + currSong.bridgeChords;
		
		/* Don't want to show these for now... 
		displayString += "\nRhythm1: " + currSong.rhythm1;
		displayString += "\nRhythm2: " + currSong.rhythm2;
		
		displayString += "\nTheme: " + currSong.theme;
		*/
		
		displayString += "\nMelody: " + currSong.melody;
		
		songStructureView.setText(displayString);
	}
	
	public void createTempMidi()
	{
		MidiManager midiManager = new MidiManager();
		MidiFile midiSong = midiManager.generateChordMidi(currSong);
		
		File midiOut = new File(getApplicationContext().getFilesDir(), "tempOut.mid");
		try {
			midiSong.writeToFile(midiOut);
		} 
		catch(Exception e) {
			Context context = getApplicationContext();
			CharSequence text = getResources().getString(R.string.error_create_MIDI);//"Oops! Something bad happened trying to create a new MIDI file!";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}

}
