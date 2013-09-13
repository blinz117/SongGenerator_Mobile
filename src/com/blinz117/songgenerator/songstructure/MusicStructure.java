package com.blinz117.songgenerator.songstructure;

import java.util.*;

public class MusicStructure {
	
	public enum ScaleType { MAJOR, NATURALMINOR, HARMONICMINOR, MIXOLYDIAN, DORIAN };
	
	public static final int[] TIMESIGNUMVALUES = {2, 3, 4};
	public static final int[] TIMESIGDENOMVALUES = {4}; // just 4 for now... may expand later
	// these describe the intervals (in semi-tones) between the notes
	// in each scale
	public static final int[] MAJORSCALE = {2, 2, 1, 2, 2, 2, 1};
	public static final int[] MAJORSCALEINTERVALS = {0, 2, 4, 5, 7, 9, 11};
	
	public static final int[] NATURALMINORSCALE = {2, 1, 2, 2, 1, 2, 2};
	public static final int[] NATMINORSCALEINTERVALS = {0, 2, 3, 5, 7, 8, 10};
	
	public static final int[] HARMONICMINORSCALE = {2, 1, 2, 2, 1, 3, 1};
	public static final int[] HARMMINORSCALEINTERVALS = {0, 2, 3, 5, 7, 8, 11};
	
	public static final int[] MIXOLYDIANSCALE = {2, 2, 1, 2, 2, 1, 2};
	public static final int[] MIXOLYDIANSCALEINTERVALS = {0, 2, 4, 5, 7, 9, 10};
	
	public static final int[] DORIANSCALE = {2, 1, 2, 2, 2, 1, 2};
	public static final int[] DORIANSCALEINTERVALS = {0, 2, 3, 5, 7, 9, 10};
	
	// Define the pitches present in the "Western" system
	// (forgive my ignorance on naming conventions)
	public enum Pitch {C, D_FLAT, D, E_FLAT, E, F, G_FLAT, G, A_FLAT, A, B_FLAT, B;
		public int getBaseMidiPitch(){
			return this.ordinal() + 60;
		}
		
		// TODO: Implement MIDI key signature so importing into other software shows correct key signature
		// cof = Circle of Fifths: used to determine key signature
		protected int[] cofMajor = {0, -5, 2, -3, 4, -1, -6, 1, -4, 3, -2, 5};
		
		public int getMIDIKeyNumMajor()
		{
			return cofMajor[this.ordinal()];
		}
		
		public int getMIDIKeyNumMinor()
		{
			// TODO: I THINK THIS STILL NEEDS WORK!!!!
			return getRelativeMajor().getMIDIKeyNumMajor();
		}
		
		public Pitch getRelativeMinor()
		{
			return values()[(this.ordinal() + 9) % NUMPITCHES];
		}
		
		public Pitch getRelativeMajor()
		{
			return values()[(this.ordinal() + 3) % NUMPITCHES];
		}
		
		@Override
		public String toString(){
			switch(this)
			{
			case C:
				return "C";
			case D_FLAT:
				return "Db";
			case D:
				return "D";
			case E_FLAT:
				return "Eb";
			case E:
				return "E";
			case F:
				return "F";
			case G_FLAT:
				return "Gb";
			case G:
				return "G";
			case A_FLAT:
				return "Ab";
			case A:
				return "G";
			case B_FLAT:
				return "Bb";
			case B:
				return "B";
			default:
				return "";
			}
		}
	}
	public static Pitch[] PITCHES = Pitch.values();
	public static final int NUMPITCHES = 12, OCTAVE = NUMPITCHES;
	
	public enum SongPart { VERSE, CHORUS, BRIDGE };
	
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
		case MIXOLYDIAN:
			return MIXOLYDIANSCALEINTERVALS;
		case DORIAN:
			return DORIANSCALEINTERVALS;
		default:
			return null;
		}
	}
	
	public enum Cadence{ AUTHENTIC, HALF, PLAGAL, INTERRUPTED;
		public List<Integer> getChords()
		{
			switch(this)
			{
			case AUTHENTIC:
				return Arrays.asList(5, 1);
			case HALF:
				return Arrays.asList(5);
			case PLAGAL:
				return Arrays.asList(4, 1);
			case INTERRUPTED:
				return Arrays.asList(5, -1);
			default:
				return new ArrayList<Integer>();
			}
		}
		
		public static final double[] INTERRUPTEDCHORDCHANCES = {0.0, 3.0, 1.0, 2.0, 0.0, 4.0, 0.1};
	
	}
	
	////////////////////////////////////////////////////
	//This is a playground for testing at the moment:
	
	/*
	 * TODO: Should this really be here? Does it make more sense in the SongWriter?
	 * It is not really specific to song structure itself... more like song writing?
	 * I dunno... think about it some more...
	 */
	
	
	public static final double[][] chordChances = {
		{0, 2, 4, 10, 8, 4, 0.5},
		{4, 0, 2, 5, 6, 2, 0.25},
		{3, 2, 0, 6, 6, 4, 0.25},
		{6, 4, 4, 0, 10, 4, 0.25},
		{10, 3, 3, 6, 0, 4, 0.5},
		{3, 2, 5, 5, 5, 0, 0.5},
		{10, 1, 1, 3, 4, 1, 0}
	};
	
	/*
	 * Translates degree, which is the interval from the root of the chord, 
	 * represented by chordRoot, to the interval relative to the scale tonic
	 */
	public int chordToScaleInterval(int chordRoot, int degree)
	{
		return (chordRoot + degree) % 7;
	}
	
	/*
	 * Translates degree, which is the interval from the tonic of the scale 
	 *  to the interval relative to the chord root, represented by chordRoot
	 */
	public int scaleToChordInterval(int chordRoot, int degree)
	{
		return (degree - chordRoot) & 7;
	}

}
