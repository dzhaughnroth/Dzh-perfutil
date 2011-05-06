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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;


public class ActionSeqTest extends Assert {

	Runnable a1 = new Runnable() {
		@Override
		public void run() {
		}
	};
	
	List<Runnable> list = new ArrayList<Runnable>( Arrays.asList( a1 ) );
	
	@Test
	public void testPlain() throws Exception {
		ActionSeq seq = new ActionSeq( Seq.constant( a1 ), 10L );
		assertTrue( seq.hasNext() );
		for ( int i = 0; i <3; i++ ) {
			assertSame( a1, seq.next() );
			assertSame( a1, seq.nextAction() );
			assertEquals( new Long( 10L ), seq.nextDelay() );
		}
	}
	
	@Test
	public void testRemove() throws Exception {
		ActionSeq seq = new ActionSeq( list.iterator(), Seq.constant( 0L ));
		assertTrue( seq.hasNext() );
		assertEquals( a1, seq.next() );
		seq.remove();
		assertEquals( 0, list.size() );
		
		assertEquals( seq.getDelays().next(), new Long( 0 ) );
		assertSame( seq.iterator(), seq );
	}
}
