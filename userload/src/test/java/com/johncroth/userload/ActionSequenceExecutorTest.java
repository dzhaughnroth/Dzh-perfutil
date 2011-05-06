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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.johncroth.histo.logging.UnitTestEventRecorder;
import com.johncroth.userload.ActionSequenceExecutor.CountingMonitor;


public class ActionSequenceExecutorTest extends Assert {

	JavaUtilActionSequenceExecutor exec;
	
	@BeforeMethod
	public void setUp() throws Exception {
		ScheduledExecutorService ses = Executors.newScheduledThreadPool(2);
		exec = new JavaUtilActionSequenceExecutor(ses);
		assertSame(exec.getService(), ses);
		c1.set(0);
		c2.set(0);
		// our monitor does nothing, expects nothing
		exec.getMonitor().slept( null, 0, 0 );
		exec.getMonitor().failed( null, null, null, 0 );
		exec.getMonitor().completed( null, null, -1);
		instanceCount.set( 0 );
		runCount.set( 0 );
	}

	AtomicInteger c1 = new AtomicInteger();
	AtomicInteger c2 = new AtomicInteger();
	Runnable a1 = new Runnable() {
		@Override
		public void run() {
			c1.getAndIncrement();
		}
	};
	
	Runnable a2 = new Runnable() {	
		@Override
		public void run() {
			c2.getAndIncrement();
			throw new RuntimeException();
		}
	};

	void runThreeTimes() throws Exception {
		ActionSeq seq1 = new ActionSeq( Seq.constant( a1 ), Seq.constant( 20L ) );
		ActionSeq seq2 = new ActionSeq( Seq.constant( a2 ), Seq.constant( 20L ) );
		exec.addActionSequence( seq1 );
		exec.addActionSequence( seq2 );
		Thread.sleep(35);
		exec.shutdown();	
		Thread.sleep( 20 );
		assertTrue( exec.isShutdown() );
	}
	
	@Test
	public void testWithTwoThreads() throws Exception {
		runThreeTimes();
		assertEquals( 3, c1.get() );
		assertEquals( 3, c2.get() );
	}

	@Test
	public void testMonitor() throws Exception {
		UnitTestEventRecorder recorder = new UnitTestEventRecorder();
		exec.setMonitor(new ActionSequenceExecutor.EventRecorderMonitor(
				recorder, "foo"));
		runThreeTimes();
		assertEquals(3, c1.get());
		assertEquals(3, c2.get());
		assertEquals(12, recorder.getEvents().size());
	}

	
	static AtomicInteger runCount = new AtomicInteger();
	static AtomicInteger instanceCount = new AtomicInteger();
	static class Dumb implements Runnable {
		public Dumb() {
			instanceCount.incrementAndGet();
		}
		@Override
		public void run() {
			runCount.incrementAndGet();
		}
		
	};
	
	static class Counter extends ActionSeq {
		public Counter() {
			super( Seq.finite( Seq.generator( Dumb.class ), 1 ), 10L );
		}
	}
	
	@Test
	public void testRamper() throws Exception {
		CountingMonitor monitor = new CountingMonitor();
		exec.setMonitor( monitor );
		ActionSeq z = ActionSeq.ramper( 5, 100, Counter.class, exec);
		exec.addActionSequence( z );
		Thread.sleep( 10 );
		for ( int i = 0; i < 5; i++ ) {
			assertEquals( instanceCount.get(), i + 1 );
			Thread.sleep( 100/5 );
		}
		Thread.sleep( 50 );
		assertFalse( z.hasNext() );
		assertEquals( 5, instanceCount.get() );
		assertEquals( 5, runCount.get() );
		assertEquals( 0, monitor.getFailed() );
	}
	
	@Test
	public void testUnusualRamperCases()throws Exception {
		CountingMonitor monitor = new CountingMonitor();
		exec.setMonitor( monitor );
		assertEquals( 0, monitor.getFailed() );	
		ActionSeq z = ActionSeq.ramper( 5, 100, Counter.class, null );
		exec.addActionSequence( z );
		Thread.sleep( 120 );
		assertEquals( 5, monitor.getFailed() );	

		List<ActionSequence> zero = new ArrayList<ActionSequence>();
		ActionSeq y = ActionSeq.ramper( 2, 10, zero.iterator(), exec );
		monitor = new CountingMonitor();
		exec.setMonitor( monitor );
		exec.addActionSequence( y );
		Thread.sleep( 30 );
		assertEquals( 2, monitor.getCompleted() );
		assertEquals( 0, monitor.getFailed() );
		assertEquals( 2, monitor.getSlept() );
		assertTrue( monitor.toString().length() > 15 );
		
		
	}
	
	
}
