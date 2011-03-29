package com.johncroth.userload.getput;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link Table} implemented with a synchronized {@link Map}, mainly for testing.
 */
public class InMemoryTable implements Table {
	
	Map<String, String> map = Collections.synchronizedMap( new HashMap<String,String>() );
	
	public String get( String key ) {
		return map.get( key );
	}
	
	public void put( String key, String value ) {
		map.put( key, value );
	}
		
}
