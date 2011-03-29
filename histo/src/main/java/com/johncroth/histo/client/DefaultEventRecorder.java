package com.johncroth.histo.client;



import java.io.IOException;

import com.johncroth.histo.logging.LogHistogramLogger;

/**
 * A singleton {@link EventRecorder} with static access and a pluggable
 * delegate. The default delegate does nothing; call
 * {@link DefaultEventRecorder#useInstantLogHistogramLogger(String)} for a
 * simple file logger.
 * 
 * Dependency injection is much better than this static access as a policy; but
 * this maybe easier to use in the short term.
 */
public class DefaultEventRecorder {
	
	private DefaultEventRecorder() {
		
	}
	static {
		new DefaultEventRecorder(); //code coverage!
	}
	
	/**
	 * Makes the default the @link{ {@link LogHistogramLogger#instantFileLogger(String) }
	 * @throws IOException 
	 */
	public static void useInstantLogHistogramLogger( String appName ) throws IOException {
		setDefault( LogHistogramLogger.instantFileLogger(appName));
	}
	
	public static EventRecorder get() {
		return impl;
	}
	
	final static EventRecorder impl = new EventRecorder() {
		@Override
		public void recordEvent(String type, Number size) {
			delegate.recordEvent(type, size);
			
		}
	};
	
	static volatile EventRecorder delegate = new EventRecorder() {
		
		@Override
		public void recordEvent(String type, Number size) {
		}
	};
	
	public static void setDefault( EventRecorder recorder ) {
		delegate = recorder;
	}
}
