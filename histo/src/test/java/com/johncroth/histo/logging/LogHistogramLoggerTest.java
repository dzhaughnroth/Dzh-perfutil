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


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.johncroth.histo.core.LogHistogram;
import com.johncroth.histo.core.LogHistogramCalculator;
import com.johncroth.histo.logging.LogHistogramLogger;
import com.johncroth.histo.logging.LogHistogramRecorder;
import com.johncroth.histo.logging.RecordedInterval;

public class LogHistogramLoggerTest extends Assert {
	
	List<RecordedInterval<LogHistogramRecorder>> received;
	
	LogHistogramLogger.Listener listener;

	LogHistogramLogger logger;
	ScheduledExecutorService exec;
	volatile boolean started = false;
	
	@BeforeMethod
	public void setUp() throws Exception {
		exec = Executors.newScheduledThreadPool( 2 );
		// Contrive to start near the middle of a second hand tick; need some margin
		// for the executor to start.
		long st = System.currentTimeMillis();
		for( ; st % 1000 < 200 || st % 1000 > 800; st = System.currentTimeMillis() ) {
			Thread.sleep( 100 );
		}
		
		started = true;
		received = new ArrayList<RecordedInterval<LogHistogramRecorder>>();
		listener = new LogHistogramLogger.Listener() {
			
			@Override
			public void intervalRecorded(RecordedInterval<LogHistogramRecorder> i) {
				if (started) {
					received.add(i);
				}
			}
		};
		logger = new LogHistogramLogger( exec, 1000, listener, quitter );
	}

	@Test
	public void testLogger() throws InterruptedException {
		assertEquals(0, received.size(), "" + received);
		String[] names = { "foo", "bar", "baz" };	
		for( int i = 0; i < 3; i++ ) {
			// record 100 events then sleep until a second has elapsed.
			long now = System.currentTimeMillis();
			for (int j = 0; j < 100; j++) {
				logger.recordEvent(names[j % 3], 1000000);
				Thread.sleep(1);
			}
			while( System.currentTimeMillis() - now < 1000 ) { 
				Thread.sleep( 10 );			
			}
		}
		logger.recordEvent("last", 1);
		logger.stop();
		logger.rollAndLog.run(); // a noop now; code coverage.
		assertEquals(4, received.size(), "" + received);
		assertTrue(received.get(0).getElapsedMillis() <= 900, received.get(0).getElapsedMillis() + " for first");
		assertTrue(received.get(3).getElapsedMillis() <= 900, received.get(3).getElapsedMillis() + " for last" );
		for (int i = 1; i < 3; i++) {
			assertTrue(received.get(i).getElapsedMillis() > 980, "For " + i
					+ " was " + received.get(i).getElapsedMillis());
			assertTrue(received.get(i).getElapsedMillis() < 1020, "For " + i
					+ " was " + received.get(i).getElapsedMillis());
		}
		LogHistogram netnet = new LogHistogram();
		boolean gotLast = false;
		for (RecordedInterval<LogHistogramRecorder> ri : received) {
			for (LogHistogram lh : ri.getRecorder().getHistogramMap().values()) {
				netnet.absorb(lh);
			}
			if (ri.getRecorder().getHistogramMap().keySet().contains("last")) {
				if (!gotLast) {
					gotLast = true;
				} else {
					fail("Got Last event twice.");
				}
			}
		}
		LogHistogramCalculator c = new LogHistogramCalculator(netnet);
		assertTrue(gotLast);
		assertEquals(301, c.getTotalCount());
		assertEquals(1, quitterReceivedCount);
	}
	
	int quitterReceivedCount;
	LogHistogramLogger.Listener quitter = new LogHistogramLogger.Listener() {
		@Override
		public void intervalRecorded(RecordedInterval<LogHistogramRecorder> i) {
			logger.removeListener(quitter);
			++quitterReceivedCount;
		}
	};
	
}