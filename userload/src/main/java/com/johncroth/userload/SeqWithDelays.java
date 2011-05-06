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

import java.util.Iterator;

/**
 * An sequence of actions, plus a sequence of delays between them.
 */

public class SeqWithDelays<T> implements Iterator<T>, Iterable<T>{
	
	Iterator<T> items;
	Iterator<Long> delays;
	
	public SeqWithDelays(Iterator<T> items, Iterator<Long> delays) {
		this.items = items;
		this.delays = delays;
	}

	public Iterator<Long> getDelays() {
		return delays;
	}
	
	public Long nextDelay() {
		return delays.next();
	}

	@Override
	public boolean hasNext() {
		return items.hasNext();
	}

	@Override
	public T next() {
		return items.next();
	}

	/** Not implemented */
	@Override
	public void remove() {
		items.remove();
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}
	
}
