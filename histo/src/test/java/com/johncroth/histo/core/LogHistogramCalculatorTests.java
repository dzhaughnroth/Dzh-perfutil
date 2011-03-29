package com.johncroth.histo.core;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.johncroth.histo.core.BucketSummary;
import com.johncroth.histo.core.LogHistogram;
import com.johncroth.histo.core.LogHistogramCalculator;

public class LogHistogramCalculatorTests extends Assert {
	
	static final int SIZE = 10000; 
	LogHistogram stepHist = new LogHistogram();
	LogHistogramCalculator stepCalc;
	LogHistogram linearHist = new LogHistogram();
	LogHistogramCalculator linearCalc;
	LogHistogram empty = new LogHistogram();
	LogHistogramCalculator emptyCalc = new LogHistogramCalculator( empty );
	
	static final double CENTER = 1000001;
	List<Integer> expectedStepPers = new ArrayList<Integer>( );
	
	
	public void buildStepHist() {
		for ( int i = 0; i < 5; i++ ) {
			stepHist.add( CENTER - 1000000 );
			stepHist.add( CENTER + 1000000 );
		}
		for ( int i = 0; i < 15; i++ ) {
			stepHist.add( CENTER + 800000 );
			stepHist.add( CENTER - 800000 );
		}
		for ( int i = 0; i < 980; i++ ) {
			stepHist.add( CENTER + 300000 );
			stepHist.add( CENTER - 300000 );
		}
		for ( int i = 0; i < 8000; i++ ) {
			stepHist.add( CENTER );
		}
		expectedStepPers.add( stepHist.bucketSlotFor( CENTER - 800000. ));
		expectedStepPers.add( stepHist.bucketSlotFor( CENTER - 300000. ));
		expectedStepPers.add( stepHist.bucketSlotFor( CENTER ));
		expectedStepPers.add( stepHist.bucketSlotFor( CENTER + 300000. ));
		expectedStepPers.add( stepHist.bucketSlotFor( CENTER + 800000. ));

	}
	
	@BeforeMethod
	public void setUp() {
		stepHist = new LogHistogram();
		linearHist = new LogHistogram();
		empty = new LogHistogram();
		emptyCalc = new LogHistogramCalculator( empty );	
		expectedStepPers = new ArrayList<Integer>( );

		for( int i = 0; i < SIZE; i++ ) {
			linearHist.add( i );
		}
		buildStepHist();
		stepCalc = new LogHistogramCalculator(stepHist );
		linearCalc = new LogHistogramCalculator( linearHist );		
	}

	@Test
	public void testBasics() {
		assertSame( linearHist, linearCalc.getData() );
	}
	
	@Test
	public void testTotalAndMean() {
		// Mean is exact.
		assertEquals( SIZE, linearCalc.getTotalCount() );
		assertEquals( (SIZE - 1.0) / 2.0, linearCalc.getMean(), .0001);
		assertEquals( SIZE, stepCalc.getTotalCount() );
		assertEquals( CENTER, stepCalc.getMean(), .0001 );
		
		assertEquals( 0.0, emptyCalc.getMean() );
		assertEquals( 0, emptyCalc.getTotalCount() );
		assertEquals( 0.0, emptyCalc.getTotalWeight() );
	}
	
	@Test
	public void testBounds() {
		assertEquals( SIZE, linearCalc.getUpperBound(), .00001 );
		assertEquals( 0, linearCalc.getLowerBound(), .00001 );
		assertEquals( Double.MAX_VALUE, emptyCalc.getLowerBound() );
		assertEquals( Double.MIN_VALUE, emptyCalc.getUpperBound() );
	}
	
	@Test
	public void testQuantiles() {
		LogHistogram one = new LogHistogram();
		one.add( 100.1 );
		LogHistogramCalculator oneCalc = new LogHistogramCalculator( one );
		List<Integer> fooQuants = oneCalc.calculateDefaultQuantileBuckets();
		assertEquals( 5, fooQuants.size() );
		assertSorted( fooQuants );
		assertEquals( 20, fooQuants.get( 0 ).intValue());
		assertEquals( 20, fooQuants.get( 4 ).intValue());	
		
		// build a 2 + 1 distribution
		one.add( 100.1 );
		one.add( 1000.1 );
		oneCalc = new LogHistogramCalculator( one );
		fooQuants = oneCalc.calculateDefaultQuantileBuckets();
		assertEquals( 5, fooQuants.size() );
		assertSorted( fooQuants );
		assertEquals( 20, fooQuants.get( 0 ).intValue());
		assertEquals( 20, fooQuants.get( 2 ).intValue());
		// 95th and 99.9th %iles are different in our 2 + 1 distribution.
		assertEquals( 30, fooQuants.get( 3 ).intValue());	
		assertEquals( 30, fooQuants.get( 4 ).intValue());	
		

		List<Integer> percentiles = linearCalc.calculateDefaultQuantileBuckets();
		assertTrue( 35 < linearHist.buckets.size(), "Not enough buckets: " + linearHist.buckets.size() );
		assertEquals( LogHistogramCalculator.DEFAULT_QUANTILES.length, percentiles.size() );
		assertSorted( percentiles );
		// 10th smallest entry is 9
		assertEquals( 9, percentiles.get( 0 ).intValue() );
		assertEquals( 39, percentiles.get( 4 ).intValue());

		List<Integer> stepPers = stepCalc.calculateDefaultQuantileBuckets();
		assertEquals( expectedStepPers, stepPers );
		assertEquals( 5, stepPers.size() );	
		assertSorted( stepPers );
	}

	@Test
	public void testModal() {
		LogHistogram x = new LogHistogram();
		for( int i = 1; i < 4; i ++ ) {
			x.add( 100. + .1 * i );
			x.add( 15. + .01 * i );
		}
		for ( int i = 1; i < 6; i++ ) {
			x.add( 60. + .1 * i);
		}
		x.add( 300 );
		x.add( 1. );
		LogHistogramCalculator c = new LogHistogramCalculator( x );
		List<BucketSummary> bs = c.bucketsFor( c.modalOrder() );
		assertEquals( 5, bs.get( 0 ).getCount() );
		assertEquals( 3, bs.get( 2 ).getCount() );
		assertEquals( 1, bs.get( 3 ).getCount() );
		int lastCount = Integer.MAX_VALUE;
		for( BucketSummary b : bs ) {
			assertTrue(  b.getCount() <= lastCount, "Bucket " + b.getPlaceInHistogram() + " has count " + b.getCount() 
					+ " should be less than " + lastCount );
		}
		
		// Some more tests for BucketSummary.
		
		BucketSummary sum = bs.get( 3 );
		assertEquals( 1.0, sum.getWeight(), .00001 );
		assertEquals( 1, sum.getCount() );
		assertTrue( sum.getLowerBound() < sum.getUpperBound() );
		assertTrue( sum.toString().length() > 2 );

		assertNull( c.bucketFor( 100232 ).getPlaceInHistogram());
	}
	
	<U extends Comparable<? super U>>  void assertSorted( List<U> x ) {
		ArrayList<U> a = new ArrayList<U>( x );
		Collections.sort( a );
		assertEquals( x, a, "Not sorted: " + x );
	}
}
