package com.johncroth.histo.core;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.johncroth.histo.core.LogHistogram.Bucket;

public class LogHistogramCalculator {

	LogHistogram data;
	double totalWeight = 0.0;
	long totalCount = 0;
	Integer smallestIndex = Integer.MAX_VALUE;
	Integer largestIndex = Integer.MIN_VALUE;
	BucketSummary smallest = BucketSummary.NULL;
	BucketSummary largest = BucketSummary.NULL;
	List<Integer> sortedKeys;

	public LogHistogramCalculator( LogHistogram data ) {
		this.data = data;
		calculateTotals();
	}

	public LogHistogram getData() {
		return data;
	}

	void calculateTotals() {
		totalCount = 0;
		ArrayList<Integer> keys = new ArrayList<Integer>();
		for( Integer i : data.buckets.keySet() ) {
			Bucket b = data.buckets.get( i );
			totalCount += b.getCount();
			totalWeight += b.getWeight();
			smallestIndex = Math.min( i, smallestIndex );
			largestIndex = Math.max( i,largestIndex );
			keys.add( i );
		}
		Collections.sort( keys );
		sortedKeys = Collections.unmodifiableList( keys );
		smallest = bucketFor( smallestIndex );
		largest = bucketFor( largestIndex );
	}
	
	/** 
	 * Compute the index of the buckets for a set of percentiles.
	 * @param percentileValues must be in ascending order, between 0 and 1, such as {.001,.05,.5,.95,.999} 
	 * @return equal length list of the indexes of buckets in which the percentile division lies.
	 */
	public List<Integer> calculateQuantileBuckets( double[] percentileValues ) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		// depends on calculateTotals being called first.
		long countSoFar = 0;
		int index = 0;
		for( Integer i : sortedKeys ) {
			Bucket b = data.buckets.get( i );
			countSoFar += b.getCount();
			while ( index < percentileValues.length && 
					countSoFar >= percentileValues[index] * (double) totalCount ) {
				result.add( i );
				index++;
			}
		}
		return result;
	}
	
	Comparator<Integer> modalComparator = new Comparator<Integer>() {

		@Override
		public int compare(Integer o1, Integer o2) {
			Bucket b1 = getData().buckets.get( o1 );
			Bucket b2 = getData().buckets.get( o2 );
			return new Long( b2.getCount() ).compareTo( b1.getCount() );
		}
	};
	
	/**
	 * Return a list of buckets indexes with those with highest count first. A
	 * sort of statistical "mode." Apply {@link #bucketsFor(Collection)} to
	 * result for richer detail.
	 */
	public List<Integer> modalOrder() {
		List<Integer> result = new ArrayList<Integer>( sortedKeys );
		Collections.sort( result, modalComparator );
		return result;
	}

	public static final double[] DEFAULT_QUANTILES = new double[] {.001, .05, .5, .95, .999 };	

	/**
	 * Same as calculateQuantileBuckets with {.001, .05., .5, .95, .999} as
	 * parameters. Apply {@link #bucketsFor(Collection)} to result for richer detail.
	 */
	public List<Integer> calculateDefaultQuantileBuckets() {
		return calculateQuantileBuckets( DEFAULT_QUANTILES );
	}
		
	/** Sum of the counts of all the buckets in the {@link LogHistogram}. */
	public long getTotalCount() {
		return totalCount;
	}
	
	/**
	 * Sum of the weights of all the buckets in the {@link LogHistogram}. Mainly
	 * useful for mean.
	 */
	
	public double getTotalWeight() {
		return totalWeight;
	}
	
	/** The exact mean, that is, totalWeight / totalCount, Or 0.0 for an empty histogram.*/
	public double getMean() {
		double result = 0.0;
		if ( totalCount > 0 ) {
			result = totalWeight / (double) totalCount;
		}
		return result;
	}
	
	/**
	 * Create a view of the bucket with the given index and some summary data
	 * for that bucket. See {@link BucketSummary}.
	 */
	public BucketSummary bucketFor(Integer slot) {
		BucketSummary result = null;
		LogHistogram x = getData();
		Bucket b = x.buckets.get(slot);
		if (b != null) {
			result = new BucketSummary(slot);
			result.setCount(b.getCount());
			result.setWeight(b.getWeight());
			result.setLowerBound(x.minValueForBucket(slot));
			result.setUpperBound(x.maxValueForBucket(slot));
		}
		else {
			result = BucketSummary.NULL;
		}
		return result;

	}
	/** Create a list of views from a set of index values.
	 * 
	 *  E.g., <code>this.bucketsFor( this.calculateDefaultQuantileBuckets()</code>
	 */
	public List<BucketSummary> bucketsFor( Collection<Integer> indexes ) {
		ArrayList<BucketSummary> result = new ArrayList<BucketSummary>();
		for ( Integer i : indexes ) {
			result.add( bucketFor( i ) );			
		}
		return result;
	}
	
	/** 
	 * Smallest number bigger known to be greater than all recorded values. 
	 * 
	 * If the histogram is empty, that number is Double.MIN_VALUE, small indeed.
	 */
	public double getUpperBound() {
		return largest.getUpperBound();
	}

	/** 
	 * Largest number known to be less than all recorded values. 
	 * 
	 * If the histogram is empty, that number is Double.MAX_VALUE, large indeed.
	 */
	public double getLowerBound() {
		return smallest.getLowerBound();
	}
		
}
