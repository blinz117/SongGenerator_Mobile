package com.blinz117.songgenerator.songstructure;

import java.util.ArrayList;

import com.blinz117.songgenerator.songstructure.MusicStructure.*;
import com.leff.midi.event.ProgramChange.MidiProgram;

public class Song
{
	public int timeSigNum;
	public int timeSigDenom;
	
	public ScaleType scaleType;
	public Pitch key;
	
	public MidiProgram chordInstrument;
	public MidiProgram melodyInstrument;
	
	public ArrayList<SongPart> structure;
	
	public ChordProgression verseProgression;
	public ChordProgression chorusProgression;
	public ChordProgression bridgeProgression;
	
	public ArrayList<Integer> rhythm1;
	public ArrayList<Integer> rhythm2;
	
	public ArrayList<Integer> theme;
	
	public ArrayList<ArrayList<Integer>> melody;
	
	public Song(){		
		timeSigNum = 0;
		timeSigDenom = 0;
		
		scaleType = ScaleType.MAJOR;
		key = Pitch.C;
		
		chordInstrument = MidiProgram.ACOUSTIC_GRAND_PIANO;
		melodyInstrument = MidiProgram.ACOUSTIC_GRAND_PIANO;
		
		structure = null;
		
		verseProgression = null;
		chorusProgression = null;
		bridgeProgression = null;
		
		rhythm1 = null;
		rhythm2 = null;
		
		theme = null;
		
		melody = null;
	}
	
	public class Chord
	{
		int scaleDegree;
		// TODO: add an enum for modifier?
		// Otherwise, I think scale degree will be the only value, so having a class
		// seems like overkill (unless I can think of something else that makes sense...
		// perhaps inversion?
	}
	
	public class Rhythm
	{
		
	}
		
} //class Song
