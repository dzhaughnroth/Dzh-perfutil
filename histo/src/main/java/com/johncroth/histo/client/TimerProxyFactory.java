package com.johncroth.histo.client;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

public class TimerProxyFactory {
	
	public static final String DEFAULT_FAILURE_SUFFIX = RawTimerProxyFactory.DEFAULT_FAILURE_SUFFIX;

	static final Class<?>[] EMPTY = new Class[0];

	public TimerProxyFactory( EventRecorder recorder, Class<?>[] interfacesToMonitor ) {
		this.recorder = recorder;
		this.interfacesToMonitor = interfacesToMonitor;
		buildMethodNameSet( null );
	}

	public TimerProxyFactory( EventRecorder recorder, Class<?>[] interfacesToMonitor, Pattern methodNameFilter ) {
		this( recorder, interfacesToMonitor  );
		buildMethodNameSet( methodNameFilter );
		
	}

	public TimerProxyFactory( EventRecorder recorder, Class<?>[] interfacesToMonitor, String regex ) {
		this( recorder, interfacesToMonitor, Pattern.compile( regex ) );
	}

	
	public TimerProxyFactory( EventRecorder recorder, Class<?>[] interfacesToMonitor, String regex, int regexFlags ) {
		this( recorder, interfacesToMonitor, Pattern.compile( regex, regexFlags ) );
	}

	EventRecorder recorder;
	Class<?>[] interfacesToMonitor;
	String metricPrefix = "";
	String failureSuffix = DEFAULT_FAILURE_SUFFIX;

	public String getMetricPrefix() {
		return metricPrefix;
	}

	public void setMetricPrefix(String metricPrefix) {
		this.metricPrefix = metricPrefix;
	}

	public String getFailureSuffix() {
		return failureSuffix;
	}

	public void setFailureSuffix(String failureSuffix) {
		this.failureSuffix = failureSuffix;
	}

	Set<String> methodNames;
	
	void buildMethodNameSet( Pattern p ){
		methodNames = new HashSet<String>();
		for( Class<?> c : interfacesToMonitor ) {
			for ( Method m : c.getMethods() ) {
				methodNames.add( m.getName() );				
			}
		}
		if ( p != null ) {
			for ( Iterator<String> i = methodNames.iterator(); i.hasNext(); ) {
				if ( p.matcher( i.next() ).matches() ) {
					i.remove();
				}
			}
		}
		
	}

	/**
	 * Create a proxy with {@link #wrap(T, String)} with
	 * {@link #getMetricPrefix()}.
	 */
	public <T> T wrap( T target ) {
		return wrap( target, metricPrefix );
	}
	
	/**
	 * Create proxy for target that measures method invocations time. Metrics
	 * sent to the {@link EventRecorder} are the method names with the supplied
	 * alternativePrefix in front. 
	 */
	public <T> T wrap(T target, String alternativePrefix) {
		RawTimerProxyFactory raw = new RawTimerProxyFactory(recorder,
				metricPrefix, failureSuffix, methodNames);
		return raw.wrap(target);

	}
	
}