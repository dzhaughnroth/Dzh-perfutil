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
package com.johncroth.userload.largetest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.johncroth.userload.ActionSequence;
import com.johncroth.userload.ActionSequenceExecutor;
import com.johncroth.userload.ActionSequenceExecutor.Monitor;
import com.johncroth.userload.JavaUtilActionSequenceExecutor;
import com.johncroth.userload.largetest.TestActionSequenceFactory.TestActionSequence;


public class ActionSequenceExecutorLargerTest extends Assert {
	
	ActionSequenceExecutor exec;
	
	@BeforeMethod
	public void setUp() throws Exception {
		factory = new TestActionSequenceFactory();
		exec = new JavaUtilActionSequenceExecutor(Executors
				.newScheduledThreadPool(10));
		factory.setDefaultTimesToRun(10);
		factory.setDelayTime(10);
		factory.setDelayDeviation(2);
	}

	TestActionSequenceFactory factory;
	
	void assertAllInRange(String message, Collection<? extends Number> values, Number center,
			Number deviation) {
		for (Number n : values) {
			assertEquals(center.doubleValue(), n.doubleValue(),
					deviation.doubleValue(), message + ": " + values
							+ " not all in range " + center + " " + deviation );
		}
	}

	@Test
	public void testOne() throws Exception {
		TestActionSequence seq = factory.create();
		exec.addActionSequence( seq );
		Thread.sleep( 350 );
		ArrayList<Long> rd = new ArrayList<Long>( seq.getRequestedDelays() );
		assertAllInRange( "", rd, factory.getDelayTime(), factory.getDelayDeviation() );
		ArrayList<Long> md = new ArrayList<Long>( seq.getMeasuredDelays() );
		assertAllInRange( "", rd, factory.getDelayTime(), 5 );
		System.out.println( rd );
		System.out.println( md );
		Collections.sort( md );
		System.out.println( md );
		assertEquals( factory.getDelayTime(), md.get( 0 ), 4 );
		assertEquals( factory.getDelayTime(), md.get( rd.size() - 1 ), 4 );		
		
		assertEquals( factory.getDelayTime(), rd.get( 0 ), 5 );
		assertEquals( factory.getDelayTime(), rd.get( rd.size() - 1 ), 5 );
		assertEquals( 10, seq.getTimesRun().size() );
		assertEquals( 1, factory.getSequences().size() );
		assertSame( seq, factory.getSequences().get( 0 ) );
	}
	
	@Test
	public void testTwenty() throws Exception {
		for ( int i = 0; i < 20; i++ ) {
			TestActionSequence seq = factory.create();
			seq.addTo( exec );
			Thread.sleep( 3 );
		}
		Thread.sleep( 1500 );
		for( TestActionSequence t : factory.getSequences()) {
			assertEquals( 10, t.getTimesRun().size() );
			assertAllInRange( "requestedDelays", t.getRequestedDelays(), factory.getDelayTime(), factory.getDelayDeviation() );
			List<Long> dd = t.getDelayDiffs();
			Collections.sort( dd );
			assertAllInRange( "diffs", dd.subList(1, t.getDelayDiffs().size() - 2), 0, 5 );
		}
	}

	@Test
	public void testTwentySlower() throws Exception {
		factory.setDelayTime(50);
		factory.setDelayDeviation( 5 );
		for (int i = 0; i < 20; i++) {
			TestActionSequence seq = factory.create();
			exec.addActionSequence(seq);
			Thread.sleep(3);
		}
		Thread.sleep(8000);
		for (TestActionSequence t : factory.getSequences()) {
			assertEquals(10, t.getTimesRun().size());
			assertAllInRange("requestedDelays", t.getRequestedDelays(), factory
					.getDelayTime(), factory.getDelayDeviation());
			assertAllInRange("diffs", t.getDelayDiffs().subList(1,
					t.getDelayDiffs().size() - 2), 0, 5);
		}
	}
	
	static class PathologicalTestActionSequenceFactory extends TestActionSequenceFactory {
		@Override
		protected void doSleep(long millis) throws InterruptedException {
			throw new InterruptedException();
		}
				
	}
	
	Throwable found;
	int completedCount = 0;
	int sleptCount = 0;
	@Test
	public void testPathology() throws InterruptedException {
		ScheduledExecutorService ses = Executors
		.newScheduledThreadPool(1);
		JavaUtilActionSequenceExecutor x = new JavaUtilActionSequenceExecutor( ses );
		assertSame( ses, x.getService() );
		
		assertNull( found );
		Monitor m = new Monitor() {
			@Override
			public void failed(ActionSequence seq, Runnable action,
					Throwable e, long elpasedMillis) {
				found = e;
			}

			@Override
			public void completed(ActionSequence seq, Runnable action,
					long elapsedMillis) {
				completedCount++;
			}

			@Override
			public void slept(ActionSequence seq, long intendedAmount,
					long actualAmount) {
				sleptCount++;
				
			}
		
		};
		x.setMonitor(m);
		assertSame( m, x.getMonitor() );
		PathologicalTestActionSequenceFactory pfac = new PathologicalTestActionSequenceFactory();
		pfac.setDelayTime( 10 );
		pfac.setDelayDeviation( 2 );
		x.addActionSequence( pfac.create() );
		Thread.sleep( 20 );
		assertNotNull( found );
		x.shutdown();
		Thread.sleep( 20 );
		assertTrue( x.isShutdown() );
		
		ActionSequenceExecutor stupid = new ActionSequenceExecutor( ) {

			@Override
			protected void schedule(Runnable r, long delay, TimeUnit unit) {
			}
			
		};
		
		// unimplemented
		stupid.shutdown();
		assertFalse( stupid.isShutdown() );
		
	}

}
