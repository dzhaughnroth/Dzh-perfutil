/**
 * Perfutil -- https://github.com/dzhaughnroth/Dzh-perfutil 
 * (C) 2011 John Charles Roth
 *
 * Perfutil is free software, licensed under the terms of the GNU GPL 
 * Version 2 or, at your option, any later version. You should have 
 * received a copy of the license with this file. See the above web address
 * for more information, or contact the Free Software Foundation, Boston, MA. 
 * It is distributed WITHOUT WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
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
