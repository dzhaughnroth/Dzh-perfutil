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

import java.util.Calendar;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/** 
 * Try to match the second/minutes/hours hand.
 */

public class Clockwork {
	
	Clockwork() {
		
	}
	
	static final int ONE_SECOND = 1000;
	static final int ONE_MINUTE = 60 * ONE_SECOND;
	static final int ONE_HOUR = 60 * ONE_MINUTE;
	static final int ONE_DAY = 24 * ONE_HOUR;
	
	static final SortedMap<Integer, Integer> PERIODS_MATCHED;
	
	static {
		TreeMap< Integer,Integer > map = new TreeMap<Integer, Integer>();
		map.put( ONE_SECOND, Calendar.MILLISECOND );
		map.put( ONE_MINUTE, Calendar.SECOND );
		map.put( ONE_HOUR, Calendar.MINUTE );
		map.put( ONE_DAY, Calendar.HOUR_OF_DAY );
		PERIODS_MATCHED = Collections.unmodifiableSortedMap( map );
	}
	
	/**
	 * When will the next tick of the clock hands be that matches the period to
	 * an even second, minute, or hour?
	 * 
	 * For example, if the period is 12 seconds, and it is 21.234 seconds into
	 * the current minute, we will return the time at 24 seconds. If the time
	 * was 24.3 seconds into the current minute, we would return the time at 36
	 * seconds. In either case, if the period was instead 60 seconds, we would return
	 * the next minute.
	 * 
	 * @param periodMillis
	 *            The period.
	 * @param nowMillis
	 *            The current time.
	 * @return 0 is no match is good, otherwise the timeInMillis of the next
	 *         chance to match up.
	 * 
	 */
	public static long timeOfNextSync( long periodMillis, long nowMillis ) {
		long result = 0;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis( nowMillis );
		c.set( Calendar.MILLISECOND, 0 );
		for( Integer tickSizeMillis : PERIODS_MATCHED.keySet() ) {
			c.set( PERIODS_MATCHED.get( tickSizeMillis ), 0 ); // zero out the smaller calendar value.
			if ( periodMillis < tickSizeMillis && tickSizeMillis % periodMillis == 0 ) {
				long timeFromLastTick = nowMillis - c.getTimeInMillis();
				long periodsSoFar = timeFromLastTick / periodMillis;
				result = (periodsSoFar + 1 ) * periodMillis + c.getTimeInMillis();
				break;
			}
		}
		return result;
	}
}
