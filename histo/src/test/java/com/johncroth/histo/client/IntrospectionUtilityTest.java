package com.johncroth.histo.client;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.johncroth.histo.client.RawTimerProxyFactory;

public class IntrospectionUtilityTest extends Assert {
	
	static interface A {
		void a();
	}
	
	static interface B {
		void b();
	}
	
	static interface C extends A, B {
		
	}
	
	static interface B2 {
		void b();
	}
	
	static class ImplA implements C, B2 { // yeah, pathological.

		@Override
		public void a() {
		}

		@Override
		public void b() {
		}
		
	}
	
	static class ImplB implements A {

		@Override
		public void a() {
		}

		public void b() {
		}
		
	}
	
	class ImplC extends ImplB implements B {
	}

	
	static class MySet extends HashSet<Object> implements B {
		private static final long serialVersionUID = 1L;

		@Override
		public void b() {
		}
	}
	
	@Test
	public void testAllImplementedInterfaces() {
		// this is the problem we solve.
		assertTrue( new MySet() instanceof Collection );
		assertFalse( Arrays.asList( MySet.class.getInterfaces() ).contains( Set.class ));
		Set<Class<?>> ifs = RawTimerProxyFactory.allImplementedInterfaces( new MySet() );
		assertTrue( ifs.contains( Set.class ));
		ifs = RawTimerProxyFactory.allImplementedInterfaces( C.class );
		assertFalse( ifs.contains( C.class ));
		assertFalse( ifs.contains( B.class ));	// doesn't implement them.
		ifs = RawTimerProxyFactory.allImplementedInterfaces( new ImplA() );
		assertTrue( ifs.contains( C.class ));
		assertFalse( ifs.contains(B.class), "Contains a superfluous super interface it doesn't need." ); 
		// We do not guarantee a minimal set, however; HashSet yields both Set
		// and Collection, for example.
	}
	
}
