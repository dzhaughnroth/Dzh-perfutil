package com.johncroth.histo.logging;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.johncroth.histo.client.EventRecorder;

/**
 * Convenient Default recorder for tests that are small.
 */

public class UnitTestEventRecorder implements EventRecorder {
	
	public static class Event {
		Event( String t, Number s ) {
			type = t; 
			size = s;
		}
		public String type;
		public Number size;
		public String toString() {
			return type + ":\t" + size;
		}
	}
	
	List<Event> events = Collections.synchronizedList( new ArrayList<Event>() );
	LogHistogramRecorder logHistogramRecorder = new LogHistogramRecorder();

	public List<Event> getEvents() {
		return events;
	}

	public LogHistogramRecorder getLogHistogramRecorder() {
		return logHistogramRecorder;
	}

	@Override
	public void recordEvent(String type, Number size) {
		events.add( new Event( type, size ));		
		logHistogramRecorder.recordEvent( type, size );
	}
	
}
