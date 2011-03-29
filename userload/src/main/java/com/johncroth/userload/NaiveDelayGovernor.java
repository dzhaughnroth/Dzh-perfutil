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
