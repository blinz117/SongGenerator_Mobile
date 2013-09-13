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
		patterns = new ArrayList<Pattern>(inst.patterns);
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
	
	public ArrayList<ArrayList<Note>> getNotes()
	{
		ArrayList<ArrayList<Note>> notes = new ArrayList<ArrayList<Note>>();
		for (Pattern pattern: patterns)
		{
			notes.addAll(pattern.notes);
		}
		return notes;
	}
	
	public ChordProgression plus(ChordProgression addend)
	{
		ChordProgression result = new ChordProgression(this);
		result.patterns.addAll(addend.patterns);
		return result;
	}

}
