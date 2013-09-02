package com.blinz117.songgenerator.songstructure;

import java.util.ArrayList;

public class ChordProgression {
	
	public ArrayList<Pattern> patterns;
	
	public ChordProgression()
	{
		patterns = new ArrayList<Pattern>();
	}
	
	public ChordProgression(ChordProgression inst)
	{
		patterns = inst.patterns;
	}
	
	public ArrayList<Integer> getChords()
	{
		ArrayList<Integer> chords = new ArrayList<Integer>();
		for (Pattern pattern: patterns)
		{
			chords.addAll(pattern.chords);
		}
		return chords;
	}
	
	public ArrayList<ArrayList<Integer>> getMelody()
	{
		ArrayList<ArrayList<Integer>> melody = new ArrayList<ArrayList<Integer>>();
		for (Pattern pattern: patterns)
		{
			melody.addAll(pattern.melody);
		}
		return melody;
	}

}
