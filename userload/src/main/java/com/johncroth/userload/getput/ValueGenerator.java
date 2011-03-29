package com.johncroth.userload.getput;


public class ValueGenerator {
	
	public String createValue( String key ) {
		return System.currentTimeMillis() + ": Created for (" + key + ")";
	}
	
	public String updateValue( String key, String previousValue ) {
		return System.currentTimeMillis() + ": Updated from:\n" + previousValue;
	}
	
}
