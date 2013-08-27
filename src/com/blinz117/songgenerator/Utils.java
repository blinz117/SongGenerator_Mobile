package com.blinz117.songgenerator;

import java.util.Random;

public class Utils {
	
	static Random randGen = new Random();

	/*
	 * returns an index into an array based on the probabilities in the array
	 * The values in the array do not have to add up to 1. They will be scaled if not.
	 * returns -1 if something goes wrong
	 */
	public static int pickNdxByProb(double[] probs)
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
	
}
