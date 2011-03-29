package com.johncroth.histo.core;

public class BucketSummary {
	
	public static final BucketSummary NULL;
	static {
		NULL = new BucketSummary( null );
		NULL.setCount( 0 );
		NULL.setWeight( 0. );
		NULL.setUpperBound( Double.MIN_VALUE );
		NULL.setLowerBound( Double.MAX_VALUE );
	}
	
	public BucketSummary( Integer placeInHistogram ) {
		this.placeInHistogram = placeInHistogram;
	}
	
	Integer placeInHistogram;
	long count;
	double weight;
	double lowerBound, upperBound;
	public Integer getPlaceInHistogram() {
		return placeInHistogram;
	}

	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public double getLowerBound() {
		return lowerBound;
	}
	public void setLowerBound(double lowerBound) {
		this.lowerBound = lowerBound;
	}
	public double getUpperBound() {
		return upperBound;
	}
	public void setUpperBound(double upperBound) {
		this.upperBound = upperBound;
	}
	
	public String toString() {
		return getPlaceInHistogram() + ": " + getCount();
	}
	
	
}