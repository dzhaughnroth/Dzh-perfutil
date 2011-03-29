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
import com.johncroth.userload.getput.IdSequence;


public class IdSequenceTest extends Assert {
	
	long stdDev = 100000;
	long spaceSize = stdDev * 10;
	long center = spaceSize * 1000;
	GaussianIntegerSequence gis = new GaussianIntegerSequence( spaceSize, center, stdDev );
	IdSequence idSeq = new IdSequence( gis );
	public List<String> next( int size ) {
		return next( idSeq, size );
	}
	
	public List<String> next( Iterator<String> iterator, int size ) {
		ArrayList<String> x = new ArrayList<String>();
		for (int i = 0; i < size; i++ ) {
			x.add( iterator.next() );
		}
		return x;
	}

	@Test
	public void testBasic() {
		// two noops
		assertTrue( idSeq.hasNext() );
		idSeq.remove();
		List<String> x = next( 10 );
		assertEquals( 10, x.size() );
		List<String> y = new ArrayList<String>( x );
		Collections.sort( x );
		assertFalse( x.equals( y ));
		for( String s : x ) {
			assertEquals( s.length(), idSeq.template.length() );
		}
	}
}
