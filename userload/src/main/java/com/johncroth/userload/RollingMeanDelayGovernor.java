package com.johncroth.userload;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Trivial implementation of DelayGovernor; ignores reports, recommends
 * unchanged delays
 */
public class RollingMeanDelayGovernor implements DelayGovernor {

	/** Recommends adjusting according to the mean of the last 50 events. */
	public RollingMeanDelayGovernor() {
		this(50);
	}

	/**
	 * Recommends adjusting according to the mean of the last rollThreshold
	 * events.
	 */
	public RollingMeanDelayGovernor(int rollThreshold) {
		this.rollThreshold = rollThreshold;
	}

	int rollThreshold = 0;
	int accumulatedDiffs = 0;
	int accumulatedCount = 0;
	// assert: Using an Atomic here save synchronizing on recommendActualDelay
	// The catch is that we might give stale recommendations; but no problem,
	// as long as they are consistent.
	AtomicInteger recommendedDifference = new AtomicInteger();

	public synchronized void reportDelay(long intendedDelay,
			long requestedDelay, long actualDelay) {
		accumulatedCount++;
		accumulatedDiffs += actualDelay - requestedDelay;
		if (accumulatedCount == rollThreshold) {
			recommendedDifference.set(accumulatedDiffs / accumulatedCount);
			accumulatedCount = 0;
			accumulatedDiffs = 0;
		}
	}

	public long recommendActualDelay(long desiredDelay) {
		return Math.max( 0, desiredDelay - recommendedDifference.get() );
	}

}
