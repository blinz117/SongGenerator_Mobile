package com.blinz117.songgenerator;

import java.util.Random;
import java.util.Vector;

public class SongWriter {

	public enum SongPart{
		VERSE, CHORUS, BRIDGE
	}
	
	public enum Pitch{
		C, C_SHARP, D, D_SHARP, E, F, F_SHARP, G, G_SHARP, A, A_SHARP, B
	}
	
	
	public SongWriter(){
	}
	
	public Song writeSong()
	{
		Song masterpiece = new Song();
		masterpiece.writeNewSong();
		return masterpiece;
	}
	
	public class Song{
		Vector<SongPart> vStructure;
		Random randGen;
		double[] baseProbs = {0.6, 0.3, 0.1};
		
		public Song(){
			randGen = new Random();
			vStructure = new Vector<SongPart>();
		}
		
		public void writeNewSong()
		{
			// Generate probabilities
			// TODO: Probably want to put this into its own method when more stuff is added
			double[] partProbs = baseProbs;
			int numPartTypes = partProbs.length;
			
			int iNumParts = randGen.nextInt(3) + 4;
			SongPart nextPart = null;
			
			for (int iPart = 0; iPart < iNumParts; iPart++)
			{
				double nextChoice = randGen.nextDouble();
				double probSum = 0.0;
				Boolean bContinueChecking = true;
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
		}
	}
}
