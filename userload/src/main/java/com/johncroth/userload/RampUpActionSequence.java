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

/** 
 * Gradually adds {@link ActionSequence} from {@link #create()} method to {@link #executor} 
 * until {@link peakCount} is reached. Then waits awhile, then stops its executor.
 * 
 * @author jroth
 *
 */

public abstract class RampUpActionSequence implements ActionSequence, Runnable {
	
	protected RampUpActionSequence( ActionSequenceExecutor exec ) {
		executor = exec;
	}

	ActionSequenceExecutor executor;
	int peakCount = 100;
	int secondsToRampUp = 30, secondsToHold = 60;
	int countSoFar = 0;
	
	public ActionSequenceExecutor getExecutor() {
		return executor;
	}

	public int getPeakCount() {
		return peakCount;
	}

	public void setPeakCount(int peakCount) {
		this.peakCount = peakCount;
	}

	public int getSecondsToRampUp() {
		return secondsToRampUp;
	}

	public void setSecondsToRampUp(int secondsToRampUp) {
		this.secondsToRampUp = secondsToRampUp;
	}

	public int getSecondsToHold() {
		return secondsToHold;
	}

	public void setSecondsToHold(int secondsToHold) {
		this.secondsToHold = secondsToHold;
	}

	public int getCountSoFar() {
		return countSoFar;
	}

	protected abstract ActionSequence create();
	
	boolean done = false;
	
	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public Runnable nextAction() {
		return this;
	}

	volatile boolean finishedRamping = false;

	@Override
	public long nextDelay() {
		long result = (1000L * secondsToRampUp) / peakCount;
		if ( countSoFar >= peakCount ) {
			result = 1000L * secondsToHold; 
			finishedRamping = true;
		}
		return result;
	}

	@Override
	public void run() {
		if ( !finishedRamping ) {
			if ( !executor.isShutdown() ) {
				executor.addActionSequence( create() );		
				++countSoFar;
			}
		}
		else {
			executor.shutdown();
			done = true;
		}
	}
	
}
