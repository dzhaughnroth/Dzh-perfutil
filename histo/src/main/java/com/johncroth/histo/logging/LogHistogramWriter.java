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
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.json.simple.JSONObject;

import com.johncroth.histo.core.LogHistogram;
import com.johncroth.histo.core.LogHistogram.Bucket;

/** 
 * Records each type of event into a {@link LogHistogram} for later aggregation.
 */

public class LogHistogramWriter {
	
	public static final String RECORDER = "recorder";
	public static final String END = "end";
	public static final String START = "start";
	public static final String WEIGHT = "weight";
	public static final String COUNT = "count";

	public final static String MESSAGE_START_LINE = RecordedInterval.class.getCanonicalName() + " JSON:";
	public static Formatter createJavaLoggingFormatter() {
		Formatter result = new Formatter() {
			
			@Override
			public String format(LogRecord record) {
				return record.getMessage() + "\n";
			}
		};
		return result;
	}

	@SuppressWarnings("unchecked")
	JSONObject convert( LogHistogram x ) {
		JSONObject jo = new JSONObject();
		for ( Map.Entry<Integer, Bucket> entry : x.getBucketMap().entrySet()) {
			JSONObject b = new JSONObject();
			b.put( COUNT, entry.getValue().getCount() );
			b.put( WEIGHT, entry.getValue().getWeight() );
			jo.put( entry.getKey(), b );
		}
		return jo;
	}
	
	@SuppressWarnings("unchecked")
	JSONObject convert( LogHistogramRecorder rec ) {
		JSONObject jo = new JSONObject();
		for( String key : rec.getHistogramMap().keySet() ) {
			jo.put( key, convert( rec.getHistogramMap().get(key) ) );
		}
		return jo;
	}
	
	@SuppressWarnings("unchecked")
	JSONObject convert( RecordedInterval<LogHistogramRecorder> interval ) {
		JSONObject jo = new JSONObject();
		jo.put( START, interval.getStartMillis() );
		jo.put( END, interval.getEndMillis() );
		jo.put( RECORDER, convert( (LogHistogramRecorder) interval.getRecorder() ) );
		return jo;
	}
	
	public String formatJsonOnly( RecordedInterval<LogHistogramRecorder> recorder ) {
		return convert(recorder).toString();
	}

	public String formatMessage( RecordedInterval<LogHistogramRecorder> ri ) {
		return  MESSAGE_START_LINE + "\n" + formatJsonOnly( ri );
	}
}
