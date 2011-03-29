package com.johncroth.histo.logging;

import static com.johncroth.histo.logging.LogHistogramWriter.COUNT;
import static com.johncroth.histo.logging.LogHistogramWriter.END;
import static com.johncroth.histo.logging.LogHistogramWriter.MESSAGE_START_LINE;
import static com.johncroth.histo.logging.LogHistogramWriter.RECORDER;
import static com.johncroth.histo.logging.LogHistogramWriter.START;
import static com.johncroth.histo.logging.LogHistogramWriter.WEIGHT;

import java.io.BufferedReader;
import java.io.Reader;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.johncroth.histo.core.LogHistogram;
import com.johncroth.histo.util.Notifier;

/** 
 * Records each type of event into a {@link LogHistogram} for later aggregation.
 */

public class LogHistogramParser extends Notifier<LogHistogramParser.Listener> {
	
	public enum EventType {
		IGNORED, ERROR, INTERVAL_READ
	}
	
	public static interface Listener {
		void lineParsed( EventType type, String line, Object detail );
	}

	protected void notifyListeners( EventType type, String line, Object detail ) {
		for( Listener l : currentListeners() ) {
			l.lineParsed( type, line, detail);
		}
	}
	
	public void parse(Reader reader) throws Exception {
		BufferedReader r = new BufferedReader(reader);
		boolean expectingJson = false;
		String line = r.readLine();
		while (line != null) {
			EventType type = EventType.IGNORED;
			Object detail = null;
			if (line.startsWith(MESSAGE_START_LINE)) {
				if ( expectingJson ) {
					type = EventType.ERROR;
					detail = "Duplicate message start.";
					notifyListeners( type, line, detail );
				}
				expectingJson = true;
			} else {
				if (expectingJson) {
					try {
						Object jv = JSONValue.parseWithException(line);
						RecordedInterval<LogHistogramRecorder> ri = parseIntervalJson((JSONObject) jv);
						type = EventType.INTERVAL_READ;
						detail = ri;
					} catch (Exception e) {
						type = EventType.ERROR;
						detail = e;
					}
					expectingJson = false;
				}
			}
			if ( !expectingJson ) {
				notifyListeners(type, line, detail);
			}
			line = r.readLine();
		}

	}
	
	LogHistogram parseHistogramJson( JSONObject jo ) {
		LogHistogram result = new LogHistogram();
		for( Object key : jo.keySet() ) {
			int iKey = Integer.parseInt( String.valueOf( key ) );
			JSONObject joBucket = (JSONObject) jo.get( key );
			long c = (Long) joBucket.get( COUNT );
			double w = (Double) joBucket.get( WEIGHT );
			result.setBucket( iKey, c, w );
		}
		return result;
	}

	LogHistogramRecorder parseRecorderJson( JSONObject jo ) {
		LogHistogramRecorder result = new LogHistogramRecorder();
		for( Object o : jo.keySet() ) {
			String metric = (String) o;
			LogHistogram hist = parseHistogramJson( (JSONObject) jo.get( o ) );
			result.getHistogramMap().put(metric, hist);
		}
		return result;
	}
	
	RecordedInterval<LogHistogramRecorder> parseIntervalJson(JSONObject jo) {
		long start = (Long) jo.get(START);
		long end = (Long) jo.get(END);
		LogHistogramRecorder rec = parseRecorderJson((JSONObject) jo
				.get(RECORDER));
		RecordedInterval<LogHistogramRecorder> result = new RecordedInterval<LogHistogramRecorder>(
				rec, start, end);
		return result;
	}

}
