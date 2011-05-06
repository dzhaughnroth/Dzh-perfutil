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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.johncroth.histo.client.EventRecorder;

public abstract class ActionSequenceExecutor {

	private volatile Monitor monitor = NOOP;

	public void setMonitor( Monitor monitor ) {
		this.monitor = monitor;
	}

	public Monitor getMonitor( ) {
		return monitor;
	}

	public void addActionSequence( ActionSequence seq ) {
		Rescheduler r = new Rescheduler( seq, 0 );
		schedule( r, 0, TimeUnit.MILLISECONDS );
	}

	protected abstract void schedule( Runnable r, long delay, TimeUnit unit );
	
	public void shutdown() {		
	}
	
	public boolean isShutdown() {
		return false;
	}
	
	class Rescheduler implements Runnable {
		long lastSched, intendedDelay;
		ActionSequence seq;
		Rescheduler( ActionSequence seq, long delay ) {
			this.seq = seq;
			this.intendedDelay = delay;
			lastSched = System.nanoTime();
		}

		@Override
		public void run() {
			Monitor m = getMonitor();
			long start = System.nanoTime();
			long actualDelay = TimeUnit.NANOSECONDS.toMillis(start - lastSched );
			m.slept(seq, intendedDelay, actualDelay);
			Runnable r = seq.nextAction();
			try {
				r.run();
				m.completed( seq, r, TimeUnit.NANOSECONDS.toMillis( System.nanoTime() - start ));
			}
			catch( Throwable e ) {
				m.failed(seq, r, e, TimeUnit.NANOSECONDS.toMillis( System.nanoTime() - start ));
			}
			if ( seq.hasNext() ) { 
				intendedDelay = seq.nextDelay();
				lastSched = System.nanoTime();
				schedule( this, intendedDelay, TimeUnit.MILLISECONDS );
			}
		}
	}
	
	
	/** Monitor methods will be invoked concurrently */
	public static interface Monitor {

		void failed( ActionSequence seq, Runnable action, Throwable e, long elpasedMillis );
		
		void completed( ActionSequence seq, Runnable action, long elapsedMillis );
		
		void slept( ActionSequence seq, long intendedAmount, long actualAmount );
	}

	public static class CountingMonitor implements Monitor {
		AtomicInteger failed = new AtomicInteger();
		AtomicInteger completed = new AtomicInteger();
		AtomicInteger slept = new AtomicInteger();
		
		public int getFailed() {
			return failed.get();
		}
		
		public int getCompleted() {
			return completed.get();
		}
		
		public int getSlept() {
			return slept.get();
		}

		@Override
		public void failed(ActionSequence seq, Runnable action, Throwable e,
				long elpasedMillis) {
			failed.incrementAndGet();			
		}

		@Override
		public void completed(ActionSequence seq, Runnable action,
				long elapsedMillis) {
			completed.incrementAndGet();			
		}

		@Override
		public void slept(ActionSequence seq, long intendedAmount,
				long actualAmount) {
			slept.incrementAndGet();			
		}
		
		@Override
		public String toString() {
			return( "Completed: " + getCompleted() + "; Failed: " + getFailed() + "; Slept: " + getSlept() );
		}
	}
	
	public static class EventRecorderMonitor implements Monitor {
		public static final String FAILED = "-failed";
		public static final String COMPLETED = "-completed";
		public static final String SLEEP_DIFF = "-sleep-diff";
		
		public EventRecorderMonitor( EventRecorder recorder, String prefix ) {
			this.recorder = recorder;
			failedMsg = prefix + FAILED;
			completedMsg = prefix + COMPLETED;
			sleepDiffMsg = prefix + SLEEP_DIFF;
		}

		final EventRecorder recorder;
		final String failedMsg, completedMsg, sleepDiffMsg;		
		@Override
		public void failed(ActionSequence seq, Runnable action, Throwable e,
				long elapsedMillis) {
			recorder.recordEvent( failedMsg, elapsedMillis ); 			
		}

		@Override
		public void completed(ActionSequence seq, Runnable action,
				long elapsedMillis) {
			recorder.recordEvent( completedMsg, elapsedMillis );			
		}

		@Override
		public void slept(ActionSequence seq, long intendedAmount,
				long actualAmount) {
			recorder.recordEvent(sleepDiffMsg,
					Math.abs(actualAmount - intendedAmount));
		}
		
	}
	
	static Monitor NOOP = new Monitor() {
			
		@Override
		public void slept(ActionSequence seq, long intendedAmount, long actualAmount) {
		}
		
		@Override
		public void failed(ActionSequence seq, Runnable action, Throwable e, long elpasedMillis) {
		}
		
		@Override
		public void completed(ActionSequence seq, Runnable action, long elapsedMillis) {
		}
	};
	
}
