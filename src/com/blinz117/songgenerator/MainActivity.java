package com.blinz117.songgenerator;

import java.io.*;
import java.util.ArrayList;

import com.blinz117.songgenerator.MidiManager;
import com.blinz117.songgenerator.SaveFileDialogFragment.SaveFileDialogListener;
import com.blinz117.songgenerator.songstructure.Pattern;
import com.blinz117.songgenerator.songstructure.ChordProgression;
import com.blinz117.songgenerator.songstructure.Song;
import com.blinz117.songgenerator.songstructure.MusicStructure.*;

import com.leff.midi.*;
import com.leff.midi.event.ProgramChange.MidiProgram;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.content.Context;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

import android.support.v4.app.*;

public class MainActivity extends FragmentActivity implements OnItemSelectedListener, OnCompletionListener, SaveFileDialogListener{

	Spinner timeSigNumSpin;
	Spinner timeSigDenomSpin;
	Button songGenButton;
	TextView songStructureView;
	Button playButton;
	Button saveButton;
	
	SongWriter songWriter;
	Song currSong;
	
	MidiFile midiSong;
	
	MediaPlayer mediaPlayer;
	
	String saveFileName;
	
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
		
		saveButton = (Button)findViewById(R.id.songSave);
		saveButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				showSaveDialog();
			}
		});
		
		songWriter = new SongWriter();
		currSong = null;
		
		midiSong = null;
		
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(this);
		
		saveFileName = "";
	}
	
/*	
 * TODO: Get this up and running again after making stuff Serializable
 *  SUPRESS THESE FOR NOW...
 *  The way I have restructured Songs, chord progressions, patterns, and melodies, this doesn't lend itself
 *  well to saving/restoring state until I have Song (and its subcomponents) implement the Serializable interface.
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	//Save state on certain changes, such as screen rotation
    	boolean hasSong = (currSong != null);
    	savedInstanceState.putBoolean("HASSONG", hasSong);
    	
    	if (hasSong)
    	{
	    	savedInstanceState.putSerializable("STRUCTURE", currSong.structure);
	    	savedInstanceState.putSerializable("VERSE", currSong.verseProgression.getChords());
	    	savedInstanceState.putSerializable("CHORUS", currSong.chorusProgression.getChords());
	    	savedInstanceState.putSerializable("BRIDGE", currSong.bridgeProgression.getChords());
	    	
	    	savedInstanceState.putSerializable("MELODY", currSong.melody);
	    	
	    	savedInstanceState.putSerializable("TSNUM", currSong.timeSigNum);
	    	savedInstanceState.putSerializable("TSDENOM", currSong.timeSigDenom);
	    	
	    	savedInstanceState.putSerializable("SCALETYPE", currSong.scaleType);
	    	savedInstanceState.putSerializable("KEY", currSong.key);
	    	
	    	savedInstanceState.putSerializable("CHORDINSTRUMENT", currSong.chordInstrument);
	    	savedInstanceState.putSerializable("MELODYINSTRUMENT", currSong.melodyInstrument);
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
	          currSong.structure = (ArrayList<SongPart>) savedInstanceState.getSerializable("STRUCTURE");
	          
	          // TODO: This is not an ideal way to do this... maybe need to implement the serialize interface for
	          // a song object and all of the classes it contains
	          
	          currSong.verseProgression = new ChordProgression();
	          ArrayList<Integer> chords = (ArrayList<Integer>) savedInstanceState.getSerializable("VERSE");
	          // TODO: AAAAHH!!! HACK ALERT! HACK ALERT!
	          // only works right now because patterns and chord progressions all have the same number of elements
	          for (int i = 0; i < 4; i++)
	          {
	        	  Pattern newPattern = new Pattern();
	        	  newPattern.chords.addAll(chords.subList(i*4, i*4 + 4));
	        	  currSong.verseProgression.patterns.add(newPattern);
	          }

	          currSong.chorusProgression = new ChordProgression();
	          chords = (ArrayList<Integer>) savedInstanceState.getSerializable("CHORUS");
	          // TODO: AAAAHH!!! HACK ALERT! HACK ALERT!
	          // only works right now because patterns and chord progressions all have the same number of elements
	          for (int i = 0; i < 4; i++)
	          {
	        	  Pattern newPattern = new Pattern();
	        	  newPattern.chords.addAll(chords.subList(i*4, i*4 + 4));
	        	  currSong.chorusProgression.patterns.add(newPattern);
	          }
	          currSong.bridgeProgression = new ChordProgression();
	          chords = (ArrayList<Integer>) savedInstanceState.getSerializable("BRIDGE");
	          // TODO: AAAAHH!!! HACK ALERT! HACK ALERT!
	          // only works right now because patterns and chord progressions all have the same number of elements
	          for (int i = 0; i < 4; i++)
	          {
	        	  Pattern newPattern = new Pattern();
	        	  newPattern.chords.addAll(chords.subList(i*4, i*4 + 4));
	        	  currSong.bridgeProgression.patterns.add(newPattern);
	          }
	          //currSong.chorusProgression = (ArrayList<Integer>) savedInstanceState.getSerializable("CHORUS");
	         // currSong.bridgeProgression = (ArrayList<Integer>) savedInstanceState.getSerializable("BRIDGE");
	          //currSong.verseProgression = (ArrayList<Integer>) savedInstanceState.getSerializable("VERSE");
	          //currSong.chorusProgression = (ArrayList<Integer>) savedInstanceState.getSerializable("CHORUS");
	         // currSong.bridgeProgression = (ArrayList<Integer>) savedInstanceState.getSerializable("BRIDGE");
	          
	          currSong.melody = (ArrayList<ArrayList<Integer>>) savedInstanceState.getSerializable("MELODY");
	
	          currSong.timeSigNum = (Integer) savedInstanceState.getSerializable("TSNUM");	          
	          currSong.timeSigDenom = (Integer) savedInstanceState.getSerializable("TSDENOM");
	          
	          currSong.scaleType = (ScaleType) savedInstanceState.getSerializable("SCALETYPE");
	          currSong.key = (Pitch) savedInstanceState.getSerializable("KEY");
	          
	          currSong.chordInstrument = (MidiProgram) savedInstanceState.getSerializable("CHORDINSTRUMENT");
	          currSong.melodyInstrument = (MidiProgram) savedInstanceState.getSerializable("MELODYINSTRUMENT");
	          
	          updateDisplay();
	          // Probably need to do a bit of checking if we are currently playing a song. As it is,
	          // I think we lost the media player. Maybe need to restore this somehow.
	          //createTempMidi();
	          playButton.setEnabled(true);
	          saveButton.setEnabled(true);
          }
    }
*/
	
	// TODO: Make a settings menu
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
			saveButton.setEnabled(true);
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
				saveButton.setEnabled(true);
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
				saveButton.setEnabled(false);
				songGenButton.setEnabled(false);
			}
			catch (Exception e) 
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
/*		
 * 		Removing this temporarily. Just randomly generate until I add more customization later
 * 
 * 		Spinner spinner = (Spinner) parent;
		String value = parent.getItemAtPosition(pos).toString();
		if (spinner.getId() == R.id.timeSigNum)
			songWriter.setTimeSigNumerator(Integer.parseInt(value));
		else if (spinner.getId() == R.id.timeSigDenom)
			songWriter.setTimeSigDenominator(Integer.parseInt(value));*/
    }

	@Override
    public void onNothingSelected(AdapterView<?> parent) {
		//Nothing to do as far as I can tell
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		songGenButton.setEnabled(true);
		saveButton.setEnabled(true);
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
		displayString = displayString + "\nTempo: " + currSong.tempo + " BPM";
		displayString = displayString + "\nChord instrument: " + currSong.chordInstrument;
		displayString = displayString + "\nMelody instrument: " + currSong.melodyInstrument;
		displayString += "\nScale: " + currSong.key.toString() + " " + currSong.scaleType;
		displayString = displayString + "\n" + currSong.structure.toString();
		displayString = displayString + "\nVerse: " + currSong.verseProgression.getChords();
		displayString = displayString + "\nChorus: " + currSong.chorusProgression.getChords();
		displayString = displayString + "\nBridge: " + currSong.bridgeProgression.getChords();
		
		/* Don't want to show these for now... 
		displayString += "\nRhythm1: " + currSong.rhythm1;
		displayString += "\nRhythm2: " + currSong.rhythm2;
		
		displayString += "\nTheme: " + currSong.theme;
		*/
		
		displayString += "\nMelody: " + currSong.melody;
		
		songStructureView.setText(displayString);
	}
	
	public void createMidiFile()
	{
		MidiManager midiManager = new MidiManager();
		midiSong = midiManager.generateChordMidi(currSong);
	}
	
	public void createTempMidi()
	{
		try {
			createMidiFile();
			File midiOut = new File(getFilesDir(), "tempOut.mid");
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
	
	public FileDescriptor retrieveTempMidi()
	{
		FileInputStream midiStream;
		try 
		{
			midiStream = openFileInput(getResources().getString(R.string.temp_midi));
			FileDescriptor fd = midiStream.getFD();
			return fd;

		}
		catch  (Exception e) 
		{ 
			showError(getResources().getString(R.string.error_read_MIDI));
			return null;
		}
	}
	
	public void saveMidi()
	{
		// make sure we can write to external storage:
	    String state = Environment.getExternalStorageState();
	    if (!Environment.MEDIA_MOUNTED.equals(state))
	    {
	    	// couldn't access external storage
	    	showError("Couldn't access external storage!");
	        return;
	    }

	    try {
		    File parentDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
		    if (!parentDir.exists())
		    	parentDir.mkdirs();
		    
		    File saveFile = new File(parentDir, saveFileName + ".mid");
		    if (saveFile.exists())
		    {
		    	// TODO: allow user to overwrite file or possible choose a different file name
		    	showError("Oops! File already exists... should probably let you save it anyway at some point... Oh well!");
		    	return;
		    }
		    
		    // it may already exist, but just be safe
		    createMidiFile();
		    midiSong.writeToFile(saveFile);
	    }
	    catch(Exception e) {
	    	showError(getResources().getString(R.string.error_create_MIDI));
	    }
	    
	}
	
	public void showError(String message)
	{
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, message, duration);
		toast.show();
	}
	
	public void showSaveDialog()
	{
   
	    DialogFragment saveDialog = new SaveFileDialogFragment();
	    saveDialog.show(getSupportFragmentManager(), "saveDialog");
	}

	@Override
	public void onSetFileName(SaveFileDialogFragment dialog){
		saveFileName = dialog.fileName;
		
	    // user hit cancel or didn't input anything
	    if (saveFileName.equals(""))
	    	return;
	    
	    saveMidi();
		
	}
	
	@Override
	public void onCancelSaveDialog(SaveFileDialogFragment dialog){
		saveFileName = "";
	}


}
