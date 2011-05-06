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

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Arbitrarily long lived iterators.
 */

public abstract class Seq<T> implements Iterator<T>{
	
	public boolean hasNext() {
		return true;
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}
		
	public static <T> Iterator<T> constant( final T value ) {
		return new Seq<T>() {
			public T next() {
				return value;
			}
		};
	}
	
	static Random random = new Random(0);
	
	public static Iterator<Double> gaussian(final double center,
			final double standardDeviation, final Random r) {
		return new Seq<Double>() {
			@Override
			public Double next() {
				return standardDeviation * r.nextGaussian() + center;
			}
		};
	}
	
	public static Iterator<Double> gaussian( double center, double stdDev ) {
		return gaussian(center, stdDev, random);
	}
	
	public static Iterator<Integer> range( final int minInclusive, final int maxExclusive, final Random r ) {
		return new Seq<Integer>() {
			@Override
			public Integer next() {
				return r.nextInt( maxExclusive - minInclusive ) + minInclusive;			
			}
		};
	}
	
	public static Iterator<Integer> range( int minInclusive, int maxExclusive ) {
		return range( minInclusive, maxExclusive, random );
	}
	
	public static <T> Iterator<T> finite( final Iterator<T> seq, final int maxNumber ) {

		return new Iterator<T>() {
			int count = maxNumber;
			@Override
			public boolean hasNext() {
				return seq.hasNext() && count > 0;
			}

			@Override
			public T next() {
				--count;
				return seq.next();
			}

			@Override
			public void remove() {
				seq.remove();
			}
			
		};
	}
	
	/** The sequence of results of repeated calls to a given method. */
	public static <T> Iterator<T> generator( final Callable<T> factoryMethod ) {
		return new Seq<T>() {

			@Override
			public T next() {
				try {
					return factoryMethod.call();
				} catch (Exception e) {
					throw new RuntimeException( "Seq.generator factory method failed.", e );
				}
			}
		};
	}
	
	/**
	 * Special case of {@link #generator(Callable)} where the factory
	 * method return the result of {@link Class#newInstance()}.
	 */
	
	public static <T> Iterator<T> generator( final Class<? extends T> klass ) {
		return generator( new Callable<T>() {
			@Override
			public T call() throws Exception {
				return klass.newInstance();
			}
		});
	}

}
