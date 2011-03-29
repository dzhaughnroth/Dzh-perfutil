package com.johncroth.userload.getput;

import com.johncroth.userload.ActionSequence;

public class GetPutActionSequence implements ActionSequence, Runnable {
	
	
	Table db;
	String id;
	ValueGenerator valueGenerator;
	
	int updateCount;
	int createCount;
	int readCount;
	int errorCount;
	
	public int getUpdateCount() {
		return updateCount;
	}

	public int getCreateCount() {
		return createCount;
	}

	public int getReadCount() {
		return readCount;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public Table getDb() {
		return db;
	}

	public void setDb(Table db) {
		this.db = db;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ValueGenerator getValueGenerator() {
		return valueGenerator;
	}

	public void setValueGenerator(ValueGenerator valueGenerator) {
		this.valueGenerator = valueGenerator;
	}

	public Runnable nextAction() {
		return this; // todo
	}
	
	public long nextDelay() {
		return 100;
	}
	
	public boolean isDone() {
		return false;
	}
	
	public void run() {
		try {
			String foo = db.get( id );
			++readCount;

			if ( foo == null ) {
				foo = valueGenerator.createValue( id );
				++createCount;
			}
			else {
				foo = valueGenerator.updateValue( id, foo );
				++updateCount;
			}
			db.put( id, foo );
		}
		catch( Throwable t ) {
			logError( id, t );
		}
	}

	protected void logError(String id2, Throwable t) {
		++errorCount;	
	}
	
}
