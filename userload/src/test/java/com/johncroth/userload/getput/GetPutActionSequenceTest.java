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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.johncroth.userload.ActionSequenceExecutor;
import com.johncroth.userload.JavaUtilActionSequenceExecutor;
import com.johncroth.userload.getput.GaussianIntegerSequence;
import com.johncroth.userload.getput.GetPutActionSequence;
import com.johncroth.userload.getput.IdSequence;
import com.johncroth.userload.getput.InMemoryTable;
import com.johncroth.userload.getput.ValueGenerator;


public class GetPutActionSequenceTest extends Assert {
	
	ActionSequenceExecutor exec;
	
	@BeforeMethod
	public void setUp() throws Exception {
		exec = new JavaUtilActionSequenceExecutor( Executors.newScheduledThreadPool( 10 ));
	}
	
	IdSequence idSeq = new IdSequence( new GaussianIntegerSequence( 500000000, 5000000, 1000000 ));
	InMemoryTable db = new InMemoryTable();
	ValueGenerator vg = new ValueGenerator();
	List<GetPutActionSequence> seqs = new ArrayList<GetPutActionSequence>();
	DistributedHashtableForTesting tdb = new DistributedHashtableForTesting( db );
	
	GetPutActionSequence create() {
		GetPutActionSequence result = new GetPutActionSequence();
		result.setDb( tdb );
		result.setValueGenerator( vg );
		result.setId( idSeq.next() );
		assertSame( result.getDb(), tdb  );
		assertSame( result.getValueGenerator(), vg );
		assertNotNull( result.getId() );
		return result;		
	}

	@Test
	public void testBasics() {
		GetPutActionSequence seq = create();
		assertEquals( 0, seq.getCreateCount());
		seq.nextAction().run();
		assertEquals( 1, db.map.size() );
		String val = db.get( db.map.keySet().iterator().next() );
		assertNotNull ( val );
		seq.run();
		String val2 = db.get( db.map.keySet().iterator().next() );
		assertFalse( val.equals( val2 ), val + " did not change.");
		assertEquals( 1, seq.getCreateCount());
	}
	
	class NastyDb extends InMemoryTable {
		@Override
		public void put( String key, String value ) {
			throw new RuntimeException( "I don't wanna." );
		}
	}
	@Test
	public void testErrors() throws Exception {
		GetPutActionSequence seq = create();
		seq.setDb( new NastyDb() );
		seqs.add( seq );
		exec.addActionSequence( seq );
		Thread.sleep( 200 );
		exec.shutdown();
		Thread.sleep( 200 );
		assertTrue( seq.getErrorCount() > 0 );
		assertEquals( seq.getErrorCount(), seq.getReadCount() );
		assertEquals( 0, seq.getUpdateCount() );
		
	}
	
}
