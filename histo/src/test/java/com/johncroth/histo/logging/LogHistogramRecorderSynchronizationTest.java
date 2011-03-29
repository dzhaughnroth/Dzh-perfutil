package com.johncroth.histo.logging;


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
import com.johncroth.histo.logging.LogHistogramRecorder;

public class LogHistogramRecorderSynchronizationTest extends Assert {
	
	static int threadCount = 30;
	LogHistogramRecorder rec;
	ExecutorService exec;
	Porker[] porkers;	

	@BeforeMethod
	void setUp() {
		rec = new LogHistogramRecorder();
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
							rec.recordEvent( "foo", (Math.abs(r.nextGaussian() / 100.)));
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
		LogHistogramCalculator calc = new LogHistogramCalculator( rec.getHistogram( "foo" ) );
		assertEquals( calc.getTotalCount(), total );
		assertTrue( total > 1000000 );
	}

	@Test 
	public void testBucketCreationRace() throws Exception {
		exec.submit( new PathoRunner() );
		exec.submit( new PathoRunner() );
		exec.shutdown();
		exec.awaitTermination( 1000, TimeUnit.MILLISECONDS );
		assertEquals( pathocorder.getHistogramMap().size(), 1 );
		LogHistogram h = pathocorder.getHistogramMap().values().iterator().next();
		assertEquals( new LogHistogramCalculator( h ).getTotalCount(), 2 );		
	}
	
	static class Pathocorder extends LogHistogramRecorder {
		
		AtomicBoolean firstPass = new AtomicBoolean(false);
		AtomicBoolean secondPass = new AtomicBoolean(false);

		// First call stalls until the second one completes.
		@Override
		protected LogHistogram createLogHistogram( String type ) {
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
			return super.createLogHistogram( type );
		}		
	}
	
	Pathocorder pathocorder = new Pathocorder();
	
	class PathoRunner implements Runnable {

		@Override
		public void run() {
			pathocorder.recordEvent( "foo", 11 );
		}
		
	}
	
	


}
