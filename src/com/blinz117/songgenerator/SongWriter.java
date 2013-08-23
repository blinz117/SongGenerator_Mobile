package com.blinz117.songgenerator;

import java.util.Random;
import java.util.ArrayList;

import com.blinz117.songgenerator.SongStructure.*;

public class SongWriter {

	/*
	 * TODO: Should some of these go into the SongStructure class?
	 */
	static final double[] CHORDPROBS = {5.0, 1.5, 1.5, 4.0, 5.0, 1.5, 0.25};
	static final int NUMCHORDS = CHORDPROBS.length;
	int probSums = 0;
	
	double[] basePartProbs = {0.6, 0.3, 0.1};
	
	Random randGen;
	
	Pitch mCurrKey;
	int mTimeSigNumer;
	int mTimeSigDenom;
	
	public SongWriter() 
	{
		// initialize a few things
		for (int ndx = 0; ndx < NUMCHORDS; ndx++)
		{
			probSums += CHORDPROBS[ndx];
		}
		
		randGen = new Random();
		
		mCurrKey = null;
		mTimeSigNumer = 0;
		mTimeSigDenom = 0;
	}
	
	public Pitch getKey() {return mCurrKey; }
	public int getTimeSigNumerator() {return mTimeSigNumer; }
	public int getTimeSigDenominator() {return mTimeSigDenom; }
	
	public void setKey(Pitch newKey) { mCurrKey = newKey; }
	public void setTimeSigNumerator(int newNumer) { mTimeSigNumer = newNumer; }
	public void setTimeSigDenominator(int newDenom) { mTimeSigDenom = newDenom; }
	
	
	public Song writeNewSong()
	{
		Song masterpiece = new Song();
		
		// Generate probabilities
		// TODO: Probably want to put this into its own method when more stuff is added
		double[] partProbs = basePartProbs;
		//int numPartTypes = partProbs.length;
		
		int iNumParts = randGen.nextInt(2) + 4;
		
		for (int iPart = 0; iPart < iNumParts; iPart++)
		{
			int songPartNdx = Utils.pickNdxByProb(partProbs);
			
			masterpiece.vStructure.add(SongPart.values()[songPartNdx]);//nextPart);
			partProbs = basePartProbs;
			partProbs[songPartNdx] = 0.1;
		}
		
		masterpiece.rhythm1 = generateRhythm();
		
		// now generate chord progression for each segment
		masterpiece.verseChords = generateBetterChordProgression();
		masterpiece.chorusChords = generateChordProgression();
		masterpiece.bridgeChords = generateChordProgression();
		
		masterpiece.rhythm2 = null;
		masterpiece.rhythm2 = generateRhythm();
		
		return masterpiece;
	} // writeNewSong
	
	/*
	 * TODO: Make Song a more "struct-like" class; it may make more sense if it just holds information
	 * rather than writing itself(?)... that is what the songwriter class is really for.
	 * Also, maybe make it external to the SongWriter(?)
	 */
/*	public class Song
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
			
	} //class Song
*/	
	/*
	 * Generation methods
	 */
	public ArrayList<Integer> generateChordProgression()
	{
		ArrayList<Integer> chordProg = new ArrayList<Integer>();
		// make it an even number of chords for now
		int numChords = 4*(randGen.nextInt(3) + 1);
		for (int chord = 0; chord < numChords; chord++)
		{
			int nextChord = Utils.pickNdxByProb(CHORDPROBS) + 1;
			chordProg.add(nextChord);
		}
		return chordProg;			
	}
	
	// slightly better(?) "algortihm" for generating chord progressions
	public ArrayList<Integer> generateBetterChordProgression()
	{
		ArrayList<Integer> chordProg = new ArrayList<Integer>();
		
		int currChord = Utils.pickNdxByProb(CHORDPROBS);
		chordProg.add(currChord + 1);
		
		// make it an even number of chords for now
		int numChords = 4*(randGen.nextInt(3) + 1);
		for (int chord = 1; chord < numChords; chord++)
		{
			currChord = Utils.pickNdxByProb(SongStructure.chordChances[currChord]) ;
			chordProg.add(currChord + 1);
		}
		return chordProg;			
	}
	
	public ArrayList<Integer> generateRhythm()
	{
		int numHalfBeats = mTimeSigNumer * 2;
		ArrayList<Integer> rhythm = new ArrayList<Integer>();
		
		int note = 0;
		while (note < numHalfBeats)
		{
			// generate a crappy probability spread
			double[] probs = new double[numHalfBeats - note];
			for (int prob = 0; prob < probs.length; prob++)
			{
				probs[prob] = (double)(probs.length - prob);
			}
			int numBeats = Utils.pickNdxByProb(probs) + 1;
			if (numBeats < 0)
				continue;
			
			note += numBeats;
			
			// small chance to be negative (a rest)
			double restChance = randGen.nextDouble();
			if (restChance < 0.2)
				numBeats *= -1;
			
			rhythm.add(numBeats);
		}
		return rhythm;
	}
	
} //class SongWriter
