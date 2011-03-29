package com.johncroth.userload.getput;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.johncroth.userload.getput.ValueGenerator;


public class ValueGeneratorTest extends Assert {
	
	ValueGenerator vg;
	
	@BeforeMethod
	public void setUp() {
		vg = new ValueGenerator();
	}
	
	@Test
	public void testBasic() {
		String now = String.valueOf( System.currentTimeMillis() / 1000L );
		String val = vg.createValue( "foo" );
		assertTrue( val.contains( now.toString() ));
		assertTrue( val.contains( "foo" ));
		
		String val2 = vg.updateValue( "foo", val );
		assertTrue( val2.endsWith( val ));
		String delta = val2.substring( 0, val2.length() - val.length() );
		assertTrue( delta.contains( now ));
		assertTrue( delta.contains( "Updated" ));

		String val3 = vg.updateValue( "foo", val2 );
		assertTrue( val3.endsWith( val2 ));
		delta = val3.substring( 0, val3.length() - val2.length() );
		assertTrue( delta.contains( now ));
		assertTrue( delta.contains( "Updated" ));
		
	}
}
