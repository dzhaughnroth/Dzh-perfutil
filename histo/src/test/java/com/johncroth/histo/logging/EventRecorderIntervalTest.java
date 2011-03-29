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