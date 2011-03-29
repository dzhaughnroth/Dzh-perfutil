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
