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
package com.johncroth.histo.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.johncroth.histo.util.Notifier;

public class NotifierTest extends Assert {

	// Sample listener interface.
	static interface Listener {
		void eventOccurred(String name);
	}

	// Sample subclass of Notifier
	static class MyNotifier extends Notifier<Listener> {

		void sendEvent( final String name ) {
			for( Listener l : currentListeners() ) {
				l.eventOccurred( name );
			}
		}

		Object lockingObject() {
			return listeners;
		}
	}

	// A simple listener.
	static class MyListener implements Listener {
		List<String> events = new ArrayList<String>();

		public void eventOccurred(String x) {
			events.add(x);
		}
	}

	volatile boolean sleepOver = false;

	@Test
	public void testSynchronization() throws Exception {
		final MyNotifier x = new MyNotifier();
		MyListener ml = new MyListener();
		x.addListener(ml);
		assertNotSame( x.listeners, x.currentListeners() );
		assertEquals( x.listeners, x.currentListeners() );
		new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized (x.lockingObject()) {
					try {
						Thread.sleep(100);
						sleepOver = true;
					} catch (InterruptedException e) {
					}
				}

			}
		}).start();

		Thread.sleep(10);
		long now = System.currentTimeMillis();
		assertFalse(sleepOver);
		// must acquire lock.
		x.sendEvent("hi");
//		System.out.println( System.currentTimeMillis() - now );
		assertTrue(sleepOver, "Didn't wait to aquire lock " + ( System.currentTimeMillis() - now ) );
		x.sendEvent("buy");
		assertEquals( ml.events, Arrays.asList( "hi", "buy" ));
		assertEquals( 1, x.currentListeners().size() );
		x.removeListener( ml );
		assertEquals( 0, x.currentListeners().size() );
	}

}
