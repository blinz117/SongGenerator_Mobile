package com.blinz117.songgenerator;

import java.util.ArrayList;

import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.meta.Tempo;
import com.leff.midi.event.meta.TimeSignature;

public class MidiManager {

	public MidiManager()
	{
	}
	
	public MidiFile generateTempMidi(Song song, int timeSigNum, int timeSigDenom) {
		
		int eigthNote = 240; // Still need to figure out why this value works... is it the resolution below?
		
		// 1. Create some MidiTracks
		MidiTrack tempoTrack = new MidiTrack();
		MidiTrack noteTrack = new MidiTrack();
		
		// 2. Add events to the tracks
		// 2a. Track 0 is typically the tempo map
		TimeSignature ts = new TimeSignature();
		ts.setTimeSignature(timeSigNum, timeSigDenom, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);
		
		Tempo t = new Tempo();
		t.setBpm(120);
		
		tempoTrack.insertEvent(ts);
		tempoTrack.insertEvent(t);
		
		int channel = 0;
		int pitch = 60;
		int currBeat = 0;
		ArrayList<Integer> rhythm1 = song.rhythm1;
		for (int ndx = 0; ndx < rhythm1.size(); ndx++)
		{
			int velocity = 100;
			int numEigthNotes = rhythm1.get(ndx);
			// handle rests
			if (numEigthNotes < 0)
			{
				numEigthNotes *= -1;
				velocity = 0;
			}
			
			noteTrack.insertNote(channel, pitch, velocity, currBeat * eigthNote, eigthNote * numEigthNotes);
			currBeat += numEigthNotes;
		}
		
		ArrayList<Integer> rhythm2 = song.rhythm2;
		for (int ndx = 0; ndx < rhythm1.size(); ndx++)
		{
			int velocity = 100;
			int numEigthNotes = rhythm2.get(ndx);
			// handle rests
			if (numEigthNotes < 0)
			{
				numEigthNotes *= -1;
				velocity = 0;
			}
			
			noteTrack.insertNote(channel, pitch, velocity, currBeat * eigthNote, eigthNote * numEigthNotes);
			currBeat += numEigthNotes;
		}
		// It's best not to manually insert EndOfTrack events; MidiTrack will
		// call closeTrack() on itself before writing itself to a file
		
		// 3. Create a MidiFile with the tracks we created
		ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
		tracks.add(tempoTrack);
		tracks.add(noteTrack);
		
		MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);
		
		return midi;
	}
	
	public MidiFile generateChordMidi(Song song, int timeSigNum, int timeSigDenom) {
		
		int qtrNote = 480; // Still need to figure out why this value works... is it the resolution below?
		
		// 1. Create some MidiTracks
		MidiTrack tempoTrack = new MidiTrack();
		MidiTrack noteTrack = new MidiTrack();
		
		// 2. Add events to the tracks
		// 2a. Track 0 is typically the tempo map
		TimeSignature ts = new TimeSignature();
		ts.setTimeSignature(timeSigNum, timeSigDenom, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);
		
		Tempo t = new Tempo();
		t.setBpm(120);
		
		tempoTrack.insertEvent(ts);
		tempoTrack.insertEvent(t);
		
		int channel = 0;
		int basePitch = 60;
		int velocity = 100;
		ArrayList<Integer> chords = song.verseChords;
		ArrayList<Integer> themeNotes = song.theme;
		for (int ndx = 0; ndx < chords.size(); ndx++)
		{
			int root = chords.get(ndx);
			int[] triad = SongStructure.generateTriad(root);
			
			for (int interval = 0; interval < 3; interval++)
			{
				noteTrack.insertNote(channel, basePitch + triad[interval], velocity, ndx * qtrNote * timeSigNum, qtrNote * timeSigNum);
			}
			
			/*for (int melodyNote = 0; melodyNote < themeNotes.size(); melodyNote++)
			{
				int timeStart = (ndx * qtrNote * timeSigNum) + (qtrNote * melodyNote);
				int pitch = basePitch + root + themeNotes.get(melodyNote) + 12;
				noteTrack.insertNote(channel + 1, pitch, velocity + 20, timeStart, qtrNote);
			}*/
			
			themeNotes = song.melody.get(ndx);
			for (int melodyNote = 0; melodyNote < themeNotes.size(); melodyNote++)
			{
				int timeStart = (ndx * qtrNote * timeSigNum) + (qtrNote * melodyNote);
				int pitch = basePitch + SongStructure.MAJORSCALEINTERVALS[(root + themeNotes.get(melodyNote)) % 7] + 12;
				noteTrack.insertNote(channel + 1, pitch, velocity + 20, timeStart, qtrNote);
			}

		}
		
		// It's best not to manually insert EndOfTrack events; MidiTrack will
		// call closeTrack() on itself before writing itself to a file
		
		// 3. Create a MidiFile with the tracks we created
		ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
		tracks.add(tempoTrack);
		tracks.add(noteTrack);
		
		MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);
		
		return midi;
	}
	
	public MidiFile example()
	{
		
		// 1. Create some MidiTracks
		MidiTrack tempoTrack = new MidiTrack();
		MidiTrack noteTrack = new MidiTrack();
		MidiTrack bassTrack = new MidiTrack();
		
		// 2. Add events to the tracks
		// 2a. Track 0 is typically the tempo map
		TimeSignature ts = new TimeSignature();
		ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);
		
		Tempo t = new Tempo();
		t.setBpm(60);
		
		tempoTrack.insertEvent(ts);
		tempoTrack.insertEvent(t);
		
		// 2b. Track 1 will have some notes in it
		for(int i = 0; i < 20; i++) {
			
			int channel = 0, pitch = 41 + i, velocity = 100;
			NoteOn on = new NoteOn(i*480, channel, pitch, velocity);
			NoteOff off = new NoteOff(i*480 + 120, channel, pitch, 0);
			
			noteTrack.insertEvent(on);
			noteTrack.insertEvent(off);
			
			// There is also a utility function for notes that you should use instead of the above.
			noteTrack.insertNote(channel, pitch + 2, velocity, i*480, 120);
			
			bassTrack.insertNote(channel, pitch - 10, velocity, i*480 + 125, 115);
		}
		
		// It's best not to manually insert EndOfTrack events; MidiTrack will
		// call closeTrack() on itself before writing itself to a file
		
		// 3. Create a MidiFile with the tracks we created
		ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
		tracks.add(tempoTrack);
		tracks.add(noteTrack);
		tracks.add(bassTrack);
		
		MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);
		
		return midi;
		
/*		// 4. Write the MIDI data to a file
		File output = new File(context.getFilesDir(), "tempOut.mid");
		try {
			midi.writeToFile(output);
		} catch(IOException e) {
			System.err.println(e);
		}*/
	}
}
