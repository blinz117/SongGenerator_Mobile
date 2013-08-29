package com.blinz117.songgenerator;

import java.util.Random;
import java.util.ArrayList;

import com.blinz117.songgenerator.SongStructure.*;
import com.leff.midi.event.ProgramChange;
import com.leff.midi.event.ProgramChange.MidiProgram;

public class SongWriter {

	/*
	 * TODO: Should some of these go into the SongStructure class?
	 */
	public static ProgramChange.MidiProgram[] baseInstruments = {
		MidiProgram.STRING_ENSEMBLE_2, 
		MidiProgram.ACOUSTIC_GUITAR_STEEL,
		MidiProgram.ACOUSTIC_GRAND_PIANO,
		MidiProgram.VIOLIN,
		MidiProgram.ROCK_ORGAN,
		MidiProgram.DISTORTION_GUITAR
	};
	
	static final double[] CHORDPROBS = {5.0, 1.5, 1.5, 4.0, 5.0, 1.5, 0.25};
	static final int NUMCHORDS = CHORDPROBS.length;
	int probSums = 0;
	
	static final double[] SCALETYPEPROBS = {10.0, 2.0, 1.0};
	
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
		
		int numTimeSigNums = SongStructure.TIMESIGNUMVALUES.length;
		int numTimeSigDenoms = SongStructure.TIMESIGDENOMVALUES.length;
		mTimeSigNumer = masterpiece.timeSigNum = SongStructure.TIMESIGNUMVALUES[randGen.nextInt(numTimeSigNums)];//mTimeSigNumer;
		mTimeSigDenom = masterpiece.timeSigDenom = SongStructure.TIMESIGDENOMVALUES[randGen.nextInt(numTimeSigDenoms)];//mTimeSigDenom;
		
		masterpiece.scaleType = chooseScaleType();
		masterpiece.key = SongStructure.PITCHES[randGen.nextInt(SongStructure.NUMPITCHES)];
		
		masterpiece.structure = generateStructure();
		masterpiece.verseChords = generateNewChordProgression();
		masterpiece.chorusChords = generateBetterChordProgression();
		masterpiece.bridgeChords = generateChordProgression();
		
		masterpiece.rhythm1 = generateRhythm();
		masterpiece.rhythm2 = generateRhythm();
		
		masterpiece.theme = generateTheme();
		
		masterpiece.melody = generateMelody(masterpiece.verseChords);
		
		masterpiece.chordInstrument = baseInstruments[randGen.nextInt(baseInstruments.length)];
		
		masterpiece.melodyInstrument = baseInstruments[randGen.nextInt(baseInstruments.length)];
		
		return masterpiece;
	} // writeNewSong
	
	/*
	 * Generation methods
	 */
	public ScaleType chooseScaleType()
	{
		int ndx = Utils.pickNdxByProb(SCALETYPEPROBS);
		return ScaleType.values()[ndx];
	}
	
	public ArrayList<SongPart> generateStructure()
	{
		ArrayList<SongPart> structure = new ArrayList<SongPart>();
		double[] partProbs = basePartProbs;
		
		int iNumParts = randGen.nextInt(2) + 4;
		
		for (int iPart = 0; iPart < iNumParts; iPart++)
		{
			int songPartNdx = Utils.pickNdxByProb(partProbs);
			
			structure.add(SongPart.values()[songPartNdx]);
			partProbs = basePartProbs;
			partProbs[songPartNdx] = 0.1;
		}
		return structure;
	}
	
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
	
	// slightly better(?) "algorithm" for generating chord progressions
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
	
	// slightly better(?) "algorithm" for generating chord progressions
	public ArrayList<Integer> generateNewChordProgression()
	{
		//double action = randGen.nextDouble();
		ArrayList<Integer> chordProg = new ArrayList<Integer>();
		
		/*
		 * TODO
		 * 
		 * Generate "turnaround" parts (a, b, a, b1 or a, a, a, a1)
		 */
		int numChords = 4;
		
		ArrayList<Integer> partA = generateSubProgression(numChords);
		// always start with root chord
		partA.set(0, 1);
		
		ArrayList<Integer> partB = generateSubProgression(numChords);
		// always start with root chord
		partB.set(numChords - 1, 4);
		
		ArrayList<Integer> partC;
		if (randGen.nextDouble() < 0.5)
			partC = generateSubProgression(numChords);
		else
			partC = partA;

		ArrayList<Integer> partD = generateSubProgression(numChords);
		partD.set(numChords - 1, 5);
		
		for (Integer chord: partA)
			chordProg.add(chord);
		
		for (Integer chord: partB)
			chordProg.add(chord);

		for (Integer chord: partC)
			chordProg.add(chord);
		
		for (Integer chord: partD)
			chordProg.add(chord);
//		int currChord = Utils.pickNdxByProb(CHORDPROBS);
//		chordProg.add(currChord + 1);
//		
//		// make it an even number of chords for now
//		int numChords = 4*(randGen.nextInt(3) + 1);
//		for (int chord = 1; chord < numChords; chord++)
//		{
//			currChord = Utils.pickNdxByProb(SongStructure.chordChances[currChord]) ;
//			chordProg.add(currChord + 1);
//		}
		return chordProg;			
	}
	
	public ArrayList<Integer> generateSubProgression(int numChords)
	{
		ArrayList<Integer> subProg = new ArrayList<Integer>();
		
		int currChord = Utils.pickNdxByProb(CHORDPROBS);
		subProg.add(currChord + 1);
		
		for (int chord = 1; chord < numChords; chord++)
		{
			currChord = Utils.pickNdxByProb(SongStructure.chordChances[currChord]) ;
			subProg.add(currChord + 1);
		}
		return subProg;	
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
	
	public ArrayList<Integer> generateTheme()
	{
		ArrayList<Integer> theme = new ArrayList<Integer>();
		
		double[] probs = {5, 1, 5, 1, 5, 1, 0};
		for (int note = 0; note < mTimeSigNumer; note++)
		{
			theme.add(Utils.pickNdxByProb(probs) + 1);
		}
				
		return theme;
	}
	
	public ArrayList<ArrayList<Integer>> generateMelody(ArrayList<Integer> chords)
	{
		ArrayList<ArrayList<Integer>> melody = new ArrayList<ArrayList<Integer>>();
		for (int chord = 0; chord < chords.size(); chord++)
		{
			melody.add(generateTheme());
		}
		return melody;
	}
	
} //class SongWriter
