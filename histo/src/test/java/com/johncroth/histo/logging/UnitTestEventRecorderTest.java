package com.johncroth.histo.logging;


import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.johncroth.histo.core.LogHistogram;
import com.johncroth.histo.core.LogHistogramCalculator;
import com.johncroth.histo.logging.UnitTestEventRecorder;

public class UnitTestEventRecorderTest extends Assert {
	
	@Test
	public void testBasics() {
		UnitTestEventRecorder rec = new UnitTestEventRecorder();
		rec.recordEvent( "foo", 210 );
		rec.recordEvent( "foo", 21 );
		rec.recordEvent( "bar", 21 );
		Map<String,LogHistogram> logrec = rec.getLogHistogramRecorder().getHistogramMap();
		assertEquals( 2, logrec.size() );
		LogHistogramCalculator fooCalc = new LogHistogramCalculator( (LogHistogram) logrec.get( "foo" ));
		LogHistogramCalculator barCalc = new LogHistogramCalculator( (LogHistogram) logrec.get( "bar" ));
		assertEquals( 2, fooCalc.getTotalCount() );
		assertEquals( 1, barCalc.getTotalCount() );		
		assertEquals( 3, rec.getEvents().size() );
		for( UnitTestEventRecorder.Event e : rec.events ) {
			assertTrue( "foo".equals( e.type ) || "bar".equals( e.type ));
			assertTrue( 21 == e.size.intValue() || 210 == e.size.intValue() );
			assertEquals( 0, e.toString().indexOf( e.type ) );
		}
	}

}