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

public abstract class ActionSequenceExecutor {
	
	public static interface ErrorHandler {
		void handleError( ActionSequence seq, Throwable t );
	}
	
	public ActionSequenceExecutor( DelayGovernor guvnuh ) {
		delayGovernor = guvnuh;
	}

	DelayGovernor delayGovernor;
	ErrorHandler errorHandler = new ErrorHandler() {

		@Override
		public void handleError(ActionSequence seq, Throwable t) {
		}
		
	};

	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	class Rescheduler implements Runnable {
		long lastSched, intendedDelay, requestedDelay;
		ActionSequence seq;
		Rescheduler( ActionSequence seq, long delay ) {
			this.seq = seq;
			this.intendedDelay = delay;
			this.requestedDelay = 0;
			lastSched = System.nanoTime();
		}

		@Override
		public void run() {
			long actualDelay = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastSched );
			delayGovernor.reportDelay( intendedDelay, requestedDelay, actualDelay ); 
			try {
				seq.nextAction().run();
			}
			catch( Throwable e ) {
				errorHandler.handleError( seq, e );
			}
			if ( ! seq.isDone() ) { 
				intendedDelay = seq.nextDelay();
				requestedDelay = delayGovernor.recommendActualDelay( intendedDelay );
				lastSched = System.nanoTime();
				schedule( this, requestedDelay, TimeUnit.MILLISECONDS );
			}
		}

	}


	public void addActionSequence( ActionSequence seq ) {
		long delay = delayGovernor.recommendActualDelay(seq.nextDelay());
		Rescheduler r = new Rescheduler( seq, delay );
		schedule( r, delay, TimeUnit.MILLISECONDS );
	}

	protected abstract void schedule( Runnable r, long delay, TimeUnit unit );
	
	public void shutdown() {		
	}
	
	public boolean isShutdown() {
		return false;
	}
}
