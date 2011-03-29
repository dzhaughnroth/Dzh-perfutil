package com.johncroth.histo.logging;

import com.johncroth.histo.client.EventRecorder;

public class RecordedInterval<T extends EventRecorder> {

	public RecordedInterval( T old, long startMillis, long endMillis) {
		this.recorder = old;
		this.start = startMillis;
		this.end = endMillis;
	}
	
	T recorder;
	long start, end;

	public T getRecorder() {
		return recorder;
	}

	public long getStartMillis() {
		return start;
	}

	public long getEndMillis() {
		return end;
	}
	
	public long getElapsedMillis() {
		return end - start;
	}
	
	public String toString() {
		return "RI[ " + getElapsedMillis() + "]";
	}
	
}
