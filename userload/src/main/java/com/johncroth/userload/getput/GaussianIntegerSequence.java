package com.johncroth.userload.getput;

import java.util.Iterator;
import java.util.Random;

public class GaussianIntegerSequence implements Iterator<Long>{
	
	long spaceSize;
	long center;
	long stdDev;

	public GaussianIntegerSequence( long spaceSize, long center, long stdDev ) {
		this.spaceSize = spaceSize;
		this.center = center;
		this.stdDev = stdDev;
		random = new Random();
	}
	
	Random random = new Random();

	@Override
	public boolean hasNext() {
		return true;
	}

	@Override
	public Long next() {
		long val;
		do {
			double x = random.nextGaussian();
			val = (long) ( x * stdDev );
		}
		while( Math.abs( val ) > spaceSize / 2L );
		return center + val;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}

	
	
}
