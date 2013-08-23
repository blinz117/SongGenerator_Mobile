package com.blinz117.songgenerator;

import java.util.ArrayList;

import com.blinz117.songgenerator.SongStructure.*;

public class Song
{
	ArrayList<SongPart> vStructure;
	
	ArrayList<Integer> verseChords;
	ArrayList<Integer> chorusChords;
	ArrayList<Integer> bridgeChords;
	
	ArrayList<Integer> rhythm1;
	ArrayList<Integer> rhythm2;
	
	public Song(){
		vStructure = new ArrayList<SongPart>();
		
		verseChords = null;
		chorusChords = null;
		bridgeChords = null;
		
		rhythm1 = null;
		rhythm2 = null;
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
