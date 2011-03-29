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
package com.johncroth.histo.logging;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.johncroth.histo.client.EventRecorder;
import com.johncroth.histo.util.Clockwork;
import com.johncroth.histo.util.Notifier;

public class LogHistogramLogger extends Notifier<LogHistogramLogger.Listener> 
	implements EventRecorder {
	
	/** Convenient instance for getting started.
	 * 
	 * Logs to a file "histograms/appName" in the JVM's temp directory. 
	 */
	public static LogHistogramLogger instantFileLogger( String appName ) throws IOException {
		return new LogHistogramLogger( Executors.newSingleThreadScheduledExecutor(),
				10000L, new Listeners.InstantLoggingListener( appName ));				
	}
	
	public static interface Listener {
		void intervalRecorded( RecordedInterval<LogHistogramRecorder> i );
	}
	
	RollingRecorder<LogHistogramRecorder> rollingRecorder;
	ScheduledExecutorService service;
	List<Listener> listeners = Collections.synchronizedList( new ArrayList<Listener>() );
	
	public LogHistogramLogger(
			ScheduledExecutorService service, long periodInMillis,
			long initialDelayInMillis, Listener...ls) {
		rollingRecorder = RollingRecorder.forType( LogHistogramRecorder.class );
		this.service = service;
		for( Listener l : ls ) {
			addListener( l );
		}
		service.scheduleAtFixedRate(rollAndLog, initialDelayInMillis,
				periodInMillis, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Logger will roll initially at next appropriate motion of clock hands
	 * (second, minutes, etc..), and periodically thereafter. If period does not
	 * divide evenly into hours, will start immediately.
	 */

	public LogHistogramLogger( ScheduledExecutorService service, long periodInMillis, Listener...ls ) {
		this( service, periodInMillis, computeDelaytoSync( periodInMillis ), ls);
	}
	
	static long computeDelaytoSync( long periodInMillis ) {
		long nextSync = Clockwork.timeOfNextSync(periodInMillis, System.currentTimeMillis() );
		long delay = 0;
		if ( nextSync != 0 ) {
			delay = nextSync - System.currentTimeMillis();
			delay = Math.max( delay, 0 );
		}
		return delay;
	}

	Runnable rollAndLog = new Runnable() {
		
		@Override
		public void run() {
			if ( ! stopped ) {
				notifyListeners();
			}
		}
	};
	
	
	volatile boolean stopped = false;
	
	public void stop() {
		stopped = true;
		notifyListeners();
		service.shutdown();
	}
	
	void notifyListeners( ) {
		RecordedInterval<LogHistogramRecorder> ri = rollingRecorder.roll();
		for( Listener l : currentListeners() ) {
			l.intervalRecorded( ri );
		}
	}

	@Override
	public void recordEvent(String type, Number size) {
		rollingRecorder.recordEvent(type, size);
	}

}
