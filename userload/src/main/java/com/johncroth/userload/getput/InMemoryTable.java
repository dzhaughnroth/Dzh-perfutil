/**
 * Perfutil -- https://github.com/dzhaughnroth/Dzh-perfutil 
 * (C) 2011 John Charles Roth
 *
 * Perfutil is free software, licensed under the terms of the GNU GPL 
 * Version 2 or, at your option, any later version. You should have 
 * received a copy of the license with this file. See the above web address
 * for more information, or contact the Free Software Foundation, Boston, MA. 
 * It is distributed WITHOUT WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
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
