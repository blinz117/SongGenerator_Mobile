package com.blinz117.songgenerator;

import java.util.Random;
import java.util.ArrayList;

public class SongWriter {

	// these describe the intervals (in semi-tones) between the notes
	// in each scale
	static final int[] MAJORSCALE = {2, 2, 1, 2, 2, 2, 1};
	static final int[] NATURALMINROSCALE = {2, 1, 2, 2, 1, 2, 2};
	static final int[] HARMONICMINORSCALE = {2, 1, 2, 2, 1, 3, 1};
	
	static final double[] CHORDPROBS = {5.0, 1.5, 1.5, 4.0, 5.0, 1.5, 0.25};
	static final int NUMCHORDS = CHORDPROBS.length;
	int probSums = 0;
	
	public enum SongPart { VERSE, CHORUS, BRIDGE }
	
	// Define the pitches present in the "Western" system
	// (forgive my ignorance on naming conventions)
	public enum Pitch {C, C_SHARP, D, D_SHARP, E, F, F_SHARP, G, G_SHARP, A, A_SHARP, B }
	Pitch[] PITCHES;
	static final int NUMPITCHES = 12;
	
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
		
		PITCHES = Pitch.values();
		
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
	
	public Song writeSong()
	{
		Song masterpiece = new Song();
		masterpiece.writeNewSong();
		return masterpiece;
	}
	
	public class Song
	{
		Random randGen;
		ArrayList<SongPart> vStructure;
		//Random randGen;
		double[] baseProbs = {0.6, 0.3, 0.1};
		
		ArrayList<Integer> verseChords;
		ArrayList<Integer> chorusChords;
		ArrayList<Integer> bridgeChords;
		
		ArrayList<Integer> rhythm1;
		ArrayList<Integer> rhythm2;
		
		public Song(){
			randGen = new Random();
			//randGen = new Random();
			vStructure = new ArrayList<SongPart>();
			
			verseChords = null;
			chorusChords = null;
			bridgeChords = null;
			
			rhythm1 = null;
			rhythm2 = null;
		}
		
		public ArrayList<Integer> generateChordProgression()
		{
			ArrayList<Integer> chordProg = new ArrayList<Integer>();
			// make it an even number of chords for now
			int numChords = 4*(randGen.nextInt(3) + 1);
			for (int chord = 0; chord < numChords; chord++)
			{
				int nextChord = pickNdxByProb(CHORDPROBS) + 1;
				chordProg.add(nextChord);
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
				int numBeats = pickNdxByProb(probs) + 1;
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
		
		public void writeNewSong()
		{
			// Generate probabilities
			// TODO: Probably want to put this into its own method when more stuff is added
			double[] partProbs = baseProbs;
			int numPartTypes = partProbs.length;
			
			int iNumParts = randGen.nextInt(2) + 4;
			SongPart nextPart = null;
			
			for (int iPart = 0; iPart < iNumParts; iPart++)
			{
				double nextChoice = randGen.nextDouble();
				double probSum = 0.0;
				Boolean bContinueChecking = true;
				// TODO: can probably refactor this to use the findNdxByProb function below
				for (int ndx = 0; ndx < partProbs.length; ndx++)
				{
					if (bContinueChecking)
					{
						nextPart = SongPart.values()[ndx];
						
						//check probability to see if we found the one
						probSum += partProbs[ndx];
						if (nextChoice < probSum)
						{
							// found the one we want
							bContinueChecking = false;
							partProbs[ndx] = 0.025;
							continue;
						}
					}
					// reset probability
					partProbs[ndx] = 0.975/(numPartTypes - 1);
				}
				vStructure.add(nextPart);
			}
			
			rhythm1 = generateRhythm();
			
			// now generate chord progression for each segment
			verseChords = generateChordProgression();
			chorusChords = generateChordProgression();
			bridgeChords = generateChordProgression();
			
			rhythm2 = null;
			rhythm2 = generateRhythm();
		} // writeNewSong
		
	} //class Song
	
	/*
	 * returns an index into an array based on the probabilities in the array
	 * The values in the array do not have to add up to 1. They will be scaled if not.
	 * returns -1 if something goes wrong
	 */
	public int pickNdxByProb(double[] probs)
	{
		// make sure it has a length
		if (probs.length < 1)
			return -1;
		
		double probTotal = 0.0;
		for (int prob = 0; prob < probs.length; prob++)
		{			
			//get sum total of all probabilities
			probTotal += probs[prob];
		}
		
		//Random randGen = new Random();
		double goalVal = randGen.nextDouble();
		double curProbSum = 0.0;
		for (int ndx = 0; ndx < probs.length; ndx++)
		{			
			//check probability to see if we found the one
			curProbSum += probs[ndx]/probTotal;
			if (goalVal < curProbSum)
			{
				// found the one we want
				return ndx;
			}
		}
		
		//sentinel: should never get here
		return -1;
		
	}//pickNdxByProb
	
} //class SongWriter
