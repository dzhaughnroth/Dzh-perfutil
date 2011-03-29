package com.johncroth.userload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinScheduledExecutorService implements ScheduledExecutorService {
	
	int executorCount = 10;
	int threadsPerExecutor = 10;
	ScheduledExecutorService[] workers;
	AtomicInteger current = new AtomicInteger(0);

	public RoundRobinScheduledExecutorService( int executors, int threadsPer ) {
		executorCount = executors;
		threadsPerExecutor = threadsPer;
		workers = new ScheduledExecutorService[ executorCount ];
		for ( int i = 0; i < workers.length; i++ ) {
			workers[i] = Executors.newScheduledThreadPool( threadsPerExecutor );
		}			
	}

	@Override
	public void shutdown() {
		for( ScheduledExecutorService w : workers ) {
			w.shutdown();
		}
	}

	@Override
	public List<Runnable> shutdownNow() {
		ArrayList<Runnable> result = new ArrayList<Runnable>();
		for( ScheduledExecutorService w : workers ) {
			result.addAll( w.shutdownNow() );
		}
		return result;
	}

	@Override
	public boolean isShutdown() {
		boolean result = true;
		for( ScheduledExecutorService w : workers ) {
			result = result && w.isShutdown();
		}
		return result;
	}

	@Override
	public boolean isTerminated() {
		boolean result = true;
		for( ScheduledExecutorService w : workers ) {
			result = result && w.isTerminated();
		}
		return result;		
	}

	@Override
	// TODO does not respect timeout.
	public boolean awaitTermination(long timeout, TimeUnit unit)
			throws InterruptedException {
		boolean result = true;
		for( ScheduledExecutorService w : workers ) {
			result = result && w.awaitTermination(timeout, unit);
		}
		return result;
	}

	ScheduledExecutorService nextWorker() {
		return workers[current.incrementAndGet() % executorCount];
	}
	
	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return nextWorker().submit( task );
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return nextWorker().submit( task, result );
	}

	@Override
	public Future<?> submit(Runnable task) {
		return nextWorker().submit( task );
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
			throws InterruptedException {
		return nextWorker().invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(
			Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		return nextWorker().invokeAll(tasks, timeout, unit );
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
			throws InterruptedException, ExecutionException {
		return nextWorker().invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
			long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		return nextWorker().invokeAny(tasks, timeout, unit);
	}

	@Override
	public void execute(Runnable command) {
		nextWorker().execute(command);		
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay,
			TimeUnit unit) {
		return nextWorker().schedule(command, delay, unit);
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay,
			TimeUnit unit) {
		return nextWorker().schedule(callable, delay, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
			long initialDelay, long period, TimeUnit unit) {
		return nextWorker().scheduleAtFixedRate(command, initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
			long initialDelay, long delay, TimeUnit unit) {
		return nextWorker().scheduleWithFixedDelay(command, initialDelay, delay, unit);
	}	
	
}