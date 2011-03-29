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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.johncroth.userload.getput.GaussianIntegerSequence;


public class GaussianIntegerSeqenceTest extends Assert {
	
	long stdDev = 100000;
	long spaceSize = stdDev * 10;
	long center = spaceSize * 1000;
	GaussianIntegerSequence gis = new GaussianIntegerSequence( spaceSize, center, stdDev );
	
	public List<Long> next( int size ) {
		return next( gis, size );
	}
	
	public List<Long> next( Iterator<Long> iterator, int size ) {
		ArrayList<Long> x = new ArrayList<Long>();
		for (int i = 0; i < size; i++ ) {
			x.add( iterator.next() );
		}
		return x;
	}
	
	@Test
	public void testBasic() {
		// noops courtesy of java.util.Iterator.
		assertTrue( gis.hasNext() );
		gis.remove(); 
		List<Long> x = next( 100 );
		assertEquals( 100, x.size() );
		List<Long> y = new ArrayList<Long>( x );
		Collections.sort( x );
		assertFalse( x.equals( y ));
		assertTrue( x.get( 0 ) > center - spaceSize );
		assertTrue( x.get( 99 ) < center + spaceSize );
	}
	
	@Test
	public void testGaussianness() {
		int count = 10000;
		List<Long> x = next( count );
		Collections.sort( x );
		
		assertTrue( x.get( count / 100 ) < center - 2 * stdDev );
		assertTrue( x.get( count - count / 100 ) > center + 2 * stdDev );
		assertTrue( x.get( count / 10 ) < center - stdDev );
		assertTrue( x.get( count - count / 10 ) > center + stdDev );
		assertTrue( x.get( count / 10 * 4 ) > center - stdDev );
		assertTrue( x.get( count / 10 * 6 ) < center + stdDev );
	}
	
	@Test
	public void testBounded() {
		GaussianIntegerSequence other = new GaussianIntegerSequence(spaceSize, 0, spaceSize);
		int count = 10000;
		List<Long> x = next( other, count );
		Collections.sort( x );
		assertTrue( x.get( 0 ) < spaceSize / 2 );
		assertTrue( x.get( x.size() - 1 ) < spaceSize / 2 );
	}
	
}
