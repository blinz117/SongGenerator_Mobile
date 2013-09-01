package com.blinz117.songgenerator.songstructure;

import java.util.ArrayList;

public class ChordProgression {
	
	public ArrayList<ChordPattern> patterns;
	
	public ChordProgression()
	{
		patterns = new ArrayList<ChordPattern>();
	}
	
	public ChordProgression(ChordProgression inst)
	{
		patterns = inst.patterns;
	}
	
	public ArrayList<Integer> getChords()
	{
		ArrayList<Integer> chords = new ArrayList<Integer>();
		for (ChordPattern pattern: patterns)
		{
			chords.addAll(pattern.chords);
		}
		return chords;
	}

}
