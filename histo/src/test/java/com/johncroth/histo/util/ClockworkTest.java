package com.johncroth.histo.util;


import java.util.Calendar;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.johncroth.histo.util.Clockwork;

/** 
 * Try to match the second/minutes/hours hand.
 */

public class ClockworkTest extends Assert {


	@Test
	public void testTimeOfNextSyncWithShortPeriod( ) {
		new Clockwork(); // code coverage
		Calendar c = Calendar.getInstance();
		c.set( Calendar.MILLISECOND, 132 );
		c.set( Calendar.SECOND, 23 );
		c.set( Calendar.MINUTE, 5 );
		long now = c.getTimeInMillis();
		long next = Clockwork.timeOfNextSync( 250, now );
		assertEquals( next % Clockwork.ONE_SECOND, 250 );
		assertTrue( next > now );
		c.set( Calendar.MILLISECOND, 821 );
		now = c.getTimeInMillis();
		next = Clockwork.timeOfNextSync( 250, now );
		assertEquals( next % Clockwork.ONE_SECOND, 0 );
		assertTrue( next > now );
		
		next = Clockwork.timeOfNextSync( 50, now );
		assertEquals( next % Clockwork.ONE_SECOND, 850 );
		assertTrue( next > now );		
	}
	@Test
	public void testTimeOfNextSyncWithMediumPeriod( ) {
		Calendar c = Calendar.getInstance();
		c.set( Calendar.MILLISECOND, 132 );
		c.set( Calendar.SECOND, 23 );
		c.set( Calendar.MINUTE, 5 );
		long now = c.getTimeInMillis();
		long next = Clockwork.timeOfNextSync( 12 * Clockwork.ONE_SECOND, now );
		assertEquals( next % Clockwork.ONE_MINUTE, 24 * Clockwork.ONE_SECOND );
		assertTrue( next > now );
		c.set( Calendar.SECOND, 25 );
		now = c.getTimeInMillis();
		next = Clockwork.timeOfNextSync( 12 * Clockwork.ONE_SECOND, now );
		assertEquals( next % Clockwork.ONE_MINUTE, 36 * Clockwork.ONE_SECOND );
		assertTrue( next > now );
		
		next = Clockwork.timeOfNextSync( 5000, now );
		assertEquals( next % 60000, 30000 );
		assertTrue( next > now );
		c.set( Calendar.MILLISECOND, 0 );
		now = c.getTimeInMillis();
		next = Clockwork.timeOfNextSync( 5 * Clockwork.ONE_SECOND, now );
		// This seems correct enough, although 25000 might be more correct.
		assertEquals( next % Clockwork.ONE_MINUTE, 30 * Clockwork.ONE_SECOND );
		assertTrue( next > now );

	}
	@Test
	public void testTimeOfNextSyncWithLongPeriod( ) {
		Calendar c = Calendar.getInstance();
		c.set( Calendar.MILLISECOND, 132 );
		c.set( Calendar.SECOND, 23 );
		c.set( Calendar.MINUTE, 5 );
		long now = c.getTimeInMillis();
		long next = Clockwork.timeOfNextSync( 5 * Clockwork.ONE_MINUTE, now );
		assertEquals( next % Clockwork.ONE_HOUR, 600000 );
		assertTrue( next > now );
		c.set( Calendar.SECOND, 25 );
		c.set( Calendar.MINUTE, 8 );
		now = c.getTimeInMillis();
		next = Clockwork.timeOfNextSync( 5 * Clockwork.ONE_MINUTE, now );
		assertEquals( next % Clockwork.ONE_HOUR, 600000 );
		assertTrue( next > now );
		
		next = Clockwork.timeOfNextSync( 3 * Clockwork.ONE_MINUTE, now );
		assertEquals( next % Clockwork.ONE_HOUR, 9 * Clockwork.ONE_MINUTE );
		assertTrue( next > now );

	}
	@Test
	public void testNoWayToSync() {
		Calendar c = Calendar.getInstance();
		c.set( Calendar.MILLISECOND, 132 );
		c.set( Calendar.SECOND, 23 );
		c.set( Calendar.MINUTE, 5 );
		assertEquals( 0, Clockwork.timeOfNextSync( 17000, c.getTimeInMillis() ) );
		assertEquals( 0, Clockwork.timeOfNextSync( Clockwork.ONE_HOUR * 25, c.getTimeInMillis() ));
	}

	
}
