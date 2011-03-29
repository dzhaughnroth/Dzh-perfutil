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