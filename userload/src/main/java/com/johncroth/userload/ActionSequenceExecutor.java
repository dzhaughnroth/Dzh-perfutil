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
