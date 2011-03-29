package com.johncroth.histo.client;


import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.johncroth.histo.client.RawTimerProxyFactory;
import com.johncroth.histo.core.LogHistogramCalculator;
import com.johncroth.histo.logging.LogHistogramRecorder;

public class RawTimerProxyFactoryTest extends Assert {
	
	static interface Foo {
		String getFoo();
		String getIgnored();
	}
	
	static interface Bar {
		String getBar();
	}
	
	static interface Baz extends Bar {
		String getBaz();
	}

	class Impl implements Foo, Baz {


		@Override
		public String getBaz() {
			return "baz";
		}

		@Override
		public String getBar() {
			sleep();
			if ( fail ) {
				throw new Error( "Haha!" );
			}
			return "bar";
		}

		@Override
		public String getFoo() {
			sleep();
			if ( fail ) {
				throw new IllegalArgumentException( "Ha!" );
			}

			return "foo";
		}

		@Override
		public String getIgnored() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	boolean fail = false;
	long millisToSleep = 5;

	void sleep( ) {
		try {
			Thread.sleep( millisToSleep );
		}
		catch( Exception e ) {
			
		}
	}
	
	static final String PREFIX = "stuff-";

	RawTimerProxyFactory factory;
	LogHistogramRecorder recorder = new LogHistogramRecorder();
	Set<String> methods = new HashSet<String>( Arrays.asList( "getBar", "getFoo", "zoo" ) );	

	@BeforeMethod
	public void setUp() {
		recorder = new LogHistogramRecorder();
		factory = new RawTimerProxyFactory( recorder, PREFIX, null, methods );
	}
	
	@Test
	public void testBasics() {
		Impl i = new Impl();
		Object o = factory.wrap( i );
		assertTrue( o instanceof Foo );
		assertTrue( o instanceof Bar );
		assertTrue( o instanceof Baz );
		Foo foo = (Foo) o;
		Baz baz =  (Baz) o;
		foo.getFoo();
		foo.getIgnored();
		baz.getBar();
		baz.getBaz();
		
		fail = true;
		try {
			foo.getFoo();
			fail();
		}
		catch( IllegalArgumentException e ) {
			assertEquals( "Ha!", e.getMessage() );
		}
		millisToSleep = 5;
		Bar bar2 = (Bar) factory.wrap( new Impl() );
		try {
			bar2.getBar();
			fail();
		}
		catch (Error e ) {
			assertEquals( "Haha!", e.getMessage() );
		}
		fail = false;
		baz.getBar();
		bar2.getBar();
		Map<String,Integer> xc = new HashMap<String,Integer>();
		xc.put( PREFIX + "getFoo", 1 );
		xc.put( PREFIX + "getBar", 3 );
		xc.put( PREFIX + "getFoo" + RawTimerProxyFactory.DEFAULT_FAILURE_SUFFIX, 1);
		xc.put( PREFIX + "getBar" + RawTimerProxyFactory.DEFAULT_FAILURE_SUFFIX, 1 );
		assertEquals( xc.keySet(), recorder.getHistogramMap().keySet() );
		for ( String x : xc.keySet() ) {
			LogHistogramCalculator calc = new LogHistogramCalculator( recorder.getHistogram( x ));
			assertEquals( (int) xc.get(x), calc.getTotalCount(), "For " + x );
		}
	
	}
	
	@Test
	public void testSuffixFeature() throws Exception {
		factory = new RawTimerProxyFactory(recorder, "", "-zap",
				new HashSet<String>(Arrays.asList("getIgnored", "getFoo")));
		Foo foo = (Foo) factory.wrap(new Impl());
		fail = true;
		try {
			foo.getFoo();
			fail();
		} catch (IllegalArgumentException e) {

		}
		foo.getIgnored();
		assertEquals(2, recorder.getHistogramMap().size(), "" + recorder.getHistogramMap().keySet());
		assertNotNull(recorder.getHistogram("getFoo-zap"));
		assertNotNull(recorder.getHistogram("getIgnored"));
	}

}
