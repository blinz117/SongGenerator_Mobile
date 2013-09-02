package com.blinz117.songgenerator;

import java.util.Random;
import java.util.ArrayList;

import com.blinz117.songgenerator.songstructure.*;
import com.blinz117.songgenerator.songstructure.MusicStructure;
import com.blinz117.songgenerator.songstructure.Song;
import com.blinz117.songgenerator.songstructure.MusicStructure.*;
import com.leff.midi.event.ProgramChange;
import com.leff.midi.event.ProgramChange.MidiProgram;

public class SongWriter {

	/*
	 * TODO: Should some of these go into the MusicStructure class?
	 */
	public static ProgramChange.MidiProgram[] baseInstruments = {
		MidiProgram.STRING_ENSEMBLE_2, 
		MidiProgram.ACOUSTIC_GUITAR_STEEL,
		MidiProgram.ACOUSTIC_GRAND_PIANO,
		MidiProgram.VIOLIN,
		MidiProgram.ROCK_ORGAN,
		MidiProgram.DISTORTION_GUITAR
	};
	
	protected static final double[] CHORDPROBS = {5.0, 1.5, 1.5, 4.0, 5.0, 1.5, 0.25};
	protected static final int NUMCHORDS = CHORDPROBS.length;
	
	protected static final double[] SCALETYPEPROBS = {10.0, 2.0, 1.0};
	
	protected double[] basePartProbs = {0.6, 0.3, 0.1};
	
	Random randGen;
	
	Pitch mCurrKey;
	int mTimeSigNumer;
	int mTimeSigDenom;
	
	public SongWriter() 
	{
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
		
		int numTimeSigNums = MusicStructure.TIMESIGNUMVALUES.length;
		int numTimeSigDenoms = MusicStructure.TIMESIGDENOMVALUES.length;
		mTimeSigNumer = masterpiece.timeSigNum = MusicStructure.TIMESIGNUMVALUES[randGen.nextInt(numTimeSigNums)];//mTimeSigNumer;
		mTimeSigDenom = masterpiece.timeSigDenom = MusicStructure.TIMESIGDENOMVALUES[randGen.nextInt(numTimeSigDenoms)];//mTimeSigDenom;
		
		masterpiece.scaleType = chooseScaleType();
		masterpiece.key = MusicStructure.PITCHES[randGen.nextInt(MusicStructure.NUMPITCHES)];
		
		masterpiece.structure = generateStructure();
		masterpiece.verseProgression = generateChordProgression();
		masterpiece.chorusProgression = generateChordProgression();
		masterpiece.bridgeProgression = generateChordProgression();
		
		masterpiece.rhythm1 = generateRhythm();
		masterpiece.rhythm2 = generateRhythm();
		
		masterpiece.theme = generateTheme();
		
		masterpiece.melody = masterpiece.verseProgression.getMelody();//generateMelody(masterpiece.verseProgression.getChords());
		//masterpiece.melody.addAll(generateMelody(masterpiece.chorusProgression.getChords()));
		//masterpiece.melody.addAll(generateMelody(masterpiece.bridgeProgression.getChords()));
		
		
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
	
	/*
	 * Some old methods for generating Chord progressions.
	 * Commenting out for now.

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
			currChord = Utils.pickNdxByProb(MusicStructure.chordChances[currChord]) ;
			chordProg.add(currChord + 1);
		}
		return chordProg;			
	}
	*/
	
	// slightly better(?) "algorithm" for generating chord progressions
	public ChordProgression generateChordProgression()
	{
		//double action = randGen.nextDouble();
		ChordProgression chordProg = new ChordProgression();
		
		/*
		 * TODO
		 * 
		 * Generate "turnaround" parts (a, b, a, b1 or a, a, a, a1)
		 */
		int numChords = 4;
		
		Pattern partA = generatePattern(numChords);
		// always start with root chord
		partA.chords.set(0, 1);
		
		Pattern partB = generatePattern(numChords);
		// always start with root chord
		partB.chords.set(numChords - 1, 4);
		
		Pattern partC;
		if (randGen.nextDouble() < 0.5)
			partC = generatePattern(numChords);
		else
			partC = partA;

		Pattern partD = generatePattern(numChords);
		partD.chords.set(numChords - 1, 5);
		
		chordProg.patterns.add(partA);
		
		chordProg.patterns.add(partB);

		chordProg.patterns.add(partC);
		
		chordProg.patterns.add(partD);
//		int currChord = Utils.pickNdxByProb(CHORDPROBS);
//		chordProg.add(currChord + 1);
//		
//		// make it an even number of chords for now
//		int numChords = 4*(randGen.nextInt(3) + 1);
//		for (int chord = 1; chord < numChords; chord++)
//		{
//			currChord = Utils.pickNdxByProb(MusicStructure.chordChances[currChord]) ;
//			chordProg.add(currChord + 1);
//		}
		return chordProg;			
	}
	
	public Pattern generatePattern(int numChords)
	{
		Pattern pattern = new Pattern();
		
		int currChord = Utils.pickNdxByProb(CHORDPROBS);
		pattern.chords.add(currChord + 1);
		pattern.melody.add(generateTheme());
		
		for (int chord = 1; chord < numChords; chord++)
		{
			currChord = Utils.pickNdxByProb(MusicStructure.chordChances[currChord]) ;
			pattern.chords.add(currChord + 1);
			pattern.melody.add(generateTheme());
		}
		return pattern;	
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
	
	// TODO: Old way of creating melody... this is now created with the pattern.
	// Get rid of it once I am sure that is what I want to do.
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
