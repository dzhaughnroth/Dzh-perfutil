package com.johncroth.userload.getput;

import com.johncroth.userload.getput.Table;


public class DistributedHashtableForTesting implements Table {
	
	static class Exception extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public Exception() {
			super( "Intentional for testing" );
		}
	}

	Table delegate;
	
	public DistributedHashtableForTesting( Table delegate ) {
		this.delegate = delegate;
	}

	long nextGetDelay = 20;
	long nextPutDelay = 50;
	boolean nextGetFail = false;
	boolean nextPutFail = false;	
		
	public long getNextGetDelay() {
		return nextGetDelay;
	}

	public void setNextGetDelay(long nextGetDelay) {
		this.nextGetDelay = nextGetDelay;
	}

	public long getNextPutDelay() {
		return nextPutDelay;
	}

	public void setNextPutDelay(long nextPutDelay) {
		this.nextPutDelay = nextPutDelay;
	}

	public boolean isNextGetFail() {
		return nextGetFail;
	}

	public void setNextGetFail(boolean nextGetFail) {
		this.nextGetFail = nextGetFail;
	}

	public boolean isNextPutFail() {
		return nextPutFail;
	}

	public void setNextPutFail(boolean nextPutFail) {
		this.nextPutFail = nextPutFail;
	}

	void sleep( long delay ) {
		try {
			Thread.sleep( delay );
		}
		catch( InterruptedException e ) {
			throw new RuntimeException( e );
		}
	}

	@Override
	public String get(String key) {
		sleep( getNextGetDelay() );
		if ( isNextGetFail() ) {
			throw new Exception();
		}
		return delegate.get( key );		
	}

	@Override
	public void put(String key, String value) {
		sleep( getNextPutDelay() );
		if ( isNextPutFail() ) {
			throw new Exception();
		}
		delegate.put( key, value );
	}

	
	
}