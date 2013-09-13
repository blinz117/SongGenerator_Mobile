package com.blinz117.songgenerator;

import java.util.*;

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
		MidiProgram.STRING_ENSEMBLE_1, 
		MidiProgram.ACOUSTIC_GUITAR_STEEL,
		MidiProgram.ACOUSTIC_GRAND_PIANO,
		MidiProgram.VIOLIN,
		MidiProgram.CHURCH_ORGAN,
		MidiProgram.DISTORTION_GUITAR,
		MidiProgram.BRASS_SECTION,
		MidiProgram.OVERDRIVEN_GUITAR,
		MidiProgram.TRUMPET,
		MidiProgram.CLARINET,
		MidiProgram.BANJO,
		MidiProgram.ACCORDION
	};
	
	protected static final double[] CHORDPROBS = {5.0, 1.5, 1.5, 4.0, 5.0, 1.5, 0.25};
	protected static final int NUMCHORDS = CHORDPROBS.length;
	
	protected static final double[] SCALETYPEPROBS = {15.0, 4.0, 3.0, 1.0, 2.0};
	
	protected double[] basePartProbs = {0.6, 0.3, 0.1};
	
	protected double[] pitchProbs = {5, 1, 5, 2, 5, 1, 0.025};
	
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
		mTimeSigNumer = masterpiece.timeSigNum = MusicStructure.TIMESIGNUMVALUES[randGen.nextInt(numTimeSigNums)];
		mTimeSigDenom = masterpiece.timeSigDenom = MusicStructure.TIMESIGDENOMVALUES[randGen.nextInt(numTimeSigDenoms)];
		
		masterpiece.tempo = randGen.nextInt(120) + 80;
		
		masterpiece.scaleType = chooseScaleType();
		masterpiece.key = MusicStructure.PITCHES[randGen.nextInt(MusicStructure.NUMPITCHES)];
		
		masterpiece.structure = generateStructure();
		masterpiece.verseProgression = generateVerseProgression();
		masterpiece.chorusProgression = generateChorusProgression();
		masterpiece.bridgeProgression = generateVerseProgression();
		
		masterpiece.rhythm1 = generateRhythm();
		masterpiece.rhythm2 = generateRhythm();
		
		masterpiece.theme = generateTheme();
		
		masterpiece.melody = masterpiece.verseProgression.getMelody();
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
	
	public ChordProgression generateVerseProgression()
	{
		ChordProgression chorus = generateChordProgression();
		applyCadence(chorus.patterns.get(chorus.patterns.size() - 1), Cadence.HALF);
		return chorus;
	}
	
	
	public ChordProgression generateChorusProgression()
	{
		ChordProgression chorus = generateChordProgression();
		Cadence type;
		if (randGen.nextDouble() < 0.8)
			type = Cadence.AUTHENTIC;
		else
			type = Cadence.PLAGAL;
		applyCadence(chorus.patterns.get(chorus.patterns.size() - 1), type);
		return chorus;
	}
	
	// slightly better(?) "algorithm" for generating chord progressions
	// TODO: Keep working on this so it is more robust and
	// generates more varied songs
	public ChordProgression generateChordProgression()
	{
		ChordProgression chordProg = new ChordProgression();
		
		int numChords = 4;
		
		Pattern partA, partB, partC, partD;
		
		partA = generatePattern(numChords);
		// always start with root chord
		partA.chords.set(0, 1);
		
		if (randGen.nextDouble() < 0.75)
		{
			partB = new Pattern(partA);
			if (randGen.nextDouble() < 0.45)
				applyMelodyVariation(partB);
		}
		else
			partB = generatePattern(numChords);
		double cadenceChance = randGen.nextDouble();
		if (cadenceChance < 0.75)
			// end second part on half cadence
			//partB.chords.set(numChords - 1, 4);
			applyCadence(partB, Cadence.HALF);
		else if (cadenceChance < 0.9)
			applyCadence(partB, Cadence.INTERRUPTED);

		if (randGen.nextDouble() < 0.6)
		{
			partC = new Pattern(partA);
			if (randGen.nextDouble() < 0.25)
				applyMelodyVariation(partC);
		}
		else
			partC = generatePattern(numChords);

		if (randGen.nextDouble() < 0.85)
		{
			partD = new Pattern(partB);
			if (randGen.nextDouble() < 0.85)
				// add a different melody at the end to make it more interesting
				applyMelodyVariation(partD);
				//partD.notes.set(partD.notes.size() - 1, generateNotes());
		}
		else
			partD = generatePattern(numChords);
		
		// This part will get applied at the progression level, so don't set it here
		// end last part on half cadence
		//partD.chords.set(numChords - 1, 5);
		//applyCadence(partD, Cadence.HALF);
		
		chordProg.patterns.add(partA);	
		chordProg.patterns.add(partB);
		chordProg.patterns.add(partC);
		chordProg.patterns.add(partD);

		return chordProg;			
	}
	
	public Pattern generatePattern(int numChords)
	{
		Pattern pattern = new Pattern();
		
		int currChord = Utils.pickNdxByProb(CHORDPROBS);
		pattern.chords.add(currChord + 1);
		pattern.melody.add(generateTheme());
		pattern.notes.add(generateNotes());
		
		for (int chord = 1; chord < numChords; chord++)
		{
			currChord = Utils.pickNdxByProb(MusicStructure.chordChances[currChord]) ;
			pattern.chords.add(currChord + 1);
			pattern.melody.add(generateTheme());
			pattern.notes.add(generateNotes());
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
			// generate even numbered notes with weird distribution
			double[] probs = new double[numHalfBeats - note];
			for (int prob = 0; prob < probs.length; prob++)
			{
				if (prob == 0 || (prob + 1) % 2 == 0 )
					probs[prob] = (double)(probs.length - prob);
				else 
					probs[prob] = 0;
				
				if (note % 4 == 0)
				{
					probs[0] = probs[0]/5;
				}
			}
			int numBeats = Utils.pickNdxByProb(probs) + 1;
			if (numBeats < 0)
				continue;
			
			note += numBeats;
			
			// small chance to be negative (a rest)
			double restChance = randGen.nextDouble();
			if (restChance < 0.05)
				numBeats *= -1;
			
			rhythm.add(numBeats);
		}
		return rhythm;
	}
	
	public ArrayList<Integer> generateTheme()
	{
		ArrayList<Integer> theme = new ArrayList<Integer>();
		
		for (int note = 0; note < mTimeSigNumer; note++)
		{
			theme.add(Utils.pickNdxByProb(pitchProbs) + 1);
		}
				
		return theme;
	}
	
	public ArrayList<Note> generateNotes()
	{
		ArrayList<Note> notes = new ArrayList<Note>();
		
		ArrayList<Integer> rhythm = generateRhythm();
		for (Integer item: rhythm)
		{
			int pitch;
			// check if it is a rest
			if (item < 0)
				pitch = -1;
			else
				pitch = Utils.pickNdxByProb(pitchProbs) + 1;
			
			notes.add(new Note(pitch, item));
		}
		return notes;
	}
	
	public ArrayList<Note> generateNotes(ArrayList<Integer> rhythm)
	{
		ArrayList<Note> notes = new ArrayList<Note>();
		
		for (Integer item: rhythm)
		{
			int pitch;
			// check if it is a rest
			if (item < 0)
				pitch = -1;
			else
				pitch = Utils.pickNdxByProb(pitchProbs) + 1;
			
			notes.add(new Note(pitch, item));
		}
		return notes;
	}
	
	// TODO: Old way of creating melody... this is now created with the pattern.
	// Get rid of it once I am sure that is what I want to do.
//	public ArrayList<ArrayList<Integer>> generateMelody(ArrayList<Integer> chords)
//	{
//		ArrayList<ArrayList<Integer>> melody = new ArrayList<ArrayList<Integer>>();
//		for (int chord = 0; chord < chords.size(); chord++)
//		{
//			melody.add(generateTheme());
//		}
//		return melody;
//	}
	
	public void applyCadence(Pattern pattern, Cadence type)
	{
		List<Integer> cadenceChords = type.getChords();
		int numCadenceChords = cadenceChords.size();
		int numPatternChords = pattern.chords.size();
		
		// realistically, this shouldn't happen, but try to handle it just in case
		if (numCadenceChords > numPatternChords)
			cadenceChords = cadenceChords.subList(numCadenceChords - numPatternChords, numCadenceChords);
		
		for (int chord = 0; chord < cadenceChords.size(); chord++)
		{
			pattern.chords.set(numPatternChords - numCadenceChords + chord, cadenceChords.get(chord));
		}
		
		if (type == Cadence.INTERRUPTED)
		{
			pattern.chords.set(numPatternChords - 1, Utils.pickNdxByProb(Cadence.INTERRUPTEDCHORDCHANCES) + 1);
		}
	}
	
	public void applyMelodyVariation(Pattern pattern)
	{
		int numChords = pattern.chords.size();
		double variationChance = randGen.nextDouble();
		// 20% chance to just make a whole new melody
		if (variationChance < 0.2)
		{
			for (int chord = 0; chord < numChords; chord++)
			{
				pattern.melody.set(chord, generateTheme());
				pattern.notes.set(chord, generateNotes());
			}
		}
		// 50% change to regenerate just modify the last measure/chord
		else if (variationChance < 0.7)
		{
			pattern.melody.set(numChords - 1, generateTheme());
			pattern.notes.set(numChords - 1, generateNotes());
		}
		// 20% chance to just make the last note last the whole measure
		else if (variationChance < 0.9)
		{
			int pitch = Utils.pickNdxByProb(pitchProbs) + 1;
			// have to multiply by 2 here since duration is in half beats
			Note lastNote = new Note(pitch, mTimeSigNumer * 2);
			ArrayList<Note> notes = pattern.notes.get(numChords - 1);
			notes.clear();
			notes.add(lastNote);	
		}
		
	}
	
} //class SongWriter
