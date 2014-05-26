package com.blinz117.songgenerator;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import com.blinz117.fluiddroid.*;
import com.blinz117.songbuilder.MidiGenerator;
import com.blinz117.songbuilder.SongWriter;
import com.blinz117.songbuilder.songstructure.*;
import com.blinz117.songbuilder.songstructure.MusicStructure.Pitch;
import com.blinz117.songbuilder.songstructure.MusicStructure.ScaleType;
import com.google.gson.Gson;

import com.leff.midi.*;
import com.leff.midi.event.ProgramChange.MidiProgram;

import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.content.Context;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

import android.support.v4.app.*;

public class MainActivity extends FragmentActivity implements OnItemSelectedListener, 
		OnCompletionListener, SaveFileDialogFragment.SaveFileDialogListener, SongViewFragment.SongChangedListener{

	FluidDroidSynth synth;
	boolean bIsPlaying;
	
	SongViewFragment songViewFrag;
	
	Spinner timeSigNumSpin;
	Spinner timeSigDenomSpin;
	Spinner pitchSpin;
	Spinner modeSpin;
	Spinner insChordSpin;
	Spinner insMelodySpin;
	
	EditText tempoValue;
	
	ToggleButton keyToggle;
	ToggleButton insChordToggle;
	ToggleButton insMelodyToggle;
	//boolean useRandKey;
	//boolean useRandChordIns;
	//boolean useRandMelodyIns;
	
	Button songGenButton;
	TextView songStructureView;
	Button playButton;
	//Button saveButton;
	
	SongWriter songWriter;
	Song currSong;
	
	MidiFile midiSong;
	
	AudioManager am;
	MediaPlayer mediaPlayer;
	
	String saveFileName;
	
	boolean needMIDIRefresh;
	
	final List<Integer> timeSigNumVals = convertIntArray(MusicStructure.TIMESIGNUMVALUES);
	final List<Integer> timeSigDenomVals = convertIntArray(MusicStructure.TIMESIGDENOMVALUES);
	
	/*
	 * State handlers
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		synth = new FluidDroidSynth();
		//synth.startSynth();
		bIsPlaying = false;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		FragmentManager fm = getSupportFragmentManager();
		songViewFrag = (SongViewFragment)fm.findFragmentById(R.id.songGridFragment);
		
		keyToggle = (ToggleButton)findViewById(R.id.randKeyToggle);
		keyToggle.setChecked(true);
		
		insChordToggle = (ToggleButton)findViewById(R.id.randChordInsToggle);
		insChordToggle.setChecked(true);
		
		insMelodyToggle = (ToggleButton)findViewById(R.id.randMelodyInsToggle);
		insMelodyToggle.setChecked(true);
		
		// Set up adapter and listener for spinners
		timeSigNumSpin = (Spinner) findViewById(R.id.timeSigNumerSpinner);
		initSpinnerFromList(timeSigNumSpin, timeSigNumVals);
		
		timeSigDenomSpin = (Spinner) findViewById(R.id.timeSigDenomSpinner);
		initSpinnerFromList(timeSigDenomSpin, timeSigDenomVals);
		
		pitchSpin = (Spinner) findViewById(R.id.pitchSpinner);
		initSpinnerFromArray(pitchSpin, MusicStructure.PITCHES);
		
		modeSpin = (Spinner) findViewById(R.id.modeSpinner);
		initSpinnerFromArray(modeSpin, MusicStructure.ScaleType.values());
		
		insChordSpin = (Spinner) findViewById(R.id.insChordSpinner);
		initSpinnerFromArray(insChordSpin, SongWriter.chordInstruments);
		
		insMelodySpin = (Spinner) findViewById(R.id.insMelodySpinner);
		initSpinnerFromArray(insMelodySpin, SongWriter.melodyInstruments);
		
		tempoValue = (EditText)findViewById(R.id.tempoVal);
		tempoValue.setText("120");
		tempoValue.setClickable(false);
		tempoValue.setFocusable(false);
		
		songGenButton = (Button)findViewById(R.id.songGen);
		songGenButton.setOnClickListener(onSongGenerate);
		songStructureView = (TextView)findViewById(R.id.songStructure);
		
		playButton = (Button)findViewById(R.id.songPlay);
		playButton.setOnClickListener(onPlaySong);
		
//		saveButton = (Button)findViewById(R.id.songSave);
//		saveButton.setOnClickListener(new View.OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				showSaveDialog();
//			}
//		});
		
		songWriter = new SongWriter();
		currSong = null;
		
		midiSong = null;
		
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(this);
		
		saveFileName = "";
		
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		needMIDIRefresh = false;
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		
		synth.destroy();
	}

	// temporary versions that just save and restore the display text. The
	// tempMidi file should already be saved,
	// so you should still be able to play

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) 
	{
		// Save state on certain changes, such as screen rotation
		// savedInstanceState.putCharSequence("DISPLAYTEXT", songStructureView.getText());
		Gson gson = new Gson();
		savedInstanceState.putString("SONG", gson.toJson(currSong));
		
		savedInstanceState.putBoolean("REFRESHMIDI", needMIDIRefresh);

		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) 
	{
		// Restore state on certain changes, such as screen rotation
		super.onRestoreInstanceState(savedInstanceState);

		String json = savedInstanceState.getString("SONG");
		
		Gson gson = new Gson();
		currSong = gson.fromJson(json, Song.class);
		
		if (currSong != null)
		{
			updateDisplay();
			songGenButton.setEnabled(true);
//			saveButton.setEnabled(true);
			playButton.setEnabled(true);
			playButton.setText(getResources().getString(R.string.play_song));
		}
		
		needMIDIRefresh = savedInstanceState.getBoolean("REFRESHMIDI");
		
		// CharSequence displayText = savedInstanceState.getCharSequence("DISPLAYTEXT");
		// songStructureView.setText(savedInstanceState.getCharSequence("DISPLAYTEXT"));

		// if(displayText.length() > 0)
		// {
		// songStructureView.setText(displayText);
		// songGenButton.setEnabled(true);
		// saveButton.setEnabled(true);
		// playButton.setEnabled(true);
		// playButton.setText(getResources().getString(R.string.play_song));
		// }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_save:
	        	showSaveDialog();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	View.OnClickListener onSongGenerate = new View.OnClickListener() {
		public void onClick(View view)
		{
			currSong = songWriter.writeNewSong();
			updateDisplay();
			createTempMidi();
			playButton.setEnabled(true);
//			saveButton.setEnabled(true);
		}
	};
	
	View.OnClickListener onPlaySong = new View.OnClickListener() {
		public void onClick(View view)
		{			
			if (bIsPlaying)
			{
				synth.stopPlaying();
				bIsPlaying = false;
				
//				mediaPlayer.stop();
//				onCompletion(mediaPlayer);
				
				songGenButton.setEnabled(true);
//				saveButton.setEnabled(true);
				//playButton.setEnabled(true);
				playButton.setText(getResources().getString(R.string.play_song));
				
				return;
			}
			
			// recreate the song in case user changed any parameters
			if (needMIDIRefresh)
			{
				createTempMidi();
				needMIDIRefresh = false;
			}
			
			String tempMidiPath = getFilesDir().getAbsolutePath() + "/" + getResources().getString(R.string.temp_midi);
			synth.playMIDIFile(tempMidiPath);
			playButton.setText(getResources().getString(R.string.stop_play));
//			saveButton.setEnabled(false);
			songGenButton.setEnabled(false);
			
			bIsPlaying = true;
			
			
//			FileInputStream midiStream;
//			try 
//			{
//				midiStream = openFileInput(getResources().getString(R.string.temp_midi));
//				FileDescriptor fd = midiStream.getFD();
//				mediaPlayer.setDataSource( fd );
//				mediaPlayer.prepare();
//				
//				// now that MediaPlayer is ready, request audio focus
//				// Request audio focus for playback
//				int result = am.requestAudioFocus(afChangeListener,
//				                                 // Use the music stream.
//				                                 AudioManager.STREAM_MUSIC,
//				                                 // Request permanent focus.
//				                                 AudioManager.AUDIOFOCUS_GAIN);
//				   
//				if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
//				{
//					showError("Could not gain audio focus!");
//					return;
//				}
//				
//				mediaPlayer.start();
//				
//				playButton.setText(getResources().getString(R.string.stop_play));
//				saveButton.setEnabled(false);
//				songGenButton.setEnabled(false);
//			}
//			catch (Exception e) 
//			{ 
////				Context context = getApplicationContext();
//				String errText = getResources().getString(R.string.error_read_MIDI);//"Oops! Something bad happened trying to find your MIDI file! Here's the message: " + e.getMessage();
////				int duration = Toast.LENGTH_SHORT;
////
////				Toast toast = Toast.makeText(context, text, duration);
////				toast.show();
//				showError(errText);
//			}
			
		}
	};
	
	OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
	    public void onAudioFocusChange(int focusChange) {
	        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
	        {
	            if (mediaPlayer.isPlaying()) mediaPlayer.pause();
	        } 
	        else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) 
	        {
	            mediaPlayer.start();
	        } 
	        else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) 
	        {
	        	if (mediaPlayer.isPlaying()) mediaPlayer.stop();
	        	onCompletion(mediaPlayer);
	        }
	    }
	};
	
//	public void onKeyToggleClicked(View view) {
//	    boolean useRandKey = ((ToggleButton) view).isChecked();
//	    //pitchSpin.setClickable(!useRandKey);
//	    //modeSpin.setClickable(!useRandKey);
//	    
//	    if (useRandKey)
//	    {
//		    songWriter.setKey(null);
//		    songWriter.setScaleType(null);
//	    }
//	    else
//	    {
//	    	Pitch key = (Pitch)pitchSpin.getSelectedItem();
//	    	ScaleType mode = (ScaleType)modeSpin.getSelectedItem();
//	    	
//			if (currSong != null)
//			{
//				currSong.key = key;
//				currSong.scaleType = mode;
//			}
//		    songWriter.setKey(key);
//		    songWriter.setScaleType(mode);
//	    }
//	}
//	
//	public void onChordInsToggleClicked(View view) {
//	    boolean useRandChordIns = ((ToggleButton) view).isChecked();
//	    //insChordSpin.setClickable(!useRandChordIns);
//	    
//	    if (useRandChordIns)
//	    	songWriter.setChordInstrument(null);
//	}
//	
//	public void onMelodyInsToggleClicked(View view) {
//		boolean useRandMelodyIns = ((ToggleButton) view).isChecked();
//		//insMelodySpin.setClickable(!useRandMelodyIns);
//		
//		if (useRandMelodyIns)
//			songWriter.setMelodyInstrument(null);
//	}
	
	public void onRandomToggleClicked(View view) {
		syncControlSettings(false);
	}
	
	private void syncControlSettings()
	{
		syncControlSettings(true);
	}
	
	private void syncControlSettings(boolean updateCurrSong)
	{
		updateCurrSong &= (currSong != null);
		
    	Pitch key = (Pitch)pitchSpin.getSelectedItem();
    	ScaleType mode = (ScaleType)modeSpin.getSelectedItem();
		MidiProgram insChord = (MidiProgram)insChordSpin.getSelectedItem();
		MidiProgram insMelody = (MidiProgram)insMelodySpin.getSelectedItem();	
	    
	    if (keyToggle.isChecked())
	    {
		    songWriter.setKey(null);
		    songWriter.setScaleType(null);
	    }
	    else
	    {
			songWriter.setKey(key);
			songWriter.setScaleType(mode);
	    }
	    
	    if (insChordToggle.isChecked())
	    	songWriter.setChordInstrument(null);
	    else
			songWriter.setChordInstrument(insChord);
	    
	    if (insMelodyToggle.isChecked())
	    	songWriter.setMelodyInstrument(null);
	    else
			songWriter.setMelodyInstrument(insMelody);
	    
	    if (updateCurrSong)
		{
			currSong.key = key;
			currSong.scaleType = mode;
			currSong.chordInstrument = insChord;
			currSong.melodyInstrument = insMelody;

	    	needMIDIRefresh = true;
		}
		
	}

	@Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
	{
		syncControlSettings();
/*		
 * 		Removing this temporarily. Just randomly generate until I add more customization later
 * 
 * 		Spinner spinner = (Spinner) parent;
		String value = parent.getItemAtPosition(pos).toString();
		if (spinner.getId() == R.id.timeSigNum)
			songWriter.setTimeSigNumerator(Integer.parseInt(value));
		else if (spinner.getId() == R.id.timeSigDenom)
			songWriter.setTimeSigDenominator(Integer.parseInt(value));*/
		
		// Update both the current song in case the user plays it again and the
		// SongWriter for writing another song
//		boolean updateSong = (currSong != null);
//		boolean modified = false;
//		
//		Spinner spinner = (Spinner) parent;
//		Object value = parent.getItemAtPosition(pos);//.toString();
//		if (spinner.getId() == R.id.pitchSpinner)
//		{
//			MusicStructure.Pitch key = (MusicStructure.Pitch)value;
//			if (updateSong)
//				currSong.key = key;
//			
//			// doing random right now
//			if (!keyToggle.isChecked() && key != songWriter.getKey())
//			{
//				songWriter.setKey(key);
//				modified = true;
//			}
//		}
//		else if (spinner.getId() == R.id.modeSpinner)
//		{
//			MusicStructure.ScaleType mode = (MusicStructure.ScaleType)value;
//			if (updateSong)
//				currSong.scaleType = mode;
//			
//			// doing random right now
//			if (keyToggle.isChecked() && mode != songWriter.getScaleType())
//			{
//				songWriter.setScaleType(mode);
//				modified = true;
//			}
//		}
//		else if (spinner.getId() == R.id.insChordSpinner)
//		{			
//			MidiProgram ins = (MidiProgram)value;
//			if (updateSong)
//				currSong.chordInstrument = ins;
//			
//			if (insChordToggle.isChecked() && ins != songWriter.getChordInstrument())
//			{
//				songWriter.setChordInstrument(ins);
//				modified = true;
//			}
//		}
//		else if (spinner.getId() == R.id.insMelodySpinner)
//		{			
//			MidiProgram ins = (MidiProgram)value;
//			if (updateSong)
//				currSong.melodyInstrument = ins;
//			
//			if (insMelodyToggle.isChecked() && ins != songWriter.getMelodyInstrument())
//			{
//				songWriter.setMelodyInstrument(ins);
//				modified = true;
//			}
//		}
//		else
//			return;
//		
//		if (updateSong && modified)
//			needMIDIRefresh = true;
    }

	@Override
    public void onNothingSelected(AdapterView<?> parent) {
		//Nothing to do as far as I can tell
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// Clean up when mediaPlayer stops/is stopped
		
		// Abandon audio focus   
		am.abandonAudioFocus(afChangeListener);
		
		songGenButton.setEnabled(true);
//		saveButton.setEnabled(true);
		//playButton.setEnabled(true);
		playButton.setText(getResources().getString(R.string.play_song));
		
		mp.reset();
	}
	
	/*
	 * Display/UI handling
	 */
	public void updateDisplay()
	{
		if (currSong == null)
			return;
		
		String displayString = "";
//		displayString += "Time Signature: ";
//		displayString += currSong.timeSigNum + "/" + currSong.timeSigDenom;
//		displayString = displayString + "\nTempo: " + currSong.tempo + " BPM";
//		displayString = displayString + "\nChord instrument: " + currSong.chordInstrument;
//		displayString = displayString + "\nMelody instrument: " + currSong.melodyInstrument;
//		displayString += "\nScale: " + currSong.key.toString() + " " + currSong.scaleType;
		displayString = displayString /*+ "\n"*/ + currSong.structure.toString();
		//displayString = displayString + "\nVerse: " + currSong.verseProgression.getChords();
		//displayString = displayString + "\nChorus: " + currSong.chorusProgression.getChords();
		//displayString = displayString + "\nBridge: " + currSong.bridgeProgression.getChords();
		
		/* Don't want to show these for now... 
		displayString += "\nRhythm1: " + currSong.rhythm1;
		displayString += "\nRhythm2: " + currSong.rhythm2;
		
		displayString += "\nTheme: " + currSong.theme;
		*/
		
		//displayString += "\nMelody: " + currSong.melody;
		
		//displayString += "\nVerse Notes: " + currSong.verseProgression.getNotes();//.melody;
		//displayString += "\nChorus Notes: " + currSong.chorusProgression.getNotes();
		
		songStructureView.setText(displayString);
		
		// Set new control values
		setSpinnerValue(timeSigNumSpin, (Integer)currSong.timeSigNum);
		
		setSpinnerValue(timeSigDenomSpin, (Integer)currSong.timeSigDenom);
		
		setSpinnerValue(pitchSpin, currSong.key);
		
		setSpinnerValue(modeSpin, currSong.scaleType);
		
		setSpinnerValue(insChordSpin, currSong.chordInstrument);
		
		setSpinnerValue(insMelodySpin, currSong.melodyInstrument);
		
		tempoValue.setText(currSong.tempo + "");
		
		songViewFrag.setSong(currSong);
	}
	
	public void showError(String message)
	{
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, message, duration);
		toast.show();
	}
	
	/*
	 * Midi file handling
	 */
	public void createMidiFile()
	{
		MidiGenerator midiManager = new MidiGenerator();
		midiSong = midiManager.generateChordMidi(currSong);
	}
	
	public void createTempMidi()
	{
		try {
			createMidiFile();
			File midiOut = new File(getFilesDir(), getResources().getString(R.string.temp_midi));
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

	    FileChannel source = null;
	    FileChannel destination = null;
	    FileOutputStream outStream = null;
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
		    
		    // Copy file from temporary saved file
		    FileInputStream midiStream = openFileInput(getResources().getString(R.string.temp_midi));
		    source = midiStream.getChannel();
		    outStream = new FileOutputStream(saveFile);
		    destination = outStream.getChannel();
	        destination.transferFrom(source, 0, source.size());

		    /*
		    // it may already exist, but just be safe
		    createMidiFile();
		    midiSong.writeToFile(saveFile);
		    */
	    }
	    catch(Exception e) {
	    	showError(getResources().getString(R.string.error_create_MIDI));
	    }
	    finally {
	        if(source != null) {
	            try {
					source.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        if(destination != null) {
	            try {
					destination.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    }
	    
	}
	
	/*
	 * Dialog interaction
	 */
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
	
	//UTILS:
	// @TODO: Put these in their own class at some point
	public List<Integer> convertIntArray(int[] src)
	{
		List<Integer> dest = new ArrayList<Integer>();
		
		for (int val : src)
		{
			dest.add((Integer)val);
		}
		
		return dest;
	}
	
	

	protected <T> void initSpinnerFromList(Spinner spinner, List<T> list)
	{
		ArrayAdapter<T> newAdapter = new ArrayAdapter<T>(this, R.layout.default_spinner_item, list);
		newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(newAdapter);
		spinner.setOnItemSelectedListener(this);
		//spinner.setClickable(false);
	}
	
	protected <T> void initSpinnerFromArray(Spinner spinner, T[] array)
	{
		ArrayAdapter<T> newAdapter = new ArrayAdapter<T>(this, R.layout.default_spinner_item, array);
		newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(newAdapter);
		spinner.setOnItemSelectedListener(this);
		//spinner.setClickable(false);
	}
	
	protected boolean setSpinnerValue(Spinner spinner, Object value)
	{
		boolean bFound = false;
		ArrayAdapter<Object> adapter = (ArrayAdapter<Object>)spinner.getAdapter();
		int ndx = adapter.getPosition(value);
		if (ndx >= 0)
		{
			spinner.setSelection(ndx, false);
			bFound = true;
		}
		
		return bFound;
	}

	@Override
	public void onSongChanged(Song newSong) {
		currSong = newSong;
		needMIDIRefresh = true;
		updateDisplay();
	}
}
