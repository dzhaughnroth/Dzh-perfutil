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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.johncroth.userload.getput.InMemoryTable;
import com.johncroth.userload.getput.Table;


public class InMemoryDatabaseTest extends Assert {
	
	Table db;
	
	@BeforeMethod
	public void setUp() {
		db = new InMemoryTable();
	}
	
	@Test
	public void testBasic() {
		assertNull( db.get( "foo" ));
		db.put( "foo", "Whatever" );
		assertEquals( "Whatever", db.get("foo" ));
		db.put( "foo", "Nevermind" );
		db.put( "bar", "Whatever" );
		assertEquals( "Whatever", db.get("bar" ));
		assertEquals( "Nevermind", db.get( "foo" ));
	}
}
