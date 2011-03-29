package com.johncroth.userload;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JavaUtilActionSequenceExecutor extends ActionSequenceExecutor {
	
	public JavaUtilActionSequenceExecutor( ScheduledExecutorService service ) {
		this( new NaiveDelayGovernor(), service);
	}

	public JavaUtilActionSequenceExecutor( DelayGovernor guvnuh, ScheduledExecutorService service ) {
		super( guvnuh );
		this.service = service;
	}

	ScheduledExecutorService service;

	public ScheduledExecutorService getService() {
		return service;
	}

	protected void schedule( Runnable r, long delay, TimeUnit unit ) {
		service.schedule( r, delay, TimeUnit.MILLISECONDS );
	}

	@Override
	public void shutdown() {
		service.shutdown();
	}
	
	@Override
	public boolean isShutdown() {
		return service.isShutdown();
	}
}
