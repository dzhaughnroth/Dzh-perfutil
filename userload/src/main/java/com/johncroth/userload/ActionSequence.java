package com.johncroth.userload;



public interface ActionSequence {
	
	Runnable nextAction();
	
	long nextDelay();
	
	boolean isDone();
	
}
