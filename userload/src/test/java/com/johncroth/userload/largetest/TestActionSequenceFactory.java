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
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.johncroth.userload.ActionSequence;
import com.johncroth.userload.ActionSequenceExecutor;

// ActionSeq is a better way to create sequences, not that this is illegit.

public class TestActionSequenceFactory {

	public int getBusyTime() {
		return busyTime;
	}

	public void setBusyTime(int busyTime) {
		this.busyTime = busyTime;
	}

	public int getBusyDeviation() {
		return busyDeviation;
	}

	public void setBusyDeviation(int busyDeviation) {
		this.busyDeviation = busyDeviation;
	}

	public int getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(int delayTime) {
		this.delayTime = delayTime;
	}

	public int getDelayDeviation() {
		return delayDeviation;
	}

	public void setDelayDeviation(int delayDeviation) {
		this.delayDeviation = delayDeviation;
	}

	int defaultTimesToRun = -1;
	public int getDefaultTimesToRun() {
		return defaultTimesToRun;
	}

	public void setDefaultTimesToRun(int defaultTimesToRun) {
		this.defaultTimesToRun = defaultTimesToRun;
	}

	int busyTime = 10;
	int busyDeviation = 1;
	int delayTime = 1000;
	int delayDeviation = 50;
	Random rand = new Random(0);
	List<TestActionSequence> sequences = new ArrayList<TestActionSequence>();
	
	public List<TestActionSequence> getSequences() {
		return sequences;
	}

	protected int nextSleepTime() {
		return busyTime - busyDeviation + 2 * rand.nextInt( busyDeviation );
	}
	
	protected int nextDelayTime() {
		return delayTime - delayDeviation + 2 * rand.nextInt( delayDeviation );
	}

	protected void sleep( long millis ) {
		try {
			doSleep( millis );
		} catch (InterruptedException e) {
			throw new RuntimeException( "Interrupted.", e );
		}
	}

	protected void doSleep( long millis ) throws InterruptedException {
		Thread.sleep( millis );
	}
	
	public TestActionSequence create() {
		TestActionSequence result = new TestActionSequence();
		result.setTimesToRun( defaultTimesToRun );
		sequences.add( result );
		return result;
	}
	
	public class TestActionSequence implements ActionSequence {
		
		public int getTimesToRun() {
			return timesToRun;
		}

		public void setTimesToRun(int timesToRun) {
			this.timesToRun = timesToRun;
		}

		public List<Long> getTimesScheduled() {
			return timesScheduled;
		}

		public List<Long> getTimesRun() {
			return timesRun;
		}

		public List<Long> getRequestedSleeps() {
			return requestedSleeps;
		}

		public List<Long> getRequestedDelays() {
			return requestedDelays;
		}

		public List<Long> getMeasuredDelays() {
			return measuredDelays;
		}

		public List<Long> getRunLengths() {
			return runLengths;
		}
		
		public List<Long> getDelayDiffs() {
			ArrayList<Long> result = new ArrayList<Long>();
			for( int i = 0; i < requestedDelays.size(); i++ ) {
				result.add( measuredDelays.get( i ) - requestedDelays.get( i ));
			}
			return result;
		}
		public List<Long> getSleepDiffs() {
			ArrayList<Long> result = new ArrayList<Long>();
			for( int i = 0; i < requestedSleeps.size(); i++ ) {
				result.add( runLengths.get( i ) - requestedSleeps.get( i ) );
			}
			return result;
		}

		List<Long> timesScheduled = new ArrayList<Long>();
		List<Long> timesRun = new ArrayList<Long>();
		List<Long> requestedSleeps = new ArrayList<Long>();
		List<Long> requestedDelays = new ArrayList<Long>();
		List<Long> measuredDelays = new ArrayList<Long>();
		List<Long> runLengths = new ArrayList<Long>();
		
		int timesToRun = -1;
		@Override
		public boolean hasNext() {
			return timesToRun <= 0 || timesRun.size() < timesToRun;
		}

		public void addTo( ActionSequenceExecutor e ) {
			timesScheduled.add( System.nanoTime() );
			e.addActionSequence( this );
		}
		
		@Override
		public Runnable nextAction() {

			return new Runnable() {

				@Override
				public void run() {
					long runStart = System.nanoTime();
					measuredDelays.add(TimeUnit.NANOSECONDS.toMillis(runStart
							- timesScheduled.get(timesScheduled.size() - 1)));
					act();
					timesRun.add(runStart);
					runLengths.add(TimeUnit.NANOSECONDS.toMillis(System
							.nanoTime()
							- runStart));
				}
			};
		}

		protected void act( ) {
			long sleepTime = nextSleepTime();
			requestedSleeps.add(sleepTime);
			if (sleepTime > 0) {
				sleep(sleepTime);
			}			
		}
		
		@Override
		public Long nextDelay() {
			long delay = nextDelayTime();
			requestedDelays.add(delay);
			timesScheduled.add(System.nanoTime());
			return delay;
		}
	}

}
