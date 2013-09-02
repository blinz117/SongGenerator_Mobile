package com.blinz117.songgenerator.songstructure;

import java.util.ArrayList;

public class Pattern {
	
	// This class is for small subsections of a chord progression. They are often repeated
	// during a chord progression, so that is why they are broken up
	
	public ArrayList<Integer> chords;
	public ArrayList<ArrayList<Integer>> melody;
	
	public Pattern()
	{
		chords = new ArrayList<Integer>();
		melody = new ArrayList<ArrayList<Integer>>();
	}
	
	public Pattern(Pattern inst)
	{
		chords = inst.chords;
		melody = inst.melody;
	}
	
	public ArrayList<Integer> getChords()
	{
		return chords;
	}
	
	public ArrayList<ArrayList<Integer>> getMelody()
	{
		return melody;
	}

}
