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


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class is NOT thread-safe for reads, but is thread-safe for writes. It
 * should be used in an accumulation phase, followed by an immutable analysis
 * phase, in conjunction with {@link LogHistogramCalculator} or similar.
 */
public class LogHistogram {
	
	/** Interface of main substructure for a histogram. */
	public static interface Bucket {
		
		double getWeight();
		
		long getCount();

		/** Ensure this method is thread-safe. */
		void add( long deltaCount, double deltaWeight );
	}
	
	/** Simplest implementation of Bucket.
	 * 
	 * {@link #add(long, double)} is thread-safe, gets are not.
	 */
	public class DefaultBucket implements Bucket {
		
		long weight;
		long count;
		
		
		/** Subclasses must keep this method thread-safe! */
		public synchronized void add(long deltaCount, double deltaWeight) {
			weight += deltaWeight;
			count += deltaCount;
		}

		public long getCount() {
			return count;
		}
		
		public double getWeight() {
			return weight;
		}
		
		@Override
		public String toString() {
			return String.valueOf( weight + " from " + count + " event(s)" );
		}

	}

	
//	SortedMap<Integer,Bucket> buckets = new TreeMap<Integer, Bucket>();
	ConcurrentMap<Integer,Bucket> buckets = new ConcurrentHashMap<Integer, LogHistogram.Bucket>();
	final static double LOGBASE = Math.pow( 10, .1 );
	final static double LOGDIVISOR = Math.log( LOGBASE );
	
	double myLog( double value ) {
		return Math.log( value ) / LOGDIVISOR;
	}
	
	double myExp( double value ) {
		return Math.pow( LOGBASE, value );
	}
	
	/** Integer floor of myLog( abs( value ) ) */
	int bucketSlotFor( double value ) {
		double x = myLog( Math.abs(value) );
		return (int) x;
	}
	
	double minValueForBucket( int i ) {
		return myExp( i );
	}
	
	double maxValueForBucket( int i ) {
		return minValueForBucket( i + 1);
	}
	
	Bucket bucketFor(double value) {
		double myVal = Math.abs(value);
		Integer key = bucketSlotFor(myVal);
		Bucket b = buckets.get(key);
		if (b == null) {
			b = makeBucket(key);
			Bucket x = buckets.putIfAbsent( key, b );
			if ( x != null ) {
				b = x;
			}
		}
		return b;
	}

	/** Factory method */
	protected Bucket makeBucket(Integer key) {
		return new DefaultBucket();
	}
	
	/** Most clients will just use {@link #add(double). */
	public void add( int amount, double value ) {
		bucketFor( value ).add( amount,  value );
	}

	/**
	 * Main client method. Indicate an event happened with supplied value, e.g.,
	 * a method call ran with a given duration.
	 * 
	 * Same as add( 1, value )
	 */
	public void add( double value ) {
		add( 1, value );
	}
	
	public Map<Integer, Bucket> getBucketMap() {
		return buckets;
	}

	/** Add the contents of another histogram to this one. */
	public void absorb( LogHistogram other ) {
		for( Integer i : other.buckets.keySet() ) {
			Bucket mine = buckets.get( i );
			Bucket yours = other.buckets.get( i );
			if ( mine == null ) {
				mine = makeBucket( i );
				buckets.put( i, mine );
			}
			mine.add( yours.getCount(), yours.getWeight() );
		}
	}
	
	public void setBucket( int bucketIndex, long count, double value ) {
		Bucket b = makeBucket( bucketIndex );
		b.add( count, value);
		buckets.put( bucketIndex, b );
	}

}
