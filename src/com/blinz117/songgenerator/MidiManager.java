package com.blinz117.songgenerator;

import java.util.ArrayList;

import com.blinz117.songgenerator.songstructure.ChordProgression;
import com.blinz117.songgenerator.songstructure.MusicStructure;
import com.blinz117.songgenerator.songstructure.Song;
import com.blinz117.songgenerator.songstructure.MusicStructure.ScaleType;
import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
/*import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;*/
import com.leff.midi.event.ProgramChange;
import com.leff.midi.event.meta.KeySignature;
import com.leff.midi.event.meta.Tempo;
import com.leff.midi.event.meta.TimeSignature;

public class MidiManager {
	
	protected static int qtrNote = 480; // Still need to figure out why this value works... is it the resolution below?
	
	protected Song song;
	
	public MidiManager()
	{
		song = null;
	}
	
	public MidiFile generateChordMidi(Song newSong) {
		
		//int qtrNote = 480; // Still need to figure out why this value works... is it the resolution below?
		if (newSong == null)
		{
			return null;
		}
		
		song = newSong;
		
		ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
		// 1. Create some MidiTracks
		MidiTrack tempoTrack = new MidiTrack();
		MidiTrack melodyTrack = new MidiTrack();
		MidiTrack chordTrack = new MidiTrack();
		
		// 2. Add events to the tracks
		// 2a. Track 0 is typically the tempo map
		TimeSignature ts = new TimeSignature();
		ts.setTimeSignature(song.timeSigNum, song.timeSigDenom, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);
		
		Tempo t = new Tempo();
		t.setBpm(120);
		
		tempoTrack.insertEvent(ts);
		tempoTrack.insertEvent(t);
		
		// prepare the key signature
		int iMajor, iKey;
		boolean bMajor = song.scaleType == ScaleType.MAJOR;
		if (bMajor)
		{
			iMajor = KeySignature.SCALE_MAJOR;
			iKey = song.key.getMIDIKeyNumMajor();
		}
		else
		{
			iMajor = KeySignature.SCALE_MINOR;
			iKey = song.key.getMIDIKeyNumMinor();
		}
		KeySignature k = new KeySignature(0, 0, iKey, iMajor);
		tempoTrack.insertEvent(k);
		
		// Add instruments
		ProgramChange programGuitar = new ProgramChange(0, 0, song.chordInstrument.programNumber());
		chordTrack.insertEvent(programGuitar);
		
		ProgramChange programViolin = new ProgramChange(0, 1, song.melodyInstrument.programNumber());
		melodyTrack.insertEvent(programViolin);
		
		//TODO add chord progressions here:
		addChordProgression(melodyTrack, chordTrack, song.verseProgression);
		/*int channel = 0;
		int basePitch = song.key.getBaseMidiPitch();
		int velocity = 100;
		// TODO: BAD!!!! This is very dependent on only generating verse stuff. FIX IT!!!!
		ArrayList<Integer> chords = song.verseProgression;
		ArrayList<Integer> themeNotes = song.theme;
		for (int ndx = 0; ndx < chords.size(); ndx++)
		{
			int root = chords.get(ndx);
			int[] triad = MusicStructure.generateTriad(root, song.scaleType);
			
			for (int interval = 0; interval < 3; interval++)
			{
				chordTrack.insertNote(channel, basePitch + triad[interval], velocity, ndx * qtrNote * song.timeSigNum, qtrNote * song.timeSigNum);
			}
			
			for (int melodyNote = 0; melodyNote < themeNotes.size(); melodyNote++)
			{
				int timeStart = (ndx * qtrNote * timeSigNum) + (qtrNote * melodyNote);
				int pitch = basePitch + root + themeNotes.get(melodyNote) + 12;
				noteTrack.insertNote(channel + 1, pitch, velocity + 20, timeStart, qtrNote);
			}
			
			themeNotes = song.melody.get(ndx);
			for (int melodyNote = 0; melodyNote < themeNotes.size(); melodyNote++)
			{
				int timeStart = (ndx * qtrNote * song.timeSigNum) + (qtrNote * melodyNote);
				int pitch = basePitch + MusicStructure.getScaleIntervals(song.scaleType)[(root + themeNotes.get(melodyNote)) % 7];// + 12;
				melodyTrack.insertNote(channel + 1, pitch, velocity + 20, timeStart, qtrNote);
			}

		}*/
		
		// It's best not to manually insert EndOfTrack events; MidiTrack will
		// call closeTrack() on itself before writing itself to a file
		
		// 3. Create a MidiFile with the tracks we created
		//ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
		tracks.add(tempoTrack);
		tracks.add(melodyTrack);
		tracks.add(chordTrack);
		
		MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);
		
		return midi;
	}
	
	public void addChordProgression(MidiTrack melodyTrack, MidiTrack chordTrack, ChordProgression progression)
	{
		int channel = 0;
		int basePitch = song.key.getBaseMidiPitch();
		int velocity = 100;
		
		int chordTick = 0;
		int melodyTick = 0;
		
		ArrayList<Integer> themeNotes = song.theme;
		ArrayList<Integer> chords = progression.getChords();
		
		for (int ndx = 0; ndx < chords.size(); ndx++)
		{
			int root = chords.get(ndx);
			int[] triad = MusicStructure.generateTriad(root, song.scaleType);
			
			for (int interval = 0; interval < triad.length; interval++)
			{
				chordTrack.insertNote(channel, basePitch + triad[interval], velocity, chordTick /*ndx * qtrNote * song.timeSigNum*/, qtrNote * song.timeSigNum);
			}
			chordTick += qtrNote * song.timeSigNum;
			/*for (int melodyNote = 0; melodyNote < themeNotes.size(); melodyNote++)
			{
				int timeStart = (ndx * qtrNote * timeSigNum) + (qtrNote * melodyNote);
				int pitch = basePitch + root + themeNotes.get(melodyNote) + 12;
				noteTrack.insertNote(channel + 1, pitch, velocity + 20, timeStart, qtrNote);
			}*/
			
			themeNotes = progression.getMelody().get(ndx); //song.melody.get(ndx);
			for (int melodyNote = 0; melodyNote < themeNotes.size(); melodyNote++)
			{
				//int timeStart = (ndx * qtrNote * song.timeSigNum) + (qtrNote * melodyNote);
				int pitch = basePitch + MusicStructure.getScaleIntervals(song.scaleType)[(root + themeNotes.get(melodyNote)) % 7];// + 12;
				melodyTrack.insertNote(channel + 1, pitch, velocity + 20, melodyTick/*timeStart*/, qtrNote);
				melodyTick += qtrNote;
			}

		}
	}
	
	public void addMelody(MidiTrack melodyTrack, ChordProgression progression)
	{
		int channel = 0;
		int basePitch = song.key.getBaseMidiPitch();
		int velocity = 100;
		
		int melodyTick = 0;
		
		ArrayList<Integer> themeNotes = song.theme;
		ArrayList<Integer> chords = progression.getChords();
		
		for (int ndx = 0; ndx < chords.size(); ndx++)
		{
			int root = chords.get(ndx);
			/*for (int melodyNote = 0; melodyNote < themeNotes.size(); melodyNote++)
			{
				int timeStart = (ndx * qtrNote * timeSigNum) + (qtrNote * melodyNote);
				int pitch = basePitch + root + themeNotes.get(melodyNote) + 12;
				noteTrack.insertNote(channel + 1, pitch, velocity + 20, timeStart, qtrNote);
			}*/
			
			themeNotes = song.melody.get(ndx);
			for (int melodyNote = 0; melodyNote < themeNotes.size(); melodyNote++)
			{
				//int timeStart = (ndx * qtrNote * song.timeSigNum) + (qtrNote * melodyNote);
				int pitch = basePitch + MusicStructure.getScaleIntervals(song.scaleType)[(root + themeNotes.get(melodyNote)) % 7];// + 12;
				melodyTrack.insertNote(channel + 1, pitch, velocity + 20, melodyTick/*timeStart*/, qtrNote);
				melodyTick += qtrNote;
			}

		}
	}
	
	/*	public MidiFile generateTempMidi(Song song) {
	
	int eigthNote = 240; // Still need to figure out why this value works... is it the resolution below?
	
	// 1. Create some MidiTracks
	MidiTrack tempoTrack = new MidiTrack();
	MidiTrack noteTrack = new MidiTrack();
	
	// 2. Add events to the tracks
	// 2a. Track 0 is typically the tempo map
	TimeSignature ts = new TimeSignature();
	ts.setTimeSignature(song.timeSigNum, song.timeSigDenom, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);
	
	Tempo t = new Tempo();
	t.setBpm(120);
	
	tempoTrack.insertEvent(ts);
	tempoTrack.insertEvent(t);
	
	int channel = 0;
	int pitch = 60;
	int currBeat = 0;
	ArrayList<Integer> rhythm1 = song.rhythm1;
	for (int ndx = 0; ndx < rhythm1.size(); ndx++)
	{
		int velocity = 100;
		int numEigthNotes = rhythm1.get(ndx);
		// handle rests
		if (numEigthNotes < 0)
		{
			numEigthNotes *= -1;
			velocity = 0;
		}
		
		noteTrack.insertNote(channel, pitch, velocity, currBeat * eigthNote, eigthNote * numEigthNotes);
		currBeat += numEigthNotes;
	}
	
	ArrayList<Integer> rhythm2 = song.rhythm2;
	for (int ndx = 0; ndx < rhythm1.size(); ndx++)
	{
		int velocity = 100;
		int numEigthNotes = rhythm2.get(ndx);
		// handle rests
		if (numEigthNotes < 0)
		{
			numEigthNotes *= -1;
			velocity = 0;
		}
		
		noteTrack.insertNote(channel, pitch, velocity, currBeat * eigthNote, eigthNote * numEigthNotes);
		currBeat += numEigthNotes;
	}
	// It's best not to manually insert EndOfTrack events; MidiTrack will
	// call closeTrack() on itself before writing itself to a file
	
	// 3. Create a MidiFile with the tracks we created
	ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
	tracks.add(tempoTrack);
	tracks.add(noteTrack);
	
	MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);
	
	return midi;
}*/
	
/*	public MidiFile example()
	{
		
		// 1. Create some MidiTracks
		MidiTrack tempoTrack = new MidiTrack();
		MidiTrack noteTrack = new MidiTrack();
		MidiTrack bassTrack = new MidiTrack();
		
		// 2. Add events to the tracks
		// 2a. Track 0 is typically the tempo map
		TimeSignature ts = new TimeSignature();
		ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);
		
		Tempo t = new Tempo();
		t.setBpm(60);
		
		tempoTrack.insertEvent(ts);
		tempoTrack.insertEvent(t);
		
		// 2b. Track 1 will have some notes in it
		for(int i = 0; i < 20; i++) {
			
			int channel = 0, pitch = 41 + i, velocity = 100;
			NoteOn on = new NoteOn(i*480, channel, pitch, velocity);
			NoteOff off = new NoteOff(i*480 + 120, channel, pitch, 0);
			
			noteTrack.insertEvent(on);
			noteTrack.insertEvent(off);
			
			// There is also a utility function for notes that you should use instead of the above.
			noteTrack.insertNote(channel, pitch + 2, velocity, i*480, 120);
			
			bassTrack.insertNote(channel, pitch - 10, velocity, i*480 + 125, 115);
		}
		
		// It's best not to manually insert EndOfTrack events; MidiTrack will
		// call closeTrack() on itself before writing itself to a file
		
		// 3. Create a MidiFile with the tracks we created
		ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
		tracks.add(tempoTrack);
		tracks.add(noteTrack);
		tracks.add(bassTrack);
		
		MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);
		
		return midi;
		
	}*/
}
