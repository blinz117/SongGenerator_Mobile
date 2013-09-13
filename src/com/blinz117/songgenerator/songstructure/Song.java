package com.blinz117.songgenerator.songstructure;

import java.util.ArrayList;

import com.blinz117.songgenerator.songstructure.MusicStructure.*;
import com.leff.midi.event.ProgramChange.MidiProgram;

public class Song
{
	public int timeSigNum;
	public int timeSigDenom;
	public int tempo;
	
	public ScaleType scaleType;
	public Pitch key;
	
	public MidiProgram chordInstrument;
	public MidiProgram melodyInstrument;
	
	public ArrayList<SongPart> structure;
	
	public ChordProgression verseProgression;
	public ChordProgression chorusProgression;
	public ChordProgression bridgeProgression;
	
	public ArrayList<Integer> verseChordRhythm;
	public ArrayList<Integer> chorusChordRhythm;
	
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
		
		verseChordRhythm = null;
		chorusChordRhythm = null;
		
		theme = null;
		
		melody = null;
	}
		
} //class Song
