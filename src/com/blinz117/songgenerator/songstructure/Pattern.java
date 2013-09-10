package com.blinz117.songgenerator.songstructure;

import java.util.ArrayList;

public class Pattern {
	
	// This class is for small subsections of a chord progression. They are often repeated
	// during a chord progression, so that is why they are broken up
	
	public ArrayList<Integer> chords;
	public ArrayList<ArrayList<Integer>> melody;
	public ArrayList<ArrayList<Note>> notes;
	
	public Pattern()
	{
		chords = new ArrayList<Integer>();
		melody = new ArrayList<ArrayList<Integer>>();
		notes = new ArrayList<ArrayList<Note>>();
	}
	
	public Pattern(Pattern inst)
	{
		chords = inst.chords;
		melody = inst.melody;
		notes = inst.notes;
	}
	
	public ArrayList<Integer> getChords() {return chords;}
	
	public ArrayList<ArrayList<Integer>> getMelody() { return melody; }
	
	public ArrayList<ArrayList<Note>> getNotes() { return notes; }
	
	public Pattern plus(Pattern addend)
	{
		Pattern result = new Pattern(this);
		result.chords.addAll(addend.chords);
		result.melody.addAll(addend.melody);
		result.notes.addAll(addend.notes);
		return result;
	}

}
