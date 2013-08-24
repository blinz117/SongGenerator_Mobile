package com.blinz117.songgenerator;

import java.util.ArrayList;

import com.blinz117.songgenerator.SongStructure.*;

public class Song
{
	int timeSigNum;
	int timeSigDenom;
	
	ScaleType scaleType;
	
	ArrayList<SongPart> vStructure;
	
	ArrayList<Integer> verseChords;
	ArrayList<Integer> chorusChords;
	ArrayList<Integer> bridgeChords;
	
	ArrayList<Integer> rhythm1;
	ArrayList<Integer> rhythm2;
	
	ArrayList<Integer> theme;
	
	ArrayList<ArrayList<Integer>> melody;
	
	public Song(){		
		timeSigNum = 0;
		timeSigDenom = 0;
		
		scaleType = ScaleType.MAJOR;
		
		vStructure = null;
		
		verseChords = null;
		chorusChords = null;
		bridgeChords = null;
		
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
