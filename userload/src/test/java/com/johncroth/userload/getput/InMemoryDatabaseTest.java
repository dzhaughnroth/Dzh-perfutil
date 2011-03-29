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
