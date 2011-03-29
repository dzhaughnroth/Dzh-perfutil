package com.johncroth.histo.logging;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.johncroth.histo.core.LogHistogramCalculator;
import com.johncroth.histo.logging.Listeners;
import com.johncroth.histo.logging.LogHistogramRecorder;
import com.johncroth.histo.logging.RecordedInterval;
import com.johncroth.histo.logging.Listeners.InstantLoggingListener;
import com.johncroth.histo.logging.Listeners.JavaLoggingListener;

public class ListenersTest extends Assert {
	LogHistogramRecorder rec = new LogHistogramRecorder();
	RecordedInterval<LogHistogramRecorder> ri = makeRi(0, 10);
	
	RecordedInterval<LogHistogramRecorder> makeRi( long start, long end ) {
		rec.recordEvent( "foo", 1 );
		rec.recordEvent( "bar", 2 );
		rec.recordEvent( "bar", 200 );
		return new RecordedInterval<LogHistogramRecorder>( rec, start, end );
	}

	@Test
	public void testAccumulatingListener() {
		new Listeners();
		Listeners.AccumulatingListener al = new Listeners.AccumulatingListener();
		al.intervalRecorded( ri );
		LogHistogramRecorder two = new LogHistogramRecorder();
		two.recordEvent( "foo", 100 );
		two.recordEvent( "baz", 1 );
		RecordedInterval<LogHistogramRecorder> twoRi = new RecordedInterval<LogHistogramRecorder>( two, 5, 30 );
		al.intervalRecorded( twoRi );
		
		RecordedInterval<LogHistogramRecorder> x = al.getAccumulationView();
		assertEquals( 0, x.getStartMillis() );
		assertEquals( 30, x.getEndMillis() );
		Map<String, Long> expectedSizes = new HashMap<String, Long>();
		expectedSizes.put( "foo", 2L );
		expectedSizes.put( "bar", 2L );
		expectedSizes.put( "baz", 1L );		
		Map<String, Long> sizes = new HashMap<String, Long>();
		for( String key : x.getRecorder().getHistogramMap().keySet() ) {
			LogHistogramCalculator c = new LogHistogramCalculator( x.getRecorder().getHistogram( key ));
			sizes.put( key, c.getTotalCount() );
		}
		assertEquals( expectedSizes, sizes );
	}
	
	List<String> messages = new ArrayList<String>();
	
	void deleteAll( File f ) {
		if ( ! f.exists() ) return;
		if ( f.isDirectory() ) {
			for( File g : f.listFiles() ) {
				deleteAll( g );
			}
		}
		f.delete();
	}
	
	@Test
	public void testLoggingListener() throws Exception {
		try {
			JavaLoggingListener jll = new JavaLoggingListener();
			Logger logger = jll.logger;
			for (Handler h : logger.getHandlers()) {
				logger.removeHandler(h);
			}
			logger.setUseParentHandlers(false);
			Handler h = makeHandler();
			logger.addHandler(h);
			jll.intervalRecorded(ri);
			Thread.sleep(10);
			assertEquals(1, messages.size());

			InstantLoggingListener.dirName = "junithistogramstmp";
			File td = new File(System.getProperty("java.io.tmpdir"));
			File hd = new File(td, InstantLoggingListener.dirName);
			deleteAll(hd);

			InstantLoggingListener zll = new InstantLoggingListener();

			zll = new InstantLoggingListener(ListenersTest.class.getName());
			zll.intervalRecorded(ri);
			Thread.sleep(100);
			zll.logger.getHandlers()[0].flush();
			zll.logger.getHandlers()[0].close();
			Thread.sleep(100);
			assertEquals(2, messages.size());
			zll.intervalRecorded(ri);
			zll.intervalRecorded(ri);
			zll = new InstantLoggingListener(ListenersTest.class.getName());
		} catch (Exception e) {
			System.out.println("Tmpdir is "
					+ System.getProperty("java.io.tmpdir"));
			throw e;
		}

	}

	Handler makeHandler() {
		return new Handler() {

			@Override
			public void publish(LogRecord record) {
				messages.add(record.getMessage());
			}

			@Override
			public void flush() {
			}

			@Override
			public void close() throws SecurityException {
			}
		};
	}
}