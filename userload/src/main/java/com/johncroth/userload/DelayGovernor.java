package com.johncroth.userload;

public interface DelayGovernor {
	
	void reportDelay( long intendedDelay, long requestedDelay, long actualDelay );
	
	long recommendActualDelay( long desiredDelay );

}
