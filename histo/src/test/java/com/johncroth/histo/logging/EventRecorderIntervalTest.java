package com.johncroth.histo.logging;


import org.testng.Assert;
import org.testng.annotations.Test;

import com.johncroth.histo.logging.LogHistogramRecorder;
import com.johncroth.histo.logging.RecordedInterval;

public class EventRecorderIntervalTest extends Assert {
	
	@Test
	public void testBasics() {
		LogHistogramRecorder x = new LogHistogramRecorder();
		RecordedInterval<LogHistogramRecorder> eri = new RecordedInterval<LogHistogramRecorder>( x, 10, 15 );
		assertEquals( 10, eri.getStartMillis() );
		assertEquals( 15, eri.getEndMillis() );
		assertEquals( 5, eri.getElapsedMillis() );
		assertSame( eri.getRecorder(), x );
	}
}