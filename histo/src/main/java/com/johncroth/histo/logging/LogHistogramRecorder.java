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


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.johncroth.histo.client.EventRecorder;
import com.johncroth.histo.core.LogHistogram;

/** 
 * Records each type of event into a {@link LogHistogram} for later aggregation.
 */

public class LogHistogramRecorder implements EventRecorder {
	
	ConcurrentMap<String, LogHistogram> histogramMap = new ConcurrentHashMap<String,LogHistogram>();
	
	public Map<String, LogHistogram> getHistogramMap() {
		return histogramMap;
	}
	
	/** Factory method; returns generic new one here. */
	protected LogHistogram createLogHistogram( String type ) {
		return new LogHistogram();
	}

	/** Get the existing histogram for the type, or lazily create an empty one. */
	public LogHistogram getHistogram( String type ) {
		LogHistogram result = histogramMap.get( type );
		if ( result == null ) {
			result = createLogHistogram(type);
			LogHistogram x = histogramMap.putIfAbsent(type, result);
			if ( x != null ) {
				result = x;
			}
		}
		return result;
	}
	
	@Override
	public void recordEvent(String type, Number size ) {
		getHistogram(type).add( size.doubleValue() );
	}
	
	public void absorb( LogHistogramRecorder other ) {
		for( String metric : other.getHistogramMap().keySet() ) {
			LogHistogram lh = getHistogram( metric );
			lh.absorb(other.getHistogram( metric ));
		}
	}
}
