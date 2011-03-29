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


import org.testng.Assert;
import org.testng.annotations.Test;

import com.johncroth.histo.client.EventRecorder;
import com.johncroth.histo.core.LogHistogramCalculator;
import com.johncroth.histo.logging.LogHistogramRecorder;
import com.johncroth.histo.logging.RecordedInterval;
import com.johncroth.histo.logging.RollingRecorder;

public class RollingRecorderTest extends Assert {
	RollingRecorder<LogHistogramRecorder> rer;

	@Test
	public void testBasics() throws Exception {
		long start = System.currentTimeMillis();
		rer = RollingRecorder.forType( LogHistogramRecorder.class );
		Thread.sleep( 10 );
		rer.recordEvent( "foo", 12 );
		Thread.sleep( 10 );
		long prerolla = System.currentTimeMillis();
		RecordedInterval<LogHistogramRecorder> a = rer.roll();
		long rolla = System.currentTimeMillis();
		rer.recordEvent( "bar", 20 );
		assertOneEvent( "foo", a.getRecorder() );
		Thread.sleep( 10 );
		RecordedInterval<LogHistogramRecorder> b = rer.roll();
		long rollb = System.currentTimeMillis();
		assertNotSame( a.getRecorder(), b.getRecorder() );
		assertTrue( a.getStartMillis() >= start );
		assertTrue( a.getEndMillis() - a.getStartMillis() <= rolla - start );
		assertTrue( b.getStartMillis() <= a.getEndMillis() );
		assertTrue( b.getEndMillis() - b.getStartMillis() <= rollb - prerolla );
		assertOneEvent( "foo", a.getRecorder() );
		assertOneEvent( "bar", b.getRecorder() );
	}
	
	@Test
	public void testFailure() throws Exception {
		try {
			RollingRecorder.forType( SabotagedRecorder.class );
			fail();
		}
		catch( RuntimeException e ) {
			assertEquals( e.getCause().getMessage(), MSG );
		}
		sabotage = false;
		RollingRecorder<SabotagedRecorder> rr = RollingRecorder.forType( SabotagedRecorder.class );
		rr.roll();
		sabotage = true;
		try {
			rr.roll();
			fail();
		}
		catch( RuntimeException e ) {
			assertEquals( e.getCause().getMessage(), MSG );
		}		
		
	}
	
	static boolean sabotage = true;
	static String MSG = "sabotage";
	
	static class SabotagedRecorder implements EventRecorder {
		
		public SabotagedRecorder() {
			if ( sabotage ) {
				throw new RuntimeException( MSG );
			}
		}

		public void recordEvent(String type, Number size) {
			
		}
		
	}

	void assertOneEvent( String type, EventRecorder source ) {
		LogHistogramRecorder r = (LogHistogramRecorder) source;
		LogHistogramCalculator c = new LogHistogramCalculator( r.getHistogram( type ));
		assertEquals( 1, c.getTotalCount(),  "For " + type + " got " + c.getTotalCount() );
	}
}