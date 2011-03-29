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