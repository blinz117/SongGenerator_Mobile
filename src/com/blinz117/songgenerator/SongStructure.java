package com.blinz117.songgenerator;

public class SongStructure {
	
	public enum ScaleType { MAJOR, NATURALMINOR, HARMONICMINOR };
	
	// these describe the intervals (in semi-tones) between the notes
	// in each scale
	static final int[] MAJORSCALE = {2, 2, 1, 2, 2, 2, 1};
	static final int[] MAJORSCALEINTERVALS = {0, 2, 4, 5, 7, 9, 11};
	
	static final int[] NATURALMINORSCALE = {2, 1, 2, 2, 1, 2, 2};
	static final int[] NATMINORSCALEINTERVALS = {0, 2, 3, 5, 7, 8, 10};
	
	static final int[] HARMONICMINORSCALE = {2, 1, 2, 2, 1, 3, 1};
	static final int[] HARMMINORSCALEINTERVALS = {0, 2, 3, 5, 7, 8, 11};
	
	// Define the pitches present in the "Western" system
	// (forgive my ignorance on naming conventions)
	public enum Pitch {C, C_SHARP, D, D_SHARP, E, F, F_SHARP, G, G_SHARP, A, A_SHARP, B }
	Pitch[] PITCHES = Pitch.values();
	static final int NUMPITCHES = 12;
	
	public enum SongPart { VERSE, CHORUS, BRIDGE };
	
	// currently generates a triad based on a major scale. Will eventually extend this
	// MAKE THIS BETTER!!!!
	public static int[] generateTriad(int root, ScaleType scaleType)
	{
		int[] scaleIntervals = getScaleIntervals(scaleType);
		int[] triad = new int[3];
		triad[0] = scaleIntervals[root - 1]; 
		triad[1] = scaleIntervals[(root + 1) % 7];
		triad[2] = scaleIntervals[(root + 3) % 7];
		
		return triad;
	}
	
	public static int[] getScaleIntervals(ScaleType type)
	{
		switch (type)
		{
		case MAJOR:
			return MAJORSCALEINTERVALS;
		case NATURALMINOR:
			return NATMINORSCALEINTERVALS;
		case HARMONICMINOR:
			return HARMMINORSCALEINTERVALS;
		default:
			return null;
		}
	}
	
	////////////////////////////////////////////////////
	//This is a playground for testing at the moment:
	
	/*
	 * TODO: Should this really be here? Does it make more sense in the SongWriter?
	 * It is not really specific to song structure itself... more like song writing?
	 * I dunno... think about it some more...
	 */
	
	
	static final double[][] chordChances = {
		{0, 2, 4, 10, 8, 4, 0.5},
		{4, 0, 2, 5, 6, 2, 0.25},
		{3, 2, 0, 6, 6, 4, 0.25},
		{6, 4, 4, 0, 10, 4, 0.25},
		{10, 3, 3, 6, 0, 4, 0.5},
		{3, 2, 5, 5, 5, 0, 0.5},
		{10, 1, 1, 3, 4, 1, 0}
	};

}
