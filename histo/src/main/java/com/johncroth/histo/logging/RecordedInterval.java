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
