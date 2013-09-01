package com.blinz117.songgenerator.songstructure;

import java.util.ArrayList;

public class ChordPattern {
	
	// This class is for small subsections of a chord progression. They are often repeated
	// during a chord progression, so that is why they are broken up
	
	public ArrayList<Integer> chords;
	
	public ChordPattern()
	{
		chords = new ArrayList<Integer>();
	}
	
	public ChordPattern(ChordPattern inst)
	{
		chords = inst.chords;
	}
	
	public ArrayList<Integer> getChords()
	{
		return chords;
	}

}
