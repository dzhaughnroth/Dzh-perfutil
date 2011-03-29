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

/**
 * Decorates an EventRecorder with a {@link #roll()} capability to synchronously
 * replace a recorder with a new one.
 */

public abstract class RollingRecorder<T extends EventRecorder> implements EventRecorder {

	/** Simple implementation for EventRecorders with default constructors. */
	
	public static <U extends EventRecorder> RollingRecorder<U> forType( final Class<U> klass ) {
		// fail fast if necessary.
		try {
			klass.newInstance();
		}
		catch (Exception e) {
			throw new RuntimeException( "Could not create default instance of " + klass, e );
		}
		return new RollingRecorder<U>() {

			@Override
			protected U createEventRecorder() {
				try {
					return klass.newInstance();
				} catch (Exception e) {
					throw new RuntimeException( "Could not construct an instance of " + klass, e );
				}
			}			
		};
	}

	// Classic volatile: only 1 thread changes it, rarely.
	volatile T current;
	long lastStart;
	
	public RollingRecorder( ) {
		current = createEventRecorder();
		lastStart = System.currentTimeMillis();
	}

	public RecordedInterval<T> roll() {
		T old = current;
		long oldStart = lastStart;
		current = createEventRecorder();
		long now = System.currentTimeMillis();
		lastStart = now;
		return new RecordedInterval<T>(old, oldStart, lastStart);
	}
	
	@Override
	public void recordEvent(String type, Number size ) {
		current.recordEvent( type, size);	
	}
	
	protected abstract T createEventRecorder( );
	
	
}
