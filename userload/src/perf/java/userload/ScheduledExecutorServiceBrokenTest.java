package userload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

import junit.framework.TestCase;

public class ScheduledExecutorServiceBrokenTest extends TestCase {
	
	ActionSequenceExecutor exec;
	
	public void setUp() throws Exception {
		exec = new JavaUtilActionSequenceExecutor(Executors.newScheduledThreadPool( 40 ));
	}

	List<TenCounter> counters = new ArrayList<TenCounter>();
	
	public void testCounters() throws Exception {
		for ( int i = 0; i < 100; i++ ) {
			TenCounter t = new TenCounter();
			counters.add( t );
			exec.addActionSequence( t );
			Thread.yield();
			Thread.sleep( 3 );			
		}
		Thread.sleep( 75000 );
		int tooShortCount = 0;		
		for( TenCounter t : counters ) {
			List<Long> diffs = new ArrayList<Long>();
			List<Long> milliDiffs = new ArrayList<Long>();
			Iterator<Long> rdi = t.requestedDelays.iterator();
			for ( Long x : t.nanoDelays ) {
				diffs.add( x - rdi.next() );
			}
			rdi = t.requestedDelays.iterator();
			for ( Long x : t.delays ) {
				milliDiffs.add( x - rdi.next() );
			}
			assertEquals( doneCount, t.delays.size() );
//			System.out.println( t.delays );
			Iterator<Long> i = t.nanoDelays.iterator();
			int nanoFail = 0;
			for( Long delay : t.delays ) {
				long diff = delay - i.next();
				if ( Math.abs( diff ) > 10 ) {
					nanoFail++;
				}
			}
			System.out.println( diffs );
//			System.out.println( milliDiffs );
//			System.out.println( t.nanoDelays );
//			System.out.println( "Nano failures: " + nanoFail );
//			System.out.println( t.requestedDelays );
			Collections.sort( t.nanoDelays );
			Collections.sort( t.requestedDelays );
//			System.out.println( t.delays );
//			assertTrue( t.nanoDelays.get( 9 ) < 120 );
//			assertTrue( t.nanoDelays.get( 0 ) > 59 ); // sometimes as little as 47ms.
//			System.out.println( t.delays );
			if ( t.nanoDelays.get( 0 ) < 60 ) {
				System.out.println( t.delays.get( 0 ) + " is too short." );
				tooShortCount++;
			}			
		}
		assertTrue( "Too short " + tooShortCount, tooShortCount < 5 );
	}
	
	int countNumberDone() {
		int result = 0;
		for( TenCounter t : counters ) {
			if (t.isDone()) {
				++result;
			}
		}
		return result;
	}

	long counterDelay = 5;
	int doneCount = 10;


	class TenCounter implements ActionSequence {
		Random r = new Random();
		List<Long> delays = new ArrayList<Long>();
		List<Long> nanoDelays = new ArrayList<Long>();
		List<Long> requestedDelays = new ArrayList<Long>();
		long lastRun = System.currentTimeMillis();
		long lastNanoRun = System.nanoTime();
		@Override
		public boolean isDone() {
			return delays.size() >= doneCount;
		}

		@Override
		public Runnable nextAction() {
			return new Runnable() {
				public void run() {
					long now = System.currentTimeMillis();
					long nanoNow = System.nanoTime();
					try {
						Thread.sleep( counterDelay, 1 );
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					delays.add( now - lastRun );
					nanoDelays.add( (nanoNow - lastNanoRun) / 1000000L );
					lastRun = System.currentTimeMillis();
					lastNanoRun = System.nanoTime();
				}
			};
		}

		@Override
		public long nextDelay() {
			long result = r.nextInt( 40 ) + 580;
//			long result = 80;
			requestedDelays.add( result );
			return result;
		}
		
		
	}
	

	
}
