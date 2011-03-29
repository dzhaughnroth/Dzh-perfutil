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
package com.johncroth.histo.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class RawTimerProxyFactory {
	
	static final String DEFAULT_FAILURE_SUFFIX = "-failed";

	protected class Handler implements InvocationHandler {
		Object target;
		
		private Object doInvoke( Method method, Object[] args ) throws Throwable {
			try {
				return method.invoke( target, args);
			}
			catch( InvocationTargetException e ) {
				throw e.getTargetException();
			}			
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			if ( !methodsToMonitor.contains( method.getName() ) ) {
				System.out.println( "Skipping " + method.getName() );
				return doInvoke(method, args);
			}
			else {
				StringBuilder sb = new StringBuilder( metricNamePrefix );
				sb.append( method.getName() );
				long start = System.currentTimeMillis();
				try {
					return doInvoke(method, args);
				}
				catch( Throwable t ) {
					sb.append( failureSuffix );
					throw t;
				}
				finally {
					long elapsed = System.currentTimeMillis() - start;
					recorder.recordEvent( sb.toString(), elapsed );
				}
			}
		}
	}

	String failureSuffix = DEFAULT_FAILURE_SUFFIX;
	EventRecorder recorder;
	String metricNamePrefix;
	Set<String> methodsToMonitor;
	
	/**
	 * WARNING: make sure the parameters are immutable, at least thread safe esp
	 * the interfaces and methodsToMonitor.
	 * 
	 * @param recorder
	 * @param interfacesToImplement
	 * @param metricNamePrefix
	 * @param failureSuffix
	 * @param methodsToMonitor
	 */

	protected RawTimerProxyFactory(EventRecorder recorder,
			String metricNamePrefix, String failureSuffix,
			Set<String> methodsToMonitor) {
		this.recorder = recorder;
		this.methodsToMonitor = methodsToMonitor;
		this.metricNamePrefix = metricNamePrefix;
		if (failureSuffix != null) {
			this.failureSuffix = failureSuffix;
		}
	}
	
	static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
	
	@SuppressWarnings("unchecked")
	public <T> T wrap( T target) {
		Handler h = new Handler();
		h.target = target;
		return (T) Proxy.newProxyInstance(target.getClass().getClassLoader(),
				allImplementedInterfaces(target).toArray(EMPTY_CLASS_ARRAY), h);
	}

	static void addAllImplementedInterfaces( Class<?> c, Collection<Class<?>> accumulator ) {
		if ( c == null ) return;
		for( Class<?> i : c.getInterfaces() ) {
			accumulator.add( i );
		}
		addAllImplementedInterfaces( c.getSuperclass(), accumulator );	
	}
	
	public static Set<Class<?>> allImplementedInterfaces( Object o ) {
		Set<Class<?>> result = new LinkedHashSet<Class<?>>();
		addAllImplementedInterfaces( o.getClass(), result );
		return result;
	}
	

}
