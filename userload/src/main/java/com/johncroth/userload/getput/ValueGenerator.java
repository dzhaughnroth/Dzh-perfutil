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


public class ValueGenerator {
	
	public String createValue( String key ) {
		return System.currentTimeMillis() + ": Created for (" + key + ")";
	}
	
	public String updateValue( String key, String previousValue ) {
		return System.currentTimeMillis() + ": Updated from:\n" + previousValue;
	}
	
}
