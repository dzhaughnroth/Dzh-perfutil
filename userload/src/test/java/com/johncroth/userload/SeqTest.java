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
package com.johncroth.userload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import org.testng.Assert;
import org.testng.annotations.Test;


public class SeqTest extends Assert {

	@Test
	public void testConstant() {
		String foo = "foo";
		Iterator<String> x = Seq.constant( "foo" );
		for ( int i = 0; i < 10; i++ ) {
			assertSame( foo, x.next() );
			assertTrue( x.hasNext() );
		}
		try {
			x.remove();
			fail();
		}
		catch( UnsupportedOperationException e ) {
		}
	}
	
	@Test
	public void testRange() {
		int[] counts = new int[12];		
		Iterator<Integer> x = Seq.range( 2, 12 );
		for ( int i = 0; i < 10000; i++ ) {
			++counts[x.next()];
		}
		for ( int i = 2; i < 12; i++ ) {
			assertTrue( counts[i] > 900 && counts[i] < 1100, "Count " + i + " was " + counts[i] );
		}
		assertEquals( 0, Math.max( counts[1], counts[0] ) );
		Random r1 = new Random( 0 );
		Random r2 = new Random( 0 );
		x = Seq.range( 1, 10001, r1);
		for ( int i = 0; i < 100; i++ ) {
			assertEquals( r2.nextInt( 10000 ) + 1, x.next().intValue() );
		}
	}
	
	@Test
	public void testGaussian() {
		Random r1 = new Random(0);
		Random r2 = new Random(0);
		Iterator<Double> x = Seq.gaussian(100, 10, r1);
		for (int i = 0; i < 100; i++) {
			assertEquals(x.next(), 100. + 10. * r2.nextGaussian());
		}
		// exercise constructor
		int[] stdDevBuckets = new int[4];
		x = Seq.gaussian(100, 10);
		int n = 10000;
		for (int i = 0; i < n; i++) {
			double u = Math.abs(100 - x.next());
			int bucket = (int) (u / 10.);
			++stdDevBuckets[Math.min(bucket, 3)];
		}
		double[] percentiles = { .32, .05, .003 };
		int soFar = stdDevBuckets[0];
		for (int i = 0; i < percentiles.length; i++) {
			assertEquals(n - soFar, n * percentiles[i],
					n * percentiles[i] / 5., "for " + i + " got " + soFar);
			soFar += stdDevBuckets[i + 1];
		}
	}

	int count( Iterator<?> i ) {
		int count = 0;
		while( i.hasNext() ) {
			i.next();
			++count;
		}	
		return count;
	}
	
	@Test 
	public void testFinite() {
		Iterator<String> ss = Seq.finite( Seq.constant( "foo" ), 3 );
		assertEquals( count( ss ), 3 );
		List<String> x = new ArrayList<String>( Arrays.asList( "foo", "bar", "baz" ) );		
		ss = Seq.finite( x.iterator(), 4 );
		ss.next();
		ss.remove();
		assertEquals( count( ss ), 2 );
	}

	static class Silly {
		public Silly() {
			throw new RuntimeException( "Sabotage" );
		}
	}
	
	@Test 
	public void testGenerator() {
		Iterator<Random> rs = Seq.generator(Random.class );
		for( int i = 0; i < 3; i++ ) {
			assertSame( rs.next().getClass(), Random.class );
		}
		Iterator<String> ss = Seq.generator( new Callable<String>() {

			@Override
			public String call() throws Exception {
				throw new Exception( "Sabotage" );
			}} );
		try {
			ss.next();
		}
		catch( Exception e ) {
			assertEquals( "Sabotage", e.getCause().getMessage() );
		}
	}
}
