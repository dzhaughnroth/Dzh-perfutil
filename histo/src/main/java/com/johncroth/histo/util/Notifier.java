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
package com.johncroth.histo.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility managing the threading aspects of notification, such as cloning the
 * listeners list before notification. Subclasses or clients need only supply
 * a method like
 * <pre> 
 * void notifyListeners( Event yourEvent ) {
 *    for( T listener : currentListeners() ) {
 *       listener.yourNotificationMethod( yourEvent );
 *    }
 * }
 * </pre>
 * 
 * @param <T> The interface implemented by listeners.
 */
public class Notifier<T> {
	
	List<T> listeners = Collections.synchronizedList( new ArrayList<T>() );

	public void addListener( T l ) {
		listeners.add( l );
	}
	public void removeListener( T l ) {
		listeners.remove( l );
	}
	protected List<T> currentListeners( ){
		List<T> copy;
		synchronized ( listeners ) {
			copy = new ArrayList<T>( listeners );
		}
		return copy;		
	}
}
