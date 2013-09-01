package com.blinz117.songgenerator.songstructure;

public class Note {

	public int pitch;
	public int numBeats;
	
	public Note()
	{
		pitch = -1;
		numBeats = 1;
	}
	
	public Note(Note inst)
	{
		this.pitch = inst.pitch;
		this.numBeats = inst.numBeats;
	}
	
	public Note(int pitch, int numBeats)
	{
		this.pitch = pitch;
		this.numBeats = numBeats;
	}
}
