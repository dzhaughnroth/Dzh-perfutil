package com.johncroth.userload.getput;

import java.util.Iterator;
import java.util.Random;


public class IdSequence implements Iterator<String> {
	
	String template = "00000000000000000000";
	
	GaussianIntegerSequence sequence;
	
	public IdSequence( GaussianIntegerSequence seq ) {
		this.sequence = seq;
	}
	
	Random random = new Random();

	@Override
	public boolean hasNext() {
		return true;
	}

	@Override
	public String next() {
		StringBuffer x = new StringBuffer( sequence.next().toString() );
		x.insert( 0, template.substring( x.length()));
		return x.toString();		
	}

	@Override
	public void remove() {
	}

}
