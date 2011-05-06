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
package com.johncroth.userload;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JavaUtilActionSequenceExecutor extends ActionSequenceExecutor {
	
	public JavaUtilActionSequenceExecutor( ScheduledExecutorService service ) {
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
		super.shutdown();
		service.shutdown();
	}
	
	@Override
	public boolean isShutdown() {
		super.isShutdown();
		return service.isShutdown();
	}
}
