package com.johncroth.testutil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BeanPropertiesTester {
	
	public static final String SET_TO_NULL_FAILED = "SET TO NULL FAILED";
	public static final String RESET_FAILED = "RESET FAILED.";
	Object example;
	Map<String, String> successes = new HashMap<String,String>();
	public Map<String, String> getSuccesses() {
		return successes;
	}

	public Map<String, String> getFailures() {
		return failures;
	}

	Map<String, String> failures = new HashMap<String,String>();
	
	public BeanPropertiesTester( Object example ) {
		this.example = example;
	}
	static final String GET = "get";
	static final String SET = "set";
	List<Method> findGetters() {
		ArrayList<Method> getters = new ArrayList<Method>();
		for ( Method m : example.getClass().getMethods() ) {
			if ( m.getName().startsWith( GET ) ) {
				if ( m.getParameterTypes().length == 0 ) {
					getters.add( m );
				}
			}
		}
		return getters;
	}
	
	boolean isMatchingSetter( Method method, Method getter ) {
		boolean result = false;
		if ( method.getName().startsWith( SET ) ) {
			if ( method.getName().substring( 1 ).equals( getter.getName().substring( 1 ))) {
				if ( method.getParameterTypes().length == 1 &&
						method.getParameterTypes()[0].equals( getter.getReturnType() ) ) {
					result = true;
				}
			}
		}
		return result;
	}
	
	Method findSetterFor( Method getter ) {
		Method result = null;
		for ( Method m : example.getClass().getMethods() ) {
			if ( isMatchingSetter( m, getter ) ) {
				result = m;
				break;
			}
		}
		return result;
	}
	
	public void testProperties() {
		for ( Method getter : findGetters() ) {
			try {
				Object oldValue = getter.invoke( example );
				Method setter = findSetterFor( getter );
				boolean failed = false;
				if ( setter != null && ! getter.getReturnType().isPrimitive()) {
					setter.invoke( example, new Object[] { null } );
					Object newValue = getter.invoke( example );
					if ( newValue == null ) {
						setter.invoke( example, oldValue );
						Object resetValue = getter.invoke( example );
						if ( resetValue != oldValue ) {
							failures.put( getter.getName(), RESET_FAILED );
							failed = true;
						}
					}
					else {
						failures.put( getter.getName(), SET_TO_NULL_FAILED );
						failed = true;
					}
				}
				if ( ! failed ) {
					successes.put( getter.getName(), setter == null ? "" : setter.getName() );
				}
			}
			catch( Throwable e ) {
				failures.put( getter.getName(), String.valueOf( e ) );
//				e.printStackTrace();
			}
		}
	}
	
	
}
