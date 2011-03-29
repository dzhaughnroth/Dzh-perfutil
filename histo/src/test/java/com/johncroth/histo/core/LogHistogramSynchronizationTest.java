package com.johncroth.histo.core;


import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.johncroth.histo.core.LogHistogram;
import com.johncroth.histo.core.LogHistogramCalculator;

public class LogHistogramSynchronizationTest extends Assert {
	
	static int threadCount = 30;
	LogHistogram hist;
	ExecutorService exec;
	Porker[] porkers;	

	@BeforeMethod
	void setUp() {
		hist = new LogHistogram();
		exec = Executors.newFixedThreadPool( threadCount );
		porkers = new Porker[threadCount];
		for( int i = 0; i < porkers.length; i++ ) {
			porkers[i] = new Porker();
		}
	}

	@AfterMethod
	void tearDown() throws Exception {
		exec.shutdown();
		exec.awaitTermination( 5000, TimeUnit.MILLISECONDS );
	}

	volatile boolean stopped = false;
	
	class Porker implements Runnable {
		int count;
		Random r = new Random();
		Throwable error;
		
		@Override
		public String toString() {
			return "Porker: " + count + ": " + error;
		}

		@Override
		public void run() {
			try {
				while (!stopped) {
					long start = System.currentTimeMillis();
					while (System.currentTimeMillis() - start < 1000) {
						for (int i = 0; i < 1000; i++) {
							hist.add(Math.abs(r.nextGaussian() / 100.));
							++count;
						}
					}
				}
			} catch (Throwable t) {
				error = t;
				throw new RuntimeException(t);
			}
		}
	}


	
	@Test
	public void testPork() throws Exception {
		for( Porker p : porkers ) {
			exec.execute( p );
		}
		Thread.sleep( 5000 );
		stopped = true;
		exec.awaitTermination( 3000, TimeUnit.MILLISECONDS );
		int total = 0;
		for ( Porker p : porkers ) {
			assertNull( p.error, p.toString() );
			assertTrue( p.count > 20000, "" + p.toString() );
			total += p.count;
		}
		LogHistogramCalculator calc = new LogHistogramCalculator( hist );
		assertEquals( calc.getTotalCount(), total );
		assertTrue( total > 1000000 );
	}

	@Test 
	public void testBucketCreationRace() throws Exception {
		exec.submit( new PathoRunner() );
		exec.submit( new PathoRunner() );
		exec.shutdown();
		exec.awaitTermination( 1000, TimeUnit.MILLISECONDS );
		assertEquals( pathogram.getBucketMap().size(), 1 );
		assertEquals( pathogram.getBucketMap().values().iterator().next().getCount(), 2 );		
	}
	
	static class Pathogram extends LogHistogram {
		
		AtomicBoolean firstPass = new AtomicBoolean(false);
		AtomicBoolean secondPass = new AtomicBoolean(false);

		// First call stalls until the second one completes.
		@Override
		protected Bucket makeBucket(Integer key) {
			boolean x = firstPass.getAndSet(true);
			if (!x) {
				while (!secondPass.get()) {
					try {
						Thread.sleep(25);
					} catch (InterruptedException e) {
					}
				}
			}
			secondPass.set(true);
			return super.makeBucket(key);
		}		
	}
	
	Pathogram pathogram = new Pathogram();
	
	class PathoRunner implements Runnable {

		@Override
		public void run() {
			pathogram.add( 11 );			
		}
		
	}
	
	


}
