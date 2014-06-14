package com.blinz117.songgenerator;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.blinz117.fluiddroid.*;
import com.blinz117.fluiddroid.FluidDroidSynth.SongFinishedListener;
import com.blinz117.songbuilder.MidiGenerator;
import com.blinz117.songbuilder.SongWriter;
import com.blinz117.songbuilder.songstructure.*;
import com.blinz117.songbuilder.songstructure.MusicStructure.MidiInstrument;
import com.blinz117.songbuilder.songstructure.MusicStructure.Pitch;
import com.blinz117.songbuilder.songstructure.MusicStructure.ScaleType;
import com.blinz117.songgenerator.SaveFileDialogFragment.SaveFileDialogListener;
import com.blinz117.songgenerator.SongViewFragment.SongChangedListener;
import com.google.gson.Gson;

import com.leff.midi.*;

import android.os.Bundle;
import android.os.Environment;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

import android.support.v4.app.*;

public class MainActivity extends FragmentActivity implements OnItemSelectedListener, 
		SaveFileDialogListener, SongChangedListener, SongFinishedListener{

	boolean bIsPlaying;
	
	SongViewFragment songViewFrag;
	
//	Spinner timeSigNumSpin;
//	Spinner timeSigDenomSpin;
	Spinner tempoSpin;
	Spinner pitchSpin;
	Spinner modeSpin;
	Spinner insChordSpin;
	Spinner insMelodySpin;
	
	TextView timeSigValue;
	
//	EditText tempoValue;
	
	CheckBox tempoToggle;
	CheckBox keyToggle;
	CheckBox insChordToggle;
	CheckBox insMelodyToggle;
	
	Button songGenButton;
	//TextView songStructureView;
	Button playButton;
	
	SongWriter songWriter;
	Song currSong;
	
	MidiFile midiSong;
	
	String saveFileName;
	
	boolean needMIDIRefresh;
	
//	final List<Integer> timeSigNumVals = convertIntArray(MusicStructure.TIMESIGNUMVALUES);
//	final List<Integer> timeSigDenomVals = convertIntArray(MusicStructure.TIMESIGDENOMVALUES);
	
	final List<Integer> tempoVals = convertIntArray(SongWriter.bpmValues);
	
	/*
	 * State handlers
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		FragmentManager fm = getSupportFragmentManager();
		
		if (savedInstanceState == null)
		{
			FragmentTransaction fragTransaction = fm.beginTransaction();
			fragTransaction.add(new FluidDroidSynthFragment(), "SynthFragment");
			fragTransaction.commit();
		}
		bIsPlaying = false;
		
		songViewFrag = (SongViewFragment)fm.findFragmentById(R.id.songGridFragment);
		
		tempoToggle = (CheckBox)findViewById(R.id.randTempoToggle);
		tempoToggle.setChecked(true);
		
		keyToggle = (CheckBox)findViewById(R.id.randKeyToggle);
		keyToggle.setChecked(true);
		
		insChordToggle = (CheckBox)findViewById(R.id.randChordInsToggle);
		insChordToggle.setChecked(true);
		
		insMelodyToggle = (CheckBox)findViewById(R.id.randMelodyInsToggle);
		insMelodyToggle.setChecked(true);
		
		timeSigValue = (TextView)findViewById(R.id.timeSigVal);
		// Set up adapter and listener for spinners
//		timeSigNumSpin = (Spinner) findViewById(R.id.timeSigNumerSpinner);
//		initSpinnerFromList(timeSigNumSpin, timeSigNumVals);
//		
//		timeSigDenomSpin = (Spinner) findViewById(R.id.timeSigDenomSpinner);
//		initSpinnerFromList(timeSigDenomSpin, timeSigDenomVals);
		
		tempoSpin = (Spinner)findViewById(R.id.tempoSpinner);
		initSpinnerFromList(tempoSpin, tempoVals);
		
		pitchSpin = (Spinner) findViewById(R.id.pitchSpinner);
		initSpinnerFromArray(pitchSpin, MusicStructure.PITCHES);
		
		modeSpin = (Spinner) findViewById(R.id.modeSpinner);
		initSpinnerFromArray(modeSpin, MusicStructure.ScaleType.values());
		
		insChordSpin = (Spinner) findViewById(R.id.insChordSpinner);
		initSpinnerFromArray(insChordSpin, SongWriter.chordInstruments);
		
		insMelodySpin = (Spinner) findViewById(R.id.insMelodySpinner);
		initSpinnerFromArray(insMelodySpin, SongWriter.melodyInstruments);
		
//		tempoValue = (EditText)findViewById(R.id.tempoVal);
//		tempoValue.setText("120");
//		tempoValue.setClickable(false);
//		tempoValue.setFocusable(false);
		
		songGenButton = (Button)findViewById(R.id.songGen);
		songGenButton.setOnClickListener(onSongGenerate);
		//songStructureView = (TextView)findViewById(R.id.songStructure);
		
		playButton = (Button)findViewById(R.id.songPlay);
		playButton.setOnClickListener(onPlaySong);
		
		songWriter = new SongWriter();
		currSong = null;
		
		midiSong = null;
		
		saveFileName = "";
		
		needMIDIRefresh = false;
		
		syncControlSettings(false);
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) 
	{
		// Save state on certain changes, such as screen rotation
		// savedInstanceState.putCharSequence("DISPLAYTEXT", songStructureView.getText());
		Gson gson = new Gson();
		savedInstanceState.putString("SONG", gson.toJson(currSong));
		
		savedInstanceState.putBoolean("ISPLAYING", bIsPlaying);
		
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
		
		bIsPlaying = savedInstanceState.getBoolean("ISPLAYING");
		
		if (currSong != null)
		{
			updateDisplay();
			
			playButton.setEnabled(true);
			if (!bIsPlaying)
			{
				songGenButton.setEnabled(true);
				playButton.setText(getResources().getString(R.string.play_song));
			}
			else
			{
				songGenButton.setEnabled(false);
				playButton.setText(getResources().getString(R.string.stop_play));
			}
		}
		
		needMIDIRefresh = savedInstanceState.getBoolean("REFRESHMIDI");
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
	        case R.id.show_about:
	        	showAboutPage();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void showAboutPage()
	{
		Intent aboutIntent = new Intent(this, AboutActivity.class);
		startActivity(aboutIntent);
	}
	
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
			FluidDroidSynth synth = getSynth();
			if (synth == null)
				return;
			
			if (bIsPlaying)
			{
				// when the synth has stopped playing, we should receive a callback, at
				// which point we will set the UI to its proper state
				synth.stopPlaying();
				
				return;
			}
			
			// recreate the song in case user changed any parameters
			if (needMIDIRefresh)
			{
				createTempMidi();
				needMIDIRefresh = false;
			}
			
			String tempMidiPath = getFilesDir().getAbsolutePath() + "/" + getResources().getString(R.string.temp_midi);
			
			// Make sure MIDI player started correctly
			if (!synth.playMIDIFile(tempMidiPath))
			{
				Log.e("BRYAN", "Failed to start playing MIDI file!");
				return;
			}
			
			playButton.setText(getResources().getString(R.string.stop_play));
			songGenButton.setEnabled(false);
			
			bIsPlaying = true;
		}
	};
	
	private FluidDroidSynth getSynth()
	{
		FluidDroidSynthFragment synthFragment = (FluidDroidSynthFragment)getSupportFragmentManager().findFragmentByTag("SynthFragment");
		if (synthFragment == null)
		{
			Log.e("BRYAN", "Could not find synth fragment!");
			return null;
		}
		
		return synthFragment.mSynth;
	}
	
	public void onRandomToggleClicked(View view) {
		syncControlSettings(false);
	}
	
	private void syncControlSettings()
	{
		syncControlSettings(true);
	}
	
	// We have to be pretty careful about how we handle this, because it could get called at odd times
	// (such as when onItmeSelected gets called at weird times)
	private void syncControlSettings(boolean updateCurrSong)
	{
		updateCurrSong &= (currSong != null);
		
		Integer tempo = (Integer)tempoSpin.getSelectedItem();
    	Pitch key = (Pitch)pitchSpin.getSelectedItem();
    	ScaleType mode = (ScaleType)modeSpin.getSelectedItem();
		MidiInstrument insChord = (MidiInstrument)insChordSpin.getSelectedItem();
		MidiInstrument insMelody = (MidiInstrument)insMelodySpin.getSelectedItem();	
	    
		boolean bChanged = false;
		
		songWriter.setUseRandomTempo(tempoToggle.isChecked());
		if (tempo != songWriter.getTempo())
		{
			songWriter.setTempo(tempo);
			if (updateCurrSong)
			{
				currSong.tempo = tempo;
				bChanged = true;
			}
		}
			
	    songWriter.setUseRandomScale(keyToggle.isChecked());
	    if (key != songWriter.getKey())
	    {
			songWriter.setKey(key);
			if (updateCurrSong)
			{
				currSong.key = key;
				bChanged = true;
			}
	    }
	    if (mode != songWriter.getScaleType())
	    {
			songWriter.setScaleType(mode);
			if (updateCurrSong)
			{
				currSong.scaleType = mode;
				bChanged = true;
			}
	    }
	    
	    songWriter.setUseRandomChordInst(insChordToggle.isChecked());
	    if (insChord != songWriter.getChordInstrument())
	    {
			songWriter.setChordInstrument(insChord);
			if (updateCurrSong)
			{
				currSong.chordInstrument = insChord;
				bChanged = true;
			}
	    }
	    
	    songWriter.setUseRandomMelodyInst(insMelodyToggle.isChecked());
	    if (insMelody != songWriter.getMelodyInstrument())
	    {
			songWriter.setMelodyInstrument(insMelody);
			if (updateCurrSong)
			{
				currSong.melodyInstrument = insMelody;
				bChanged = true;
			}
	    }
	    
	    if (bChanged)
		{			
			songViewFrag.setSong(currSong);

	    	needMIDIRefresh = true;
		}
		
	}

	@Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
	{
		syncControlSettings();
    }

	@Override
    public void onNothingSelected(AdapterView<?> parent) {
		//Nothing to do as far as I can tell
	}
	
	/*
	 * Display/UI handling
	 */
	public void updateDisplay()
	{
		if (currSong == null)
			return;
		
//		String displayString = "";
//
//		displayString = displayString /*+ "\n"*/ + currSong.structure.toString();
//		
//		songStructureView.setText(displayString);
		
		// Set new control values
		timeSigValue.setText(currSong.timeSigNum + "/" + currSong.timeSigDenom);
//		setSpinnerValue(timeSigNumSpin, (Integer)currSong.timeSigNum);
//		
//		setSpinnerValue(timeSigDenomSpin, (Integer)currSong.timeSigDenom);
		
		setSpinnerValue(tempoSpin, currSong.tempo);
		
		setSpinnerValue(pitchSpin, currSong.key);
		
		setSpinnerValue(modeSpin, currSong.scaleType);
		
		setSpinnerValue(insChordSpin, currSong.chordInstrument);
		
		setSpinnerValue(insMelodySpin, currSong.melodyInstrument);
		
		syncControlSettings(false);
		
//		tempoValue.setText(currSong.tempo + "");
		
		songViewFrag.setSong(currSong);
	}
	
	public void showMessage(String message)
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
			showMessage(getResources().getString(R.string.error_read_MIDI));
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
	    	showMessage("Couldn't access external storage!");
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
		    	showMessage("Oops! File already exists... should probably let you save it anyway at some point... Oh well!");
		    	return;
		    }
		    
		    // Copy file from temporary saved file
		    FileInputStream midiStream = openFileInput(getResources().getString(R.string.temp_midi));
		    source = midiStream.getChannel();
		    outStream = new FileOutputStream(saveFile);
		    destination = outStream.getChannel();
	        destination.transferFrom(source, 0, source.size());
	        
	        showMessage("Exported " + saveFileName + ".mid to Music folder");
	    }
	    catch(Exception e) {
	    	showMessage(getResources().getString(R.string.error_create_MIDI));
	    }
	    finally {
	        if(source != null) {
	            try {
					source.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	        if(destination != null) {
	            try {
					destination.close();
				} catch (IOException e) {
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
		if (currSong == null)
		{
			showMessage("No song has been generated. Please generate a song before saving it.");
			return;
		}
		
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
	}
	
	protected <T> void initSpinnerFromArray(Spinner spinner, T[] array)
	{
		ArrayAdapter<T> newAdapter = new ArrayAdapter<T>(this, R.layout.default_spinner_item, array);
		newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(newAdapter);
		spinner.setOnItemSelectedListener(this);
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

	@Override
	public void onSongFinished() {
		bIsPlaying = false;
		
		songGenButton.setEnabled(true);
		
		playButton.setText(getResources().getString(R.string.play_song));
	}
}
