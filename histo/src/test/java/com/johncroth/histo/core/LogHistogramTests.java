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
package com.johncroth.histo.core;


import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.johncroth.histo.core.LogHistogram;

public class LogHistogramTests extends Assert {
	
	LogHistogram hist;
	
	@BeforeMethod
	void setUp() {
		hist = new LogHistogram();
	}
	
	void checkBucketSlot( int expected, double value ) {
		// here we express some symmetries of the bucket function.
		assertEquals( expected, hist.bucketSlotFor( value ), "Normal for " + value);
		assertEquals( expected, hist.bucketSlotFor( -value ), "Neg for " + value );
		if ( value != 0.0 ) {
			assertEquals( -expected, hist.bucketSlotFor( 1. / value), "Inverse for " + value);
		}
	}
	
	@Test
	public void testWierdLog() {
		assertEquals( 0., hist.myLog( 1. ), .000000001);
		assertEquals( -10., hist.myLog( .1 ), .000000001);
		assertEquals( 20., hist.myLog( 100. ), .000000001);
		assertEquals( 40., hist.myLog( 10000. ), .00000000001);
		assertEquals( 5.5, hist.myExp( hist.myLog( 5.5 )), .000000000001);
		assertEquals( .1, hist.myExp( -10. ), .0000000001);
	}

	@Test
	public void testBucketSlotFor() {
		checkBucketSlot( -9, .10001);
		checkBucketSlot( -10, .0999999);
		checkBucketSlot( 20, 101. );
		checkBucketSlot( 29, 999.99 );
		checkBucketSlot( 30, 1000.01);
		checkBucketSlot( Integer.MIN_VALUE, 0. );
		
		int x = hist.bucketSlotFor( 25.5 );
		assertTrue( 25.5 > hist.minValueForBucket( x ));
		assertTrue( 25.5 < hist.maxValueForBucket( x ));

	}

	@Test
	public void testAdd() {
		assertEquals( 0, hist.buckets.size() );
		hist.add( 101 );
		hist.add( 100.1 );
		assertEquals( 1, hist.buckets.size() );
		hist.add( 0 );
		hist.add( 0 );
		assertEquals( 2, hist.buckets.size() );
		assertEquals( hist.buckets.get( Integer.MIN_VALUE ).getCount(), 2 );
		assertEquals( hist.buckets.get( 20).getCount(), 2 );
		assertNull( hist.buckets.get( 200 ) );
		assertNotNull( hist.buckets.get( 20 ).toString() );
	}

	@Test
	public void testAbsorb() {
		LogHistogram other = new LogHistogram();
		other.add( 1 );
		other.add( 10 );
		hist.add( 10 );
		hist.add( 100 );
		
		hist.absorb(other);
		assertEquals( 3, hist.getBucketMap().size() );
		assertEquals( 2, hist.buckets.get( hist.bucketSlotFor( 10 ) ).getCount() );
		assertEquals( 1, hist.buckets.get( hist.bucketSlotFor( 1 ) ).getCount() );
		assertEquals( 1, hist.buckets.get( hist.bucketSlotFor( 100 ) ).getCount() );

	}
	
	@Test
	public void testSetDirectly() {
		LogHistogram x = new LogHistogram();
		x.setBucket( 2, 23, 21.1 );
		assertEquals( 1, x.getBucketMap().size() );
		assertEquals( x.buckets.get( 2 ).getCount(), 23 );
	}
	
}
