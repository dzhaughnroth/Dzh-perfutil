package com.johncroth.histo.client;


import org.testng.Assert;
import org.testng.annotations.Test;

import com.johncroth.histo.client.DefaultEventRecorder;
import com.johncroth.histo.client.EventRecorder;
import com.johncroth.histo.core.LogHistogramCalculator;
import com.johncroth.histo.logging.LogHistogramLogger;
import com.johncroth.histo.logging.LogHistogramRecorder;

public class DefaultEventRecorderTest extends Assert {
	
	@Test
	public void testBasics() {
		EventRecorder x = DefaultEventRecorder.get();
		x.recordEvent( "foo", 21 );
		LogHistogramRecorder rec = new LogHistogramRecorder();
		DefaultEventRecorder.setDefault( rec );
		assertNotSame( DefaultEventRecorder.get(), rec );
		DefaultEventRecorder.get().recordEvent( "foo", 210 );
		LogHistogramCalculator calc = new LogHistogramCalculator( rec.getHistogram( "foo" ));
		assertEquals( calc.getTotalCount(), 1);
	}
	
	@Test
	public void testWithInstantLogger() throws Exception {
		DefaultEventRecorder.useInstantLogHistogramLogger( "junitfoo" );
		assertTrue( DefaultEventRecorder.delegate instanceof LogHistogramLogger );
		LogHistogramLogger lhl = (LogHistogramLogger) DefaultEventRecorder.delegate;
		lhl.stop();
	}
}