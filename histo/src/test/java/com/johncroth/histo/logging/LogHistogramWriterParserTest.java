package com.johncroth.histo.logging;


import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.johncroth.histo.core.LogHistogram;
import com.johncroth.histo.core.LogHistogramCalculator;
import com.johncroth.histo.logging.LogHistogramParser;
import com.johncroth.histo.logging.LogHistogramRecorder;
import com.johncroth.histo.logging.LogHistogramWriter;
import com.johncroth.histo.logging.RecordedInterval;
import com.johncroth.histo.logging.LogHistogramParser.EventType;

/** 
 * Records each type of event into a {@link LogHistogram} for later aggregation.
 */

public class LogHistogramWriterParserTest extends Assert {

	LogHistogramRecorder makeRecorder() {
		LogHistogramRecorder result = new LogHistogramRecorder();
		result.recordEvent( "foo", 1 );
		result.recordEvent( "foo", 10 );
		result.recordEvent( "bar", 100 );		
		return result;
	}
	
	LogHistogramWriter writer;
	LogHistogramParser parser;
	List<EventType> types = new ArrayList<EventType>();
	List<Object> details = new ArrayList<Object>();
	
	@BeforeMethod
	void setUp() {
		writer = new LogHistogramWriter(); 
		parser = new LogHistogramParser();
		types = new ArrayList<EventType>();
		details = new ArrayList<Object>();
		
	}
	
	@Test
	public void testLogFormatter() {
		LogRecord lr = new LogRecord( Level.INFO, "foo" );
		assertEquals( "foo\n", LogHistogramWriter.createJavaLoggingFormatter().format( lr ));
	}
	
	@Test
	public void testStupidEnumForCodeCoverage() {
		EventType x = EventType.values()[0];
		assertSame( x, EventType.valueOf( x.name() ));
	}
	
	@Test
	public void testWriteAndReadHistogram() throws Exception {
		LogHistogram x = makeRecorder().getHistogram( "foo" );
		String xJson = writer.convert( x ).toString();
		LogHistogram xParsed = parser.parseHistogramJson( (JSONObject) JSONValue.parse( xJson ));
		LogHistogramCalculator xParsedCalc = new LogHistogramCalculator( xParsed );
		assertEquals( 2, xParsedCalc.getTotalCount() );
		assertEquals( 11.0, xParsedCalc.getTotalWeight(), .00001 );
		
		LogHistogram empty = new LogHistogram();
		String emptyJson = writer.convert( empty ).toString();
		LogHistogram emptyParsed = parser.parseHistogramJson( (JSONObject) JSONValue.parse( emptyJson ) );
		assertEquals( empty.getBucketMap(), emptyParsed.getBucketMap() );
	}

	@Test
	public void testWriteAndReadInterval() throws Exception {
		LogHistogramRecorder rec = makeRecorder();
		RecordedInterval<LogHistogramRecorder> ri = new RecordedInterval<LogHistogramRecorder>(
				rec, 21, 35);
		String recJson = writer.convert(ri).toString();
		RecordedInterval<LogHistogramRecorder> riParsed = parser
				.parseIntervalJson((JSONObject) JSONValue.parse(recJson));
		assertEquals(21, riParsed.getStartMillis());
		assertEquals(35, riParsed.getEndMillis());
		LogHistogramRecorder recParsed = riParsed.getRecorder();
		assertEquals(makeRecorder().getHistogramMap().keySet(), recParsed
				.getHistogramMap().keySet());
		for (String key : rec.getHistogramMap().keySet()) {
			LogHistogramCalculator c = new LogHistogramCalculator(
					rec.getHistogram(key));
			LogHistogramCalculator cp = new LogHistogramCalculator(
					recParsed.getHistogram(key));
			assertEquals(c.getTotalCount(), cp.getTotalCount());
			assertEquals(c.getTotalWeight(), cp.getTotalWeight(), .00001);
		}
	}

	LogHistogramParser.Listener listener = new LogHistogramParser.Listener() {
		
		@Override
		public void lineParsed(EventType type, String line, Object detail) {
			types.add( type );
			details.add( detail );			
//			System.out.println( type + " " + detail + " " + line);
		}
	};
	
	int quitCount = 0;
	LogHistogramParser.Listener quitter = new LogHistogramParser.Listener() {
		
		@Override
		public void lineParsed(EventType type, String line, Object detail) {
			quitCount++;
			parser.removeListener( quitter );
		}
	};

	void checkParsedIntervals() {
		int foundIntervals = 0;
		for (Object o : details) {
			if (o instanceof RecordedInterval) {
				@SuppressWarnings("unchecked")
				RecordedInterval<LogHistogramRecorder> ri = (RecordedInterval<LogHistogramRecorder>) o;
				++foundIntervals;
				assertEquals((foundIntervals) * 100, ri.getStartMillis());
				assertEquals((foundIntervals) * 100 + 100, ri.getEndMillis());
				if (foundIntervals % 2 == 1) {
					Map<String, LogHistogram> ref = makeRecorder()
							.getHistogramMap();
					Map<String, LogHistogram> map = ri.getRecorder()
							.getHistogramMap();
					assertEquals(ref.keySet(), map.keySet());
					for (String metric : ref.keySet()) {
						LogHistogramCalculator refCalc = new LogHistogramCalculator(
								ref.get(metric));
						LogHistogramCalculator mapCalc = new LogHistogramCalculator(
								map.get(metric));
						assertEquals(refCalc.getTotalCount(),
								mapCalc.getTotalCount(), "For " + metric);
						assertEquals(refCalc.getTotalWeight(),
								mapCalc.getTotalWeight(), "For " + metric);
					}
				} else {
					assertTrue(ri.getRecorder().getHistogramMap().isEmpty());
				}
			}
		}

	}
		
	@Test
	public void testWriteAndReadGoodFile() throws Exception {
		List<EventType> expectedTypes = new ArrayList<EventType>(Arrays
				.asList(new EventType[] { EventType.IGNORED, EventType.INTERVAL_READ,
				EventType.INTERVAL_READ, EventType.IGNORED, EventType.INTERVAL_READ }));
		String x = makeFile( false );
		parser.addListener( listener );
		parser.addListener( quitter );
		parser.parse( new StringReader( x ));
		assertEquals( 1, quitCount ); // test removeListener
		assertEquals( expectedTypes, types );
		checkParsedIntervals();
	}

	@Test
	public void testWriteAndReadBadFile() throws Exception {
		List<EventType> expectedTypes = new ArrayList<EventType>(Arrays
				.asList(new EventType[] { EventType.IGNORED, EventType.ERROR,
						EventType.INTERVAL_READ, EventType.INTERVAL_READ,
						EventType.IGNORED, EventType.ERROR, EventType.IGNORED,
						EventType.ERROR, 
						EventType.INTERVAL_READ }));
		String x = makeFile(true);
		parser.addListener(listener);
		parser.parse(new StringReader(x));
		assertEquals(expectedTypes, types);
		checkParsedIntervals();
	}

	private String makeFile(boolean includeErrors) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Preamble");
		if (includeErrors) { // duplicate start or missing json line
			pw.println(LogHistogramWriter.MESSAGE_START_LINE);
		}
		pw.println(writer.formatMessage(new RecordedInterval<LogHistogramRecorder>(makeRecorder(),
				100, 200)));
		pw.println(writer.formatMessage(new RecordedInterval<LogHistogramRecorder>(
				new LogHistogramRecorder(), 200, 300)));
		pw.println("Whatever.");
		if (includeErrors) {
			StringBuilder sb = new StringBuilder(writer
					.formatMessage(new RecordedInterval<LogHistogramRecorder>(makeRecorder(), 1000,
							2000)));
			sb.insert(sb.indexOf( "\n" ) + 5, "\n" );
			pw.println(sb);
			sb = new StringBuilder(writer
					.formatMessage(new RecordedInterval<LogHistogramRecorder>(makeRecorder(), 1000,
							2000)));
			sb.delete(sb.indexOf( "\n" ) + 2, sb.indexOf( "\n" ) + 3 );
			pw.println(sb);
			
		}
		pw.println(writer.formatMessage(new RecordedInterval<LogHistogramRecorder>(makeRecorder(),
				300, 400)));
		String x = sw.toString();
		return x;
	}

}
