package com.johncroth.userload;

/**
 * Trivial implementation of DelayGovernor; ignores reports, recommends
 * unchanged delays
 */
public class NaiveDelayGovernor implements DelayGovernor {
	
	public void reportDelay( long intendedDelay, long requestedDelay, long actualDelay ) {
	}
	
	public long recommendActualDelay( long desiredDelay ) {
		return desiredDelay;
	}

}
