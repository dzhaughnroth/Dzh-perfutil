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


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.johncroth.histo.logging.LogHistogramLogger;
import com.johncroth.histo.logging.LogHistogramRecorder;
import com.johncroth.histo.logging.RecordedInterval;

public class LogHistogramLoggerMoreTest extends Assert {
	
	@Test
	public void testInstantFileLogger() throws Exception {
		LogHistogramLogger x = LogHistogramLogger.instantFileLogger( "foo" );
		assertFalse( x.stopped );
		x.stop();
		Thread.sleep( 100 );
		assertTrue( x.stopped );
	}

	boolean gotMessage = false;
	
	@Test
	public void testBogusInterval() throws Exception {
		assertFalse( gotMessage );

		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		LogHistogramLogger lhl = new LogHistogramLogger( ses, 17,
				new LogHistogramLogger.Listener() {

					@Override
					public void intervalRecorded(
							RecordedInterval<LogHistogramRecorder> i) {
						gotMessage = true;
						
					} });
		assertNotNull( lhl );
		assertFalse( gotMessage );
		Thread.sleep( 20  );
		assertTrue( gotMessage );
	}

}