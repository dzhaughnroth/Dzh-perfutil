package com.johncroth.histo.client;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.johncroth.histo.client.TimerProxyFactory;
import com.johncroth.histo.logging.LogHistogramRecorder;

public class TimerProxyFactoryTest extends Assert {
	
	TimerProxyFactory factory;
	LogHistogramRecorder recorder = new LogHistogramRecorder();
	
	@BeforeMethod
	public void setUp() {
		recorder = new LogHistogramRecorder();
		factory = new TimerProxyFactory(recorder, new Class[] { Set.class }, "ADDA.*", Pattern.CASE_INSENSITIVE );

	}
	
	@Test
	public void testRecording() {
		Set<String> x = (Set<String>) factory.wrap( new HashSet<String>() );
		x.addAll( Arrays.asList( new String[] {"foo", "bar" } )); // unrecorded per regex.
		x.iterator();
		x.contains( "bar" );
		assertEquals( 2, recorder.getHistogramMap().size() );
		assertTrue( recorder.getHistogramMap().keySet().contains( "iterator" ));
		assertTrue( recorder.getHistogramMap().keySet().contains( "contains" ));
	}
	
	@Test
	public void testNoFlags() { // mainly for code coverage
		factory = new TimerProxyFactory( recorder, new Class[] {Set.class}, "ADDA.*" );
		Set<String> x = (Set<String>) factory.wrap( new HashSet<String>() );
		x.addAll( Arrays.asList( new String[] {"foo", "bar" } )); // now recorded per regex.
		assertEquals( 1, recorder.getHistogramMap().size() );
	}
	
	@Test
	public void testPrefixAndSuffix() {
		factory.setMetricPrefix( "pre-" );
		factory.setFailureSuffix( "-ouch" );
		assertEquals( factory.getMetricPrefix() + factory.getFailureSuffix(), "pre--ouch");
		Set<String> x = (Set<String>) factory.wrap( Collections.unmodifiableSet( new HashSet<String>() ));
		try {
			x.add( "foo" );
			fail();
		}
		catch( UnsupportedOperationException e ) {
		}
		x.iterator();
		assertEquals( 2, recorder.getHistogramMap().size() );
		assertTrue( recorder.getHistogramMap().keySet().contains( "pre-iterator" ));
		assertTrue( recorder.getHistogramMap().keySet().contains( "pre-add-ouch" ));		
	}
}
